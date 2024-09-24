package com.example.DjFinder.restAPI;

import com.google.gson.annotations.SerializedName;

public class Quote {
    @SerializedName("text")
    private String text;

    @SerializedName("author")
    private String author;

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }
}