package com.activity.pis_azil.network;

import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UserRoleModel;
import com.activity.pis_azil.models.ViewAllModel;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.Query;

public interface ApiService {
    @GET("Korisnici")
    Call<List<UserModel>> getAllUsers();

    @GET("Korisnici/user_id/{id_korisnika}")
    Call<UserByEmailResponseModel> getUserById(@Path("id_korisnika") int id);

    @GET("Korisnici/email/{email}")
    Call<UserByEmailResponseModel> getUserByIdEmail(@Path("email") String email);

    @GET("Korisnici/lozinka/{email}")
    Call<ResponseBody> getPasswordByEmail(@Path("email") String email);

    @GET("Korisnici/role/{id_korisnika}")
    Call<UserRoleModel> getUserRoleById(@Path("id_korisnika") int userId);

    @PUT("Korisnici/update/{id_korisnika}")
    Call<Void> updateUser(@Header("RequestUserId") int requestUserId, @Path("id_korisnika") int id, @Body Map<String, Object> updates);

    @POST("Korisnici/add")
    Call<Void> addUser(@Header("RequestUserId") int requestUserId, @Body UserModel user);

    @PUT("KucniLjubimci/update/{id}")
    Call<Void> updateAnimal(@Path("id") String id, @Body Map<String, Object> updateData);

    @DELETE("KucniLjubimci/delete/{id}")
    Call<Void> deleteAnimal(@Path("id") String id);

    @GET("KucniLjubimci")
    Call<List<AnimalModel>> getAllAnimals();

    @GET("KucniLjubimci/{type}")
    Call<List<AnimalModel>> getAnimalsByType(@Path("type") String type);

    @GET("KucniLjubimci/{id}")
    Call<AnimalModel> getAnimalById(@Path("id") int id);

    @POST("KucniLjubimci/add")
    Call<Void> addAnimal(@Body ViewAllModel animal);

    @GET("AdoptedAnimals")
    Call<List<AnimalModel>> getAdoptedAnimals();

    @GET("AdoptedAnimals/search")
    Call<List<UserModel>> searchUsersByName(@Query("startText") String startText, @Query("endText") String endText);

    @POST("AdoptedAnimals/adopters")
    Call<List<AnimalModel>> getAnimalsForAdopters(@Body List<String> adopterIds);

    @POST("DnevnikUdomljavanja/add")
    Call<Void> addAdoption(@Body MyAdoptionModel adoptionModel);

    @GET("SearchAnimals")
    Call<List<AnimalModel>> searchAnimals(@Query("keyword") String keyword);
}
