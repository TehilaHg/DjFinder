package com.example.DjFinder.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.DjFinder.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Post {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String userName = "";
    public String avatarUrl = "";
    public String djName = "";
    public String djDescription = "";
    public String eventDate= "";
    public String djUrl = "";
    public String likeUrl = "";
    public String recommender = "";
    public Long lastUpdated;


    public Post(){}

    public Post(String id, String userName, String recommender, String djName, String djDescription,
                String eventDate, String avatarUrl, String djUrl, String likeUrl)
    {
        this.id = id;
        this.userName = userName;
        this.djName = djName;
        this.djDescription = djDescription;
        this.eventDate = eventDate;
        this.avatarUrl = avatarUrl;
        this.djUrl = djUrl;
        this.likeUrl = likeUrl;
        this.recommender = recommender;
    }

    static final String ID = "id";
    static final String USER_NAME = "username";
    static final String DJ_NAME = "djName";
    static final String DESCRIPTION = "description";
    static final String DATE = "date";
    static final String AVATAR = "avatar";
    static final String DJ_IMAGE = "djImg";
    static final String LIKE = "like";
    static final String RECOMMENDER = "recommender";
    static final String COLLECTION = "posts";
    static final String LAST_UPDATED = "lastUpdated";
    static final String LOCAL_LAST_UPDATED = "posts_local_last_updated";


    public static Post fromJson(Map<String,Object> json)
    {
        String id = (String)json.get(ID);
        String username = (String)json.get(USER_NAME);
        String djName = (String)json.get(DJ_NAME);
        String description = (String)json.get(DESCRIPTION);
        String date = (String)json.get((DATE));
        String avatar = (String)json.get((AVATAR));
        String djImg = (String)json.get((DJ_IMAGE));
        String like = (String)json.get((LIKE));
        String recommender = (String)json.get((RECOMMENDER));

        Post post = new Post(id, username, recommender, djName, description, date, avatar, djImg, like);

       try{
           Timestamp time = (Timestamp) json.get(LAST_UPDATED);
           post.setLastUpdated(time.getSeconds());
       }catch (Exception e) {}

       return post;
    }

    public static Long getPostLocalLastUpdate() {
        SharedPreferences sharedPef = MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE);
        return sharedPef.getLong(LOCAL_LAST_UPDATED,0);
    }

    public static void setPostLocalLastUpdate(Long time) {
        MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE)
        .edit().putLong(LOCAL_LAST_UPDATED, time).commit();
    }

    public Map<String,Object> toJson()
    {
        Map<String, Object> json = new HashMap<>();
        json.put(ID, getId());
        json.put(USER_NAME, getUserName());
        json.put(DJ_NAME, getDjName());
        json.put(DESCRIPTION, getDjDescription());
        json.put(DATE, getEventDate());
        json.put(AVATAR, getAvatarUrl());
        json.put(DJ_IMAGE, getDjUrl());
        json.put(LIKE, getLikeUrl());
        json.put(RECOMMENDER,getRecommender());
        // time update
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        return json;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    public String getDjName() {
        return djName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getDjDescription() {
        return djDescription;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public String getDjUrl() {
        return djUrl;
    }
    public String getLikeUrl() {
        return likeUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setDjUrl(String djUrl) {
        this.djUrl = djUrl;
    }

    public void setLikeUrl(String likeUrl) {
        this.likeUrl = likeUrl;
    }

    public void setDjName(String djName) {
        this.djName = djName;
    }

    public void setDjDescription(String djDescription) {
        this.djDescription = djDescription;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
