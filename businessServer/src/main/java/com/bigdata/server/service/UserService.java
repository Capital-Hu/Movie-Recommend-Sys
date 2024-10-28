package com.bigdata.server.service;

import com.bigdata.server.model.core.User;
import com.bigdata.server.model.request.LoginUserRequest;
import com.bigdata.server.model.request.RegisterUserRequest;
import com.bigdata.server.model.request.UpdateUserGenresRequest;
import com.bigdata.java.model.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> getUserCollection(){
        if (null == userCollection)
            this.userCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_USER_COLLECTION);
        return this.userCollection;
    }

    private Document userToDocument(User user){
        try {
            return Document.parse(objectMapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private User documentToUser(Document document){
        try {
            return objectMapper.readValue(JSON.serialize(document),User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(RegisterUserRequest request){

        if (getUserCollection().find(new Document("username", request.getUsername())).first() != null)
            return false;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFirst(true);

        Document document = userToDocument(user);
        if (null == document)
            return false;
        getUserCollection().insertOne(document);
        return true;
    }

    public boolean loginUser(LoginUserRequest request){
        Document document = getUserCollection().find(new Document("username", request.getUsername())).first();
        if (null == document)
            return false;
        User user = documentToUser(document);
        if (null == user)
            return false;
        return user.getPassword().compareTo(request.getPassword()) == 0;
    }

    public void updateUserGenres(UpdateUserGenresRequest request){
        getUserCollection().updateOne(new Document("username", request.getUsername()),new Document().append("$set",new Document("genres",request.getGenres())));
        getUserCollection().updateOne(new Document("username", request.getUsername()),new Document().append("$set",new Document("first",false)));
    }

    public User findUserByUsername(String username){
        Document document = getUserCollection().find(new Document("username", username)).first();
        if (null == document || document.isEmpty())
            return null;
        return documentToUser(document);
    }

}
