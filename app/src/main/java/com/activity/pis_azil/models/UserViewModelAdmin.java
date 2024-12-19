package com.activity.pis_azil.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModelAdmin extends ViewModel {
    private MutableLiveData<UserModel> userLiveData;
    private MutableLiveData<List<UserModel>> allUsersLiveData;
    private ApiService apiService;

    public UserViewModelAdmin() {
        apiService = ApiClient.getClient().create(ApiService.class);
        userLiveData = new MutableLiveData<>();
        allUsersLiveData = new MutableLiveData<>();
    }

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public LiveData<List<UserModel>> getAllUsers() {
        return allUsersLiveData;
    }

    public void fetchUserData(String email) {
        apiService.getUserByIdEmail(email).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserByEmailResponseModel userByEmailResponseModel = response.body();
                    UserModel user = userByEmailResponseModel.getResult();
                    if (user != null) {
                        userLiveData.setValue(user);
                    } else {
                        // Handle case where user is not found in the result
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                // Handle error
            }
        });
    }


    public void fetchAllUsers() {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsersLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                // Handle error
            }
        });
    }

    public void deleteUser(UserModel user) {
    }
}
