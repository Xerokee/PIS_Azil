package com.activity.pis_azil.network;

import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.ViewAllModel;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.Query;

public interface ApiService {
    @GET("Korisnici")
    Call<List<UserModel>> getAllUsers();

    @GET("Korisnici/user_id/{id_korisnika}")
    Call<UserModel> getUserById(@Path("id_korisnika") int id);

    @POST("Korisnici/add")
    Call<Void> addUser(@Body UserModel user);

    @PUT("Korisnici/update/{id}")
    Call<Void> updateUser(@Path("id") int id, @Body Map<String, Object> userUpdates);

    @DELETE("Korisnici/delete/{id}")
    Call<Void> deleteUser(@Path("id") int id);

    @GET("KucniLjubimci")
    Call<List<AnimalModel>> getAllAnimals();

    @GET("KucniLjubimci/{type}")
    Call<List<AnimalModel>> getAnimalsByType(@Path("type") String type);

    @POST("KucniLjubimci/add")
    Call<Void> addAnimal(@Body ViewAllModel animal);

    @PUT("KucniLjubimci/update/{id}")
    Call<Void> updateAnimal(@Path("id") String id, @Body Map<String, Object> updateData);

    @DELETE("KucniLjubimci/delete/{id}")
    Call<Void> deleteAnimal(@Path("id") String id);

    @GET("AdoptedAnimals")
    Call<List<AnimalModel>> getAdoptedAnimals();

    @GET("AdoptedAnimals/search")
    Call<List<UserModel>> searchUsersByName(@Query("startText") String startText, @Query("endText") String endText);

    @POST("AdoptedAnimals/adopters")
    Call<List<AnimalModel>> getAnimalsForAdopters(@Body List<String> adopterIds);

    @POST("AdoptedAnimals/add")
    Call<Void> addAdoption(@Body MyAdoptionModel adoption);

    @GET("SearchAnimals")
    Call<List<AnimalModel>> searchAnimals(@Query("keyword") String keyword);
}
