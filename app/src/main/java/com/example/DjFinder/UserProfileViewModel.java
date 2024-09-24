package com.example.DjFinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;

import java.util.List;

public class UserProfileViewModel extends ViewModel {

    private LiveData<List<Post>> data;

    public LiveData<List<Post>> getData(String username) {
        this.data = Model.getInstance().getUserPosts(username);
        return data;
    }

}
