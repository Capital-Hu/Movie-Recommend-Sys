package com.bigdata.server.service;


import com.bigdata.server.model.core.Rating;
import com.bigdata.server.model.request.GetUserRatingsRequest;
import com.bigdata.java.model.Constant;
import com.bigdata.server.rest.MovieRestApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jedis jedis;

    private Logger logger = LoggerFactory.getLogger(MovieRestApi.class);

    private MongoCollection<Document> ratingCollection;

    private MongoCollection<Document> getRatingCollection(){
        if (null == ratingCollection)
            this.ratingCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_RATING_COLLECTION);
        return this.ratingCollection;
    }

    private Rating documentToRating(Document document){
        try {
            Rating rating = objectMapper.readValue(JSON.serialize(document),Rating.class);
            return rating;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document ratingToDocument(Rating rating){
        try {
            Document document = Document.parse(objectMapper.writeValueAsString(rating));
            return document;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateRedis(Rating rating){
        if (jedis.llen("uid:"+rating.getUid())>= Constant.USER_RATING_QUEUE_SIZE)
            jedis.rpop("uid:"+rating.getUid());
        jedis.lpush("uid:"+rating.getUid(),rating.getMid()+":"+rating.getScore());
    }

    public void rateToMovie(Rating rating){
        Document query = new Document("uid", rating.getUid())
                .append("mid", rating.getMid());

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true);

        Document update = new Document("$set", ratingToDocument(rating));

        logger.info("Query Document: " + query.toJson());
        logger.info("Update Document: " + update.toJson());

        try {
            Document updatedDoc = getRatingCollection().findOneAndUpdate(query, update, options);
            if (updatedDoc == null) {
                logger.info("No document was updated or inserted.");
            } else {
                logger.info("Updated Document: " + updatedDoc.toJson());
            }
        } catch (Exception e) {
            logger.error("Error updating rating: " + e.getMessage(), e);
        }

        updateRedis(rating);
    }

    public List<Rating> getUserRatings(GetUserRatingsRequest request){
        FindIterable<Document> documents = getRatingCollection().find(Filters.eq("uid",request.getUid()));
        List<Rating> result = new ArrayList<>();
        for (Document item: documents) {
            result.add(documentToRating(item));
        }
        return result;
    }

}
