package com.activity.pis_azil.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> userId = new MutableLiveData<>();

    public void setUserId(String id) {
        userId.setValue(id);
    }

    public LiveData<String> getUserId() {
        return userId;
    }
}
