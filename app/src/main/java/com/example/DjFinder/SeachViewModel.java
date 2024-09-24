package com.example.DjFinder;

import androidx.lifecycle.ViewModel;

import com.example.DjFinder.model.Post;

import java.util.List;

public class SeachViewModel extends ViewModel {
    private List<Post> data;
    public List<Post> getData() {
        return data;
    }
    void setData(List<Post> data){this.data=data;}
}
