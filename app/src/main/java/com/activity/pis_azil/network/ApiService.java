package com.activity.pis_azil.network;

import com.activity.pis_azil.models.RejectAdoptionModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateDnevnikModel;
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

    @PUT("KucniLjubimci/update/{id}")
    Call<Void> updateAnimal(@Path("id") int id, @Body AnimalModel animalModel);

    @PUT("KucniLjubimci/{id}/odbij")
    Call<Void> rejectAnimal(@Path("id") int id, @Body AnimalModel animalModel);

    @PUT("KucniLjubimci/{id}/udomi")
    Call<Void> adoptAnimal(@Path("id") int id);

    @GET("GetFilteredAnimalsByAgeRange")
    Call<List<AnimalModel>> getFilteredAnimalsByAgeRange(
            @Query("tipLjubimca") String tipLjubimca,
            @Query("dobMin") Integer dobMin,
            @Query("dobMax") Integer dobMax,
            @Query("boja") String boja
    );

    @POST("KucniLjubimci/add")
    Call<Void> addAnimal(@Header("RequestAnimalId") int requestAnimalId, @Body ViewAllModel animal);

    @GET("AdoptedAnimals")
    Call<List<AnimalModel>> getAdoptedAnimals();

    @GET("dnevnik_udomljavanja")
    Call<List<UpdateDnevnikModel>> getDnevnikUdomljavanja();

    @POST("DnevnikUdomljavanja/add")
    Call<Void> addAdoption(@Body MyAdoptionModel adoptionModel);

    @PUT("DnevnikUdomljavanja/update/{id}")
    Call<Void> updateAdoption(@Header("RequestAnimalId") int requestAnimalId, @Path("id") int id, @Body UpdateDnevnikModel adoptionModel);

    @DELETE("DnevnikUdomljavanja/delete/{id}")
    Call<Void> deleteAdoption(@Header("RequestAnimalId") int requestAnimalId, @Path("id") int id);

    @GET("DnevnikUdomljavanja/{idLjubimca}/status")
    Call<Boolean> getAdoptionStatus(@Path("idLjubimca") int idLjubimca);

    @PUT("DnevnikUdomljavanja/{idLjubimca}/update/status")
    Call<Void> updateAdoptionStatus(@Path("idLjubimca") int idLjubimca, @Body Map<String, Boolean> statusUpdate);

    @GET("SearchAnimals")
    Call<List<AnimalModel>> searchAnimals(@Query("keyword") String keyword);

    @GET("odbijene_zivotinje")
    Call<List<RejectAdoptionModelRead>> getOdbijeneZivotinje();

    @POST("OdbijeneZivotinje")
    Call<Void> createOdbijenaZivotinja(@Body RejectAdoptionModel rejectAdoptionModel);

    @DELETE("OdbijeneZivotinje/{id}")
    Call<Void> deleteOdbijenaZivotinja(@Path("id") int id);
}
