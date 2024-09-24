
package com.example.DjFinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.User;

public class UserViewModel extends ViewModel {
    private LiveData<User> user;
    public LiveData<User> getUser() {
        this.user = Model.getInstance().getLoggedUser();
        return user;
    }

    public void refreshUser() {
        this.user = Model.getInstance().getLoggedUser();
    }



}
