package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.EmailService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.SendMail;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.network.DataRefreshListener;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserRoleModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.bumptech.glide.Glide;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptionAdapter extends RecyclerView.Adapter<MyAdoptionAdapter.ViewHolder> {

    private static final String TAG = "MyAdoptionAdapter";
    private DataRefreshListener dataRefreshListener;
    Context context;
    List<MyAdoptionModel> cartModelList;
    ApiService apiService;

    public MyAdoptionAdapter(Context context, List<MyAdoptionModel> cartModelList, DataRefreshListener listener) {
        this.context = context;
        this.cartModelList = cartModelList;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.dataRefreshListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_animal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        if (cartModel.getImgUrl() != null && !cartModel.getImgUrl().isEmpty()) {
            Glide.with(context).load(cartModel.getImgUrl()).into(holder.imgUrl);
        } else {
            holder.imgUrl.setImageResource(R.drawable.profile);
        }

        holder.name.setText(cartModel.getImeLjubimca());
        holder.type.setText(cartModel.getTipLjubimca());
        holder.date.setText(cartModel.getDatum());
        holder.time.setText(cartModel.getVrijeme());
        holder.state.setText(cartModel.isStanjeZivotinje() ? "Dobro" : "Loše");

        if (cartModel.getDatum() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date lastUpdateDate = sdf.parse(cartModel.getDatum());
                Calendar currentDate = Calendar.getInstance();
                Calendar lastUpdateCalendar = Calendar.getInstance();
                lastUpdateCalendar.setTime(lastUpdateDate);

                long diff = currentDate.getTimeInMillis() - lastUpdateCalendar.getTimeInMillis();
                long daysBetween = diff / (24 * 60 * 60 * 1000);

                if (daysBetween > 7) {
                    sendNotification(context, cartModel.getImeLjubimca());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Binding view holder for position: " + position + ", model: " + cartModel.toString());

        holder.deleteItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showDeleteConfirmationDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu brisati životinje.", Toast.LENGTH_SHORT).show()
        ));

        holder.updateItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showUpdateDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu ažurirati životinje.", Toast.LENGTH_SHORT).show()
        ));

        if (cartModel.isUdomljen()) {
            holder.adoptButton.setEnabled(false);
            holder.adoptButton.setText("Udomljeno");
        } else {
            holder.adoptButton.setEnabled(true);
            holder.adoptButton.setText("Udomi sad");
            holder.adoptButton.setOnClickListener(view -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    MyAdoptionModel selectedAnimal = cartModelList.get(adapterPosition);
                    if (!selectedAnimal.isUdomljen()) {
                        checkIfUserIsAdmin(selectedAnimal, adapterPosition);
                    } else {
                        Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendNotification(Context context, String animalName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "animal_status_channel";
        String channelName = "Animal Status Notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.paw)
                .setContentTitle("Nema zapisa o stanju životinje")
                .setContentText("Nema zapisa o stanju životinje " + animalName + " u posljednjih tjedan dana.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(animalName.hashCode(), builder.build());
    }

    private void checkIfUserIsAdminThenRun(Runnable onAdmin, Runnable onNonAdmin) {
        apiService.getUserRoleById(1).enqueue(new Callback<UserRoleModel>() { // Assume admin is checked with ID 1
            @Override
            public void onResponse(Call<UserRoleModel> call, Response<UserRoleModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRoleModel userRole = response.body();
                    if (userRole.isAdmin()) {
                        Log.d(TAG, "Korisnik je admin: " + userRole.toString());
                        onAdmin.run();
                    } else {
                        Log.d(TAG, "Korisnik nije admin: " + userRole.toString());
                        onNonAdmin.run();
                    }
                } else {
                    Log.d(TAG, "Response body is null or not successful");
                    onNonAdmin.run();
                }
            }

            @Override
            public void onFailure(Call<UserRoleModel> call, Throwable t) {
                Log.e(TAG, "Error checking if user is admin: ", t);
                Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Potvrda brisanja");
        builder.setMessage("Jeste li sigurni da želite izbrisati ovu stavku iz liste?");
        builder.setPositiveButton("Da", (dialog, which) -> deleteItem(position));
        builder.setNegativeButton("Ne", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteItem(int position) {
        apiService.deleteAdoption(1, cartModelList.get(position).getIdLjubimca()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Animal deleted successfully, position: " + position);
                    cartModelList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Lista izbrisana", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error deleting animal: " + response.message());
                    Toast.makeText(context, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deleting animal: ", t);
                Toast.makeText(context, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.update_dialog, null);
        builder.setView(view);
        EditText edtName = view.findViewById(R.id.edt_updated_name);
        EditText edtType = view.findViewById(R.id.edt_updated_type);
        EditText edtDate = view.findViewById(R.id.edt_updated_date);
        EditText edtTime = view.findViewById(R.id.edt_updated_time);
        EditText edtImgUrl = view.findViewById(R.id.edt_updated_img_url);
        EditText edtState = view.findViewById(R.id.edt_updated_state);

        MyAdoptionModel cartModel = cartModelList.get(position);
        edtName.setText(cartModel.getImeLjubimca());
        edtType.setText(cartModel.getTipLjubimca());
        edtDate.setText(cartModel.getDatum());
        edtTime.setText(cartModel.getVrijeme());
        edtImgUrl.setText(cartModel.getImgUrl());
        edtState.setText(cartModel.isStanjeZivotinje() ? "Dobro" : "Loše");

        builder.setPositiveButton("Ažuriraj", (dialog, which) -> {
            String updatedName = edtName.getText().toString().trim();
            String updatedType = edtType.getText().toString().trim();
            String updatedDate = edtDate.getText().toString().trim();
            String updatedTime = edtTime.getText().toString().trim();
            String updatedImgUrl = edtImgUrl.getText().toString().trim();
            String updatedState = edtState.getText().toString().trim();

            if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedType) || TextUtils.isEmpty(updatedDate) || TextUtils.isEmpty(updatedState)) {
                Toast.makeText(context, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show();
            } else {
                boolean stanjeZivotinje = updatedState.equalsIgnoreCase("Dobro");
                updateAnimalDocument(position, updatedName, updatedType, updatedDate, updatedTime, updatedImgUrl, stanjeZivotinje);
            }
        });

        builder.setNegativeButton("Odustani", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // MyAdoptionAdapter.java
    private void updateAnimalDocument(int position, String updatedName, String updatedType, String updatedDate, String updatedTime, String updatedImgUrl, boolean stanjeZivotinje) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        Log.d(TAG, "Updating animal at position: " + position + " with data: " + updatedName + ", " + updatedType + ", " + updatedDate + ", " + updatedImgUrl + ", " + stanjeZivotinje);

        UpdateDnevnikModel model = new UpdateDnevnikModel();

        model.setId_korisnika(cartModel.getIdKorisnika());
        model.setUdomljen(cartModel.isUdomljen());
        model.setId_ljubimca(cartModel.getIdLjubimca());
        model.setDatum(updatedDate);
        model.setImgUrl(updatedImgUrl);
        model.setVrijeme(updatedTime);
        model.setStanje_zivotinje(stanjeZivotinje);
        model.setTip_ljubimca(updatedType);
        model.setIme_ljubimca(updatedName);


        apiService.updateAdoption(1, cartModel.getIdLjubimca(), model).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Animal updated successfully, position: " + position);
                    cartModel.setImeLjubimca(updatedName);
                    cartModel.setTipLjubimca(updatedType);
                    cartModel.setDatum(updatedDate);
                    cartModel.setVrijeme(updatedTime);
                    cartModel.setImgUrl(updatedImgUrl);
                    cartModel.setStanjeZivotinje(stanjeZivotinje);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Lista ažurirana", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error updating animal: " + response.message() + ", code: " + response.code());
                    Log.e(TAG, "Response body: " + response.errorBody().toString());
                    Toast.makeText(context, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error updating animal: ", t);
                Toast.makeText(context, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsAdmin(final MyAdoptionModel selectedAnimal, final int adapterPosition) {
        Log.d(TAG, "Provjera ako je korisnik admin za udomljavanje");
        apiService.getUserById(1).enqueue(new Callback<UserByEmailResponseModel>() { // Pretpostavimo da je admin provjeren pomoću ID 1
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult().isAdmin()) {
                    Log.d(TAG, "Korisnik je admin, prikazivanje dijaloga za udomljavanje");
                    showAdoptionDialog(selectedAnimal, adapterPosition);
                } else {
                    Log.d(TAG, "Korisnik nije admin, ne može se udomiti životinja");
                    Toast.makeText(context, "Samo admini mogu udomljavati životinje.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Log.e(TAG, "Greška u provjeri ako je korisnik admin: ", t);
                Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdoptionDialog(MyAdoptionModel selectedAnimal, int position) {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> adopterNames = new ArrayList<>();
                    List<String> adopterIds = new ArrayList<>();
                    for (UserModel userModel : response.body()) {
                        if (!userModel.isAdmin()) {
                            adopterNames.add(userModel.getIme());
                            adopterIds.add(String.valueOf(userModel.getIdKorisnika()));
                        }
                    }
                    if (adopterNames.isEmpty()) {
                        Toast.makeText(context, "Trenutno nema dostupnih udomitelja.", Toast.LENGTH_SHORT).show();
                        return; // Exit if there are no adopters
                    }
                    CharSequence[] adoptersArray = adopterNames.toArray(new CharSequence[0]);
                    new AlertDialog.Builder(context)
                            .setTitle("Odaberite udomitelja")
                            .setItems(adoptersArray, (dialog, which) -> {
                                String selectedUserId = adopterIds.get(which);
                                Log.d(TAG, "Odabrani udomitelj ID: " + selectedUserId + ", Name: " + adopterNames.get(which));
                                adoptAnimal(selectedAnimal, selectedUserId, adopterNames.get(which));
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.e(TAG, "Ne može se dohvatiti lista udomitelja: ", t);
                Toast.makeText(context, "Ne može se dohvatiti lista udomitelja: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adoptAnimal(MyAdoptionModel selectedAnimal, String adopterId, String adopterName) {

        UpdateDnevnikModel model = new UpdateDnevnikModel();

        model.setId_korisnika(Integer.parseInt(adopterId));
        model.setUdomljen(true);
        model.setId_ljubimca(selectedAnimal.getIdLjubimca());

        Date now = new Date();
        // Format za datum
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(now);
        model.setDatum(currentDate);

        // Format za vrijeme
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeFormat.format(now);
        model.setVrijeme(currentTime);

        model.setIme_ljubimca(selectedAnimal.getImeLjubimca());
        model.setStanje_zivotinje(selectedAnimal.isStanjeZivotinje());
        model.setTip_ljubimca(selectedAnimal.getTipLjubimca());
        model.setImgUrl(selectedAnimal.getImgUrl());

        apiService.updateAdoption(1, selectedAnimal.getIdLjubimca(), model).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Animal adopted successfully: " + selectedAnimal.getIdLjubimca() + ", adopterId: " + adopterId + ", adopterName: " + adopterName);
                    Toast.makeText(context, "Životinja je udomljena za korisnika " + adopterName, Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    getEmailById(Integer.parseInt(adopterId), email -> {
                        if (email != null) {

                            String subject = "Životinja je posvojena!";
                            String body = "Poštovani, " + adopterName + " je posvojio/posvojila životinju " + selectedAnimal.getImeLjubimca() + "!";

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        SendMail.sendEmail("margeta.matija@gmail.com", subject, body);
                                    } catch (GeneralSecurityException e) {
                                        throw new RuntimeException(e);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (MessagingException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();
                        }
                    });
                } else {
                    Log.e(TAG, "Error adopting animal: " + response.message());
                    Toast.makeText(context, "Udomljavanje nije uspjelo: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error adopting animal: ", t);
                Toast.makeText(context, "Udomljavanje nije uspjelo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getEmailById(int userId, OnEmailFetchedListener listener) {
        apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String email = response.body().getResult().getEmail();
                    listener.onEmailFetched(email);
                } else {
                    Log.e(TAG, "Failed to fetch email: " + response.message());
                    listener.onEmailFetched(null);
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Log.e(TAG, "Error fetching email: ", t);
                listener.onEmailFetched(null);
            }
        });
    }

    interface OnEmailFetchedListener {
        void onEmailFetched(String email);
    }


    private void getAnimalNameById(int animalId, OnAnimalNameFetchedListener listener) {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String animalName = response.body().getImeLjubimca();
                    listener.onAnimalNameFetched(animalName);
                } else {
                    Log.e(TAG, "Failed to fetch animal name: " + response.message());
                    listener.onAnimalNameFetched(null);
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Log.e(TAG, "Error fetching animal name: ", t);
                listener.onAnimalNameFetched(null);
            }
        });
    }

    interface OnAnimalNameFetchedListener {
        void onAnimalNameFetched(String animalName);
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, type, date, time, state;
        ImageView imgUrl;
        ImageView deleteItem, updateItem;
        Button adoptButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            type = itemView.findViewById(R.id.product_type);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            state = itemView.findViewById(R.id.product_state);
            imgUrl = itemView.findViewById(R.id.img_url);
            deleteItem = itemView.findViewById(R.id.delete);
            updateItem = itemView.findViewById(R.id.update);
            adoptButton = itemView.findViewById(R.id.adoptButton);
        }
    }
}