package com.bigdata.server.service;

import com.bigdata.server.model.core.Movie;
import com.bigdata.java.model.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;


    private MongoCollection<Document> movieCollection;

    private MongoCollection<Document> getMovieCollection() {
        if (null == movieCollection)
            this.movieCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_MOVIE_COLLECTION);
        return this.movieCollection;
    }

    private Movie documentToMovie(Document document){
        try {
            Movie movie = objectMapper.readValue(JSON.serialize(document),Movie.class);
            Document score = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_AVERAGE_MOVIES).find(Filters.eq("mid", movie.getMid())).first();
            if (score == null || score.isEmpty())
                movie.setScore(00);
            else {
                double avg = score.getDouble("avg");
                BigDecimal value = new BigDecimal(avg * 2);
                value = value.setScale(1, RoundingMode.HALF_UP); // 保留一位小数并进行四舍五入
                double finalValue = value.doubleValue();
                movie.setScore(finalValue);
            }
            return movie;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document movieToDocument(Movie movie){
        try {
            Document document = Document.parse(objectMapper.writeValueAsString(movie));
            return document;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> getMoviesByMids(List<Integer> ids) {
        List<Movie> result = new ArrayList<>();
        FindIterable<Document> documents = getMovieCollection().find(Filters.in("mid", ids));
        for (Document item: documents) {
            result.add(documentToMovie(item));
        }
        return result;
    }

    public Movie findMovieInfo(int mid){
        Document movieDocument = getMovieCollection().find(new Document("mid",mid)).first();
        if (null == movieDocument || movieDocument.isEmpty())
            return null;
        return documentToMovie(movieDocument);
    }

}
