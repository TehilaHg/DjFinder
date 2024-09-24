package com.example.DjFinder.model;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class ListConverter {

    @TypeConverter
    public String fromLikedPostsList(List<String> likedPosts)
    {
        StringBuilder sb = new StringBuilder();

        for (String likedPost : likedPosts)
        {
            sb.append(likedPost);
            sb.append(",");
        }

        return sb.toString();
    }

    @TypeConverter
    public List<String> tolikedPostsList(String data) {
        return Arrays.asList(data.split(","));
    }

}
