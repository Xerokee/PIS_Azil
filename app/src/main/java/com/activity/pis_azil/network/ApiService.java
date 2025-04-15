package com.activity.pis_azil.network;

import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.models.Meeting;
import com.activity.pis_azil.models.NewMeeting;
import com.activity.pis_azil.models.RejectAdoptionModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.SifrBojaLjubimca;
import com.activity.pis_azil.models.SifrTipLjubimca;
import com.activity.pis_azil.models.SlikaModel;
import com.activity.pis_azil.models.StatistikaModel;
import com.activity.pis_azil.models.UpdateAnimalModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UserRoleModel;
import com.activity.pis_azil.models.ViewAllModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    @DELETE("Korisnici/delete/{id}")
    Call<Void> deleteUser(@Header("RequestUserId") int requestUserId, @Path("id") int userId);

    @PUT("KucniLjubimci/update/{id}")
    Call<Void> updateAnimal(@Path("id") String id, @Body Map<String, Object> updateData);

    @DELETE("KucniLjubimci/delete/{id}")
    Call<Void> deleteAnimal(@Path("id") String id);

    @GET("KucniLjubimci")
    Call<List<AnimalModel>> getAllAnimals();

    @GET("animals")
    Call<List<AnimalModel>> getAnimalsByType(@Query("type") Integer type);

    @GET("KucniLjubimci/{type}")
    Call<List<AnimalModel>> getAnimalsByType(@Path("type") String type);

    @GET("KucniLjubimci/{id}")
    Call<AnimalModel> getAnimalById(@Path("id") int id);

    @PUT("KucniLjubimci/update/{id}")
    Call<Void> updateAnimal(@Path("id") int id, @Body UpdateDnevnikModel animalModel);

    @PUT("KucniLjubimci/{id}/odbij")
    Call<Void> rejectAnimal(@Path("id") int id);

    @PUT("KucniLjubimci/{id}/udomi")
    Call<Void> adoptAnimal(@Path("id") int id);

    @GET("GetFilteredAnimalsByAgeRange")
    Call<List<AnimalModel>> getFilteredAnimalsByAgeRange(
            @Query("tipLjubimca") String tipLjubimca,
            @Query("minDob") Integer minDob,
            @Query("maxDob") Integer maxDob,
            @Query("dob") Integer dob,
            @Query("boja") String boja
    );

    @GET("sifrTipLjubimca")
    Call<List<SifrTipLjubimca>> getAnimalTypes();

    @GET("sifrBojaLjubimca")
    Call<List<SifrBojaLjubimca>> getAnimalColors();

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

    @PUT("DnevnikUdomljavanja/{idLjubimca}/vrati/{idUdomljavanja}")
    Call<Void> updateAdoptionStatus(@Path("idLjubimca") int idLjubimca, @Path("idUdomljavanja") int idUdomljavanja);

    @GET("SearchAnimals")
    Call<List<AnimalModel>> searchAnimals(@Query("keyword") String keyword);

    @GET("odbijene_zivotinje")
    Call<List<RejectAdoptionModelRead>> getOdbijeneZivotinje();

    @POST("OdbijeneZivotinje")
    Call<Void> createOdbijenaZivotinja(@Body RejectAdoptionModel rejectAdoptionModel);

    @DELETE("OdbijeneZivotinje/{id}")
    Call<Void> deleteOdbijenaZivotinja(@Path("id") int id);

    @POST("KucniLjubimci/{id}/update")
    Call<Void> updateAnimalDetail(@Path("id") int id, @Body UpdateAnimalModel updateAnimalModel);

    @GET("Aktivnosti/{id}")
    Call<HttpRequestResponseList<Aktivnost>> getAktivnostiById (@Path("id") int id);

    @POST("Aktivnosti/add")
    Call<Void> addAktivnost(@Body Aktivnost novaAktivnost);

    @Multipart
    @POST("Slike/add/{id_ljubimca}")
    Call<Void> addSlika(@Path("id_ljubimca") int id_ljubimca, @Part MultipartBody.Part image);

    @GET("Slike/{id_ljubimca}")
    Call<HttpRequestResponseList<SlikaModel>> getSlikeById(@Path("id_ljubimca") int id_ljubimca);

    @DELETE("Slike/delete/{id}")
    Call<Void> deleteSlika (@Path("id") int id);

    @PUT ("KucniLjubimci/{id}/udomljen")
    Call<Void> adoptAnimalByAdmin (@Path("id") int id);

    @GET("Statistika")
    Call<HttpRequestResponse<StatistikaModel>> getStatistika();

    @GET("Token/{email}")
    Call<String> getToken(@Path("email") String email);

    @GET("Meetings")
    Call<HttpRequestResponseList<Meeting>> getMeetings();

    @POST("Meetings/add")
    Call<Void> addMeeting(@Body NewMeeting newMeeting);

    @DELETE("Meetings/delete/idMeeting/{idMeeting}")
    Call<Void> deleteMeeting (@Path("idMeeting") int idMeeting);

    @POST("Meetings/edit/{idMeeting}/{idKorisnik}/{type}")
    Call<Void> editMeeting(@Path("idMeeting") int idMeeting, @Path("idKorisnik") int idKorisnik, @Path("type") int type);
}