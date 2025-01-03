package com.bigdata.server.service;

import com.bigdata.server.model.core.Tag;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> tagCollection;

    private MongoCollection<Document> getTagCollection(){
        if (null == tagCollection)
            this.tagCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_TAG_COLLECTION);
        return this.tagCollection;
    }

    private Tag documentToTag(Document document){
        try {
            Tag tag = objectMapper.readValue(JSON.serialize(document),Tag.class);
            return tag;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document tagToDocument(Tag tag){
        try {
            Document document = Document.parse(objectMapper.writeValueAsString(tag));
            return document;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Tag> getMovieTags(int mid){
        FindIterable<Document> documents = tagCollection.find(Filters.eq("mid", mid));
        List<Tag> tags = new ArrayList<>();
        for (Document item:documents) {
            tags.add(documentToTag(item));
        }
        return tags;
    }

    public void addTagToMovie(Tag tag){
        tagCollection.insertOne(tagToDocument(tag));
    }



}
