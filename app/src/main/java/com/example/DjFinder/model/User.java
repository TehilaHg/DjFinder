package com.example.DjFinder.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import android.content.Context;
import android.content.SharedPreferences;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.DjFinder.MyApplication;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@TypeConverters(ListConverter.class)
public class User {
    @PrimaryKey
    @NonNull
    public String email = "";
    public String userName = "";
    public String avatar = "";
    public List<String> likedPosts;
    public Long lastUpdated;

    public User(){}

    public User(String userName, String avatar, String email, List<String> likedPosts){
        this.userName = userName;
        this.avatar = avatar;
        this.email = email;
        this.likedPosts = likedPosts;
    }

    static final String USER_NAME = "username";
    static final String AVATAR = "avatar";
    static final String EMAIL = "email";
    static final String LIKED_POSTS = "liked_posts";
    static final String COLLECTION = "users";
    static final String LAST_UPDATED = "lastUpdated";
    static final String LOCAL_LAST_UPDATED = "users_local_last_updated";

    public static User fromJson(Map<String,Object> json)
    {
        String username = (String)json.get(USER_NAME);
        String avatar = (String)json.get(AVATAR);
        String email = (String)json.get(EMAIL);
        List<String> likedPosts = (List<String>)json.get(LIKED_POSTS);

        User user = new User(username, avatar, email, likedPosts);

        try{
            Timestamp time = (Timestamp) json.get(LAST_UPDATED);
            user.setUserLastUpdated(time.getSeconds());
        }catch (Exception e) {}

        return user;

    }

    public static Map<String,Object> toJson(User user) {
        Map<String, Object> json = new HashMap<>();
        json.put(USER_NAME, user.getUserName());
        json.put(AVATAR, user.getAvatar());
        json.put(EMAIL, user.getEmail());
        json.put(LIKED_POSTS, user.getLikedPosts());
        // time update
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        return json;
    }

    public static Long getUserLocalLastUpdate() {
        SharedPreferences sharedPef = MyApplication.getMyContext()
                .getSharedPreferences("TAG", Context.MODE_PRIVATE);
        return sharedPef.getLong(LOCAL_LAST_UPDATED,0);
    }

    public static void setUserLocalLastUpdate(Long time) {
        MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE)
                .edit().putLong(LOCAL_LAST_UPDATED, time).commit();
    }

    public List<String> getLikedPosts() {
        return likedPosts;
    }

    public void setLikedPosts(List<String> likedPosts) {
        this.likedPosts = likedPosts;
    }

    public Long getUserLastUpdated() {
        return lastUpdated;
    }

    public void setUserLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUserName() {
        return userName;
    }

    public  void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

}
