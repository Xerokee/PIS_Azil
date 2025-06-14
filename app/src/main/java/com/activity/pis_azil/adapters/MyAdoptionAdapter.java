package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.SendMail;
// import com.activity.pis_azil.activities.AdoptionStatusActivity;
import com.activity.pis_azil.activities.DetailedActivity;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.RejectAdoptionModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.network.DataRefreshListener;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.activity.pis_azil.network.DataRefreshListener;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptionAdapter extends RecyclerView.Adapter<MyAdoptionAdapter.ViewHolder> {

    private static final String TAG = "MyAdoptionAdapter";
    public final DataRefreshListener dataRefreshListener;
    Context context;
    List<MyAdoptionModel> cartModelList;
    // List<AnimalModel> animalModelList;
    ApiService apiService;

    public MyAdoptionAdapter(Context context, List<MyAdoptionModel> cartModelList, DataRefreshListener listener) {
        this.context = context;
        this.cartModelList = cartModelList;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.dataRefreshListener = listener;

        fetchAdoptionData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_animal_item2, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        // Provjera je li trenutni korisnik admin
        boolean isAdmin = checkIfUserIsAdmin();

        /*
        holder.adoptButton.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                MyAdoptionModel selectedAnimal = cartModelList.get(adapterPosition);
                if (!selectedAnimal.isUdomljen()) {
                    if (isAdmin) {
                        showAdoptionDialog(selectedAnimal, adapterPosition); // Ovdje koristimo novu funkciju
                    } else {
                        adoptAnimal(selectedAnimal, String.valueOf(cartModel.getIdKorisnika()), "Admin");
                    }
                } else {
                    Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (cartModel.getIdKorisnika() != 0) {
            holder.adoptButton.setVisibility(View.GONE);
        } else {
            holder.adoptButton.setVisibility(View.VISIBLE);
            holder.adoptButton.setEnabled(true);
            holder.adoptButton.setText("Udomi");
            holder.adoptButton.setBackgroundColor(Color.GREEN);
        }
        */

        if (cartModel.getIdKorisnika() == 0) {
            holder.approveButton.setEnabled(false);
            holder.approveButton.setBackgroundColor(Color.GRAY);

            holder.rejectButton.setEnabled(false);
            holder.rejectButton.setBackgroundColor(Color.GRAY);
        } else {
            holder.approveButton.setEnabled(true);
            holder.approveButton.setBackgroundColor(Color.parseColor("#03DAC5"));

            holder.rejectButton.setEnabled(true);
            holder.rejectButton.setBackgroundColor(Color.parseColor("#FF0000"));
        }

        holder.approveButton.setOnClickListener(v -> {
            approveAdoption(cartModel.getIdLjubimca(), position);
        });

        holder.rejectButton.setOnClickListener(v -> {
            rejectAdoption(position);
        });

        if (cartModel.getImgUrl() != null && !cartModel.getImgUrl().isEmpty()) {
            Glide.with(context).load(cartModel.getImgUrl()).into(holder.imgUrl);
        } else {
            holder.imgUrl.setImageResource(R.drawable.profile);
        }

        holder.itemView.setBackgroundColor(Color.WHITE);
        /*
        if (!cartModel.isStatusUdomljavanja()) {
            holder.itemView.setBackgroundColor(Color.RED); // Crvena boja za odbijeno udomljavanje
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Bijela boja za prihvaćeno udomljavanje
        }
        */
        holder.name.setText(" " + cartModel.getImeLjubimca());

        /*
        getEmailById(cartModel.getIdKorisnika(), email -> {
            if (email != null && !email.isEmpty()) {
                holder.requester.setText(email); // Prikaz emaila u TextView
            } else {
                holder.requester.setText("Nema dostupnog emaila");
            }
        });
        */
        holder.type.setText("  Tip: " + cartModel.getTipLjubimca());
        // holder.date.setText(cartModel.getDatum());
        // holder.time.setText(cartModel.getVrijeme());
        // holder.state.setText(cartModel.isStanjeZivotinje() ? "Dobro" : "Loše");

        /*
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
        */

        Log.d(TAG, "Binding view holder for position: " + position + ", model: " + cartModel.toString());

        if (!isAdmin) {
            // holder.deleteItem.setVisibility(View.GONE);
            // holder.updateItem.setVisibility(View.GONE);
            // holder.adoptButton.setVisibility(View.GONE);
            holder.approveButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
        } else {
            // holder.deleteItem.setOnClickListener(v -> showDeleteConfirmationDialog(position));
            // holder.updateItem.setOnClickListener(v -> showUpdateDialog(position));
            holder.approveButton.setVisibility(View.VISIBLE);

            if (cartModel.isUdomljen()) {
                // holder.adoptButton.setEnabled(false);
                // holder.adoptButton.setText("Udomljeno");
                // holder.adoptButton.setBackgroundColor(Color.GRAY);
            } else {
                // holder.adoptButton.setEnabled(true);
                // holder.adoptButton.setText("Udomi");
                // holder.adoptButton.setBackgroundColor(Color.GREEN);
                /*
                holder.adoptButton.setOnClickListener(view -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        MyAdoptionModel selectedAnimal = cartModelList.get(adapterPosition);
                        if (!selectedAnimal.isUdomljen()) {
                            if (isAdmin) {
                                showAdoptionDialogWithAllUsers(selectedAnimal, adapterPosition);
                            } else {
                                adoptAnimal(selectedAnimal, String.valueOf(cartModel.getIdKorisnika()), "Admin");
                            }
                        } else {
                            Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                */
            }
        }
    }

    private boolean checkIfUserIsAdmin() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        if (userJson != null) {
            Gson gson = new Gson();
            UserModel currentUser = gson.fromJson(userJson, UserModel.class);
            Log.d(TAG, "Provjera je li korisnik admin: " + currentUser.isAdmin());
            return currentUser != null && currentUser.isAdmin();
        }
        return false;
    }

    private void fetchAdoptionData() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        final UserModel currentUser;

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);

            if (currentUser == null || currentUser.getEmail() == null) {
                Log.e(TAG, "Korisnički objekt ili email je null!");
                Toast.makeText(context, "Korisnički podaci nisu dostupni", Toast.LENGTH_SHORT).show();
                return; // Exit the function to avoid further issues
            }
        } else {
            Log.e(TAG, "Korisnički JSON podaci nisu pronađeni.");
            return;
        }

        if (currentUser.isAdmin()) {
            apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
                @Override
                public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                    if (response.isSuccessful()) {
                        cartModelList = convertToMyAdoptionModel(response.body().stream().filter(item -> !item.isUdomljen()).collect(Collectors.toList()));
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                    Toast.makeText(context, "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
                @Override
                public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                    if (response.isSuccessful()) {
                        List<UpdateDnevnikModel> allAdoptions = response.body();
                        List<MyAdoptionModel> userAdoptions = new ArrayList<>();

                        for (UpdateDnevnikModel adoption : allAdoptions) {
                            if (adoption.getId_korisnika() == currentUser.getIdKorisnika() && !adoption.isUdomljen()) {
                                userAdoptions.add(convertToMyAdoptionModel(adoption));
                                // sendAdoptionStatusNotification(context, currentUser.getIdKorisnika(), adoption.getIme_ljubimca());
                            }
                        }
                        cartModelList = userAdoptions;
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                    Toast.makeText(context, "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private List<MyAdoptionModel> convertToMyAdoptionModel(List<UpdateDnevnikModel> dnevnikModels) {
        List<MyAdoptionModel> myAdoptionModels = new ArrayList<>();
        for (UpdateDnevnikModel dnevnikModel : dnevnikModels) {
            MyAdoptionModel myAdoptionModel = new MyAdoptionModel();
            myAdoptionModel.setIdLjubimca(dnevnikModel.getId_ljubimca());
            myAdoptionModel.setImeLjubimca(dnevnikModel.getIme_ljubimca());
            myAdoptionModel.setTipLjubimca(dnevnikModel.getTip_ljubimca());
            myAdoptionModel.setOpisLjubimca("");
            myAdoptionModel.setDatum(dnevnikModel.getDatum());
            myAdoptionModel.setVrijeme(dnevnikModel.getVrijeme());
            myAdoptionModel.setImgUrl(dnevnikModel.getImgUrl());
            myAdoptionModel.setUdomljen(dnevnikModel.isUdomljen());
            myAdoptionModel.setStanjeZivotinje(dnevnikModel.isStanje_zivotinje());
            myAdoptionModel.setIdKorisnika(dnevnikModel.getId_korisnika());
            myAdoptionModel.setStatusUdomljavanja(dnevnikModel.isStatus_udomljavanja());
            myAdoptionModels.add(myAdoptionModel);
        }
        return myAdoptionModels;
    }

    private MyAdoptionModel convertToMyAdoptionModel(UpdateDnevnikModel dnevnikModel) {
        MyAdoptionModel myAdoptionModel = new MyAdoptionModel();
        myAdoptionModel.setIdLjubimca(dnevnikModel.getId_ljubimca());
        myAdoptionModel.setImeLjubimca(dnevnikModel.getIme_ljubimca());
        myAdoptionModel.setTipLjubimca(dnevnikModel.getTip_ljubimca());
        myAdoptionModel.setOpisLjubimca("");
        myAdoptionModel.setDatum(dnevnikModel.getDatum());
        myAdoptionModel.setVrijeme(dnevnikModel.getVrijeme());
        myAdoptionModel.setImgUrl(dnevnikModel.getImgUrl());
        myAdoptionModel.setUdomljen(dnevnikModel.isUdomljen());
        myAdoptionModel.setStanjeZivotinje(dnevnikModel.isStanje_zivotinje());
        myAdoptionModel.setIdKorisnika(dnevnikModel.getId_korisnika());
        myAdoptionModel.setStatusUdomljavanja(dnevnikModel.isStatus_udomljavanja());
        return myAdoptionModel;
    }

    private void approveAdoption(int animalId, int position) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        if (animalId == 0) {
            Toast.makeText(context, "Animal ID is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartModel.isUdomljen()) {
            Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateDnevnikModel model = new UpdateDnevnikModel();
        model.setId_korisnika(cartModel.getIdKorisnika());
        model.setUdomljen(true);
        model.setStatus_udomljavanja(false);
        model.setIme_ljubimca(cartModel.getImeLjubimca());
        model.setTip_ljubimca(cartModel.getTipLjubimca());
        model.setDatum(cartModel.getDatum());
        model.setVrijeme(cartModel.getVrijeme());
        model.setImgUrl(cartModel.getImgUrl());
        model.setStanje_zivotinje(cartModel.isStanjeZivotinje());

        apiService.updateAdoption(1, animalId, model).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartModelList.get(position).setUdomljen(true);
                    notifyDataSetChanged();
                    dataRefreshListener.refreshData();
                    Toast.makeText(context, "Udomljavanje odobreno!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Log.d(TAG, "Animal ID: " + animalId);
                        Log.d(TAG, "RequestAnimalId: " + 1); // Uvijek postavljen na 1
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error: " + errorBody);
                        Toast.makeText(context, "Greška pri odobravanju: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(context, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectAdoption(int position) {
        MyAdoptionModel cartModel = cartModelList.get(position);
        cartModel.setStatusUdomljavanja(false);
        cartModel.setZahtjevUdomljavanja(false);
        notifyItemChanged(position);

        UpdateDnevnikModel dnevnik = new UpdateDnevnikModel();
        dnevnik.setId_korisnika(cartModel.getIdKorisnika());
        dnevnik.setUdomljen(cartModel.isUdomljen());
        dnevnik.setDatum(cartModel.getDatum());
        dnevnik.setImgUrl(cartModel.getImgUrl());
        dnevnik.setVrijeme(cartModel.getVrijeme());
        dnevnik.setStanje_zivotinje(cartModel.isStanjeZivotinje());
        dnevnik.setTip_ljubimca(cartModel.getTipLjubimca());
        dnevnik.setIme_ljubimca(cartModel.getImeLjubimca());
        dnevnik.setStatus_udomljavanja(false);
        dnevnik.setZahtjev_udomljen(false);

        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                for (AnimalModel animal : response.body()) {
                    if (animal.getImeLjubimca().equals(cartModel.getImeLjubimca())) {
                        AnimalModel updatedAnimal = new AnimalModel(
                                animal.getIdLjubimca(), 15, animal.getImeLjubimca(),
                                cartModel.getTipLjubimca(), dnevnik.getOpisLjubimca(),
                                false, false, dnevnik.getDatum(), dnevnik.getVrijeme(),
                                dnevnik.getImgUrl(), null, false
                        );

                        apiService.rejectAnimal(updatedAnimal.getIdLjubimca()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!response.isSuccessful()) return;

                                RejectAdoptionModel rejectModel = new RejectAdoptionModel();
                                rejectModel.setIdKorisnika(cartModel.getIdKorisnika());
                                rejectModel.setImeLjubimca(cartModel.getImeLjubimca());
                                rejectModel.setIdLjubimca(updatedAnimal.getIdLjubimca());
                                rejectModel.setZahtjevUdomljen(false);

                                apiService.createOdbijenaZivotinja(rejectModel).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        apiService.deleteAdoption(1, updatedAnimal.getIdLjubimca()).enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                if (response.isSuccessful()) {
                                                    cartModelList.remove(position);
                                                    notifyDataSetChanged();
                                                    Toast.makeText(context, "Udomljavanje odbijeno!", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Toast.makeText(context, "Greška prilikom brisanja!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(context, "Greška prilikom spremanja odbijenice!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e(TAG, "Greška prilikom odbijanja udomljavanja: ", t);
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Log.e(TAG, "Neuspješan dohvat životinja", t);
            }
        });
    }

    /*
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

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Potvrda brisanja");
        builder.setMessage("Jeste li sigurni da želite izbrisati ovu stavku iz liste?");
        builder.setPositiveButton("Da", (dialog, which) -> deleteItem(position));
        builder.setNegativeButton("Ne", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteItem(int position) {
        MyAdoptionModel cartModel = cartModelList.get(position);
        apiService.deleteAdoption(1, cartModelList.get(position).getIdLjubimca()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartModel.setZahtjevUdomljavanja(false);
                    Log.d(TAG, "Animal deleted successfully, position: " + position);
                    cartModelList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Lista izbrisana", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Server-side error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    private void updateAnimalDocument(int position, String updatedName, String updatedType, String updatedDate, String updatedTime, String updatedImgUrl, boolean stanjeZivotinje) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        Log.d(TAG, "Updating animal at position: " + position + " with data: " + updatedName + ", " + updatedType + ", " + updatedDate + ", " + updatedImgUrl + ", " + stanjeZivotinje);

        UpdateDnevnikModel model = new UpdateDnevnikModel();

        model.setId_korisnika(cartModel.getIdKorisnika());
        model.setUdomljen(cartModel.isUdomljen());
        // model.setId_ljubimca(cartModel.getIdLjubimca());
        model.setDatum(updatedDate);
        model.setImgUrl(updatedImgUrl);
        model.setVrijeme(updatedTime);
        model.setStanje_zivotinje(stanjeZivotinje);
        model.setStatus_udomljavanja(true);
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
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Server-side error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    private void showAdoptionDialog(MyAdoptionModel selectedAnimal, int position) {
        if (selectedAnimal.getIdKorisnika() != 0) {
            getEmailById(selectedAnimal.getIdKorisnika(), email -> {
                if (email != null && !email.isEmpty()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Potvrdi udomljavanje")
                            .setMessage("Želite li udomiti životinju za korisnika: " + email + "?")
                            .setPositiveButton("Udomi", (dialog, which) -> {
                                adoptAnimal(selectedAnimal, String.valueOf(selectedAnimal.getIdKorisnika()), email);
                            })
                            .setNegativeButton("Odustani", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Toast.makeText(context, "Korisnik koji je podnio zahtjev nije pronađen.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            showAdoptionDialogWithAllUsers(selectedAnimal, position);
        }
    }

    private void showAdoptionDialogWithAllUsers(MyAdoptionModel selectedAnimal, int position) {
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
                        return;
                    }

                    CharSequence[] adoptersArray = adopterNames.toArray(new CharSequence[0]);
                    new AlertDialog.Builder(context)
                            .setTitle("Odaberite udomitelja")
                            .setItems(adoptersArray, (dialog, which) -> {
                                String selectedUserId = adopterIds.get(which);
                                Log.d(TAG, "Odabrani udomitelj ID: " + selectedUserId + ", Ime: " + adopterNames.get(which));
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

    private void sendAdoptionStatusNotification(Context context, int idLjubimca, String animalName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "adoption_status_channel";
        String channelName = "Adoption Status Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, AdoptionStatusActivity.class);
        intent.putExtra("idLjubimca", idLjubimca);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.paw)  // Ikonica za obavijest
                .setContentTitle("Unesite status za udomljavanje")
                .setContentText("Potvrdite status za udomljavanje životinje " + animalName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)  // Klik na obavijest otvara aktivnost
                .setAutoCancel(true);  // Obavijest će se automatski ukloniti kada se klikne

        notificationManager.notify(animalName.hashCode(), builder.build());
    }


    private void adoptAnimal(MyAdoptionModel selectedAnimal, String adopterId, String adopterName) {
        if (adopterId == null || adopterId.isEmpty()) {
            Log.e(TAG, "Adopter ID nije postavljen. Ne mogu nastaviti s udomljavanjem.");
            Toast.makeText(context, "Adopter nije pronađen", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateDnevnikModel model = new UpdateDnevnikModel();

        model.setId_korisnika(Integer.parseInt(adopterId));
        model.setUdomljen(true);
        // model.setId_ljubimca(selectedAnimal.getIdLjubimca());

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(now);
        model.setDatum(currentDate);

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
                    // sendAdoptionStatusNotification(context, selectedAnimal.getIdLjubimca(), selectedAnimal.getImeLjubimca());
                    Toast.makeText(context, "Životinja je udomljena za korisnika " + adopterName, Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();

                    Toast.makeText(context, "Životinja je udomljena za korisnika " + adopterName, Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    getEmailById(Integer.parseInt(adopterId), email -> {
                        if (email != null) {
                            String subject = "Životinja je udomljena!";
                            String body = "Poštovani, " + adopterName + " je udomio/udomila životinju " + selectedAnimal.getImeLjubimca() + "!";

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        SendMail.sendEmail("margeta.matija@gmail.com", subject, body);
                                    } catch (GeneralSecurityException | IOException | MessagingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    System.out.println("send mail...");
                                }
                            }).start();
                        }
                    });
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Server-side error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        if (userId == 0) {
            Log.e(TAG, "Adopter ID je 0, nema korisnika za dohvatiti.");
            listener.onEmailFetched(null);
            return;
        }

        apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body().getResult();
                    if (user != null && user.getEmail() != null) {
                        // listener.onEmailFetched(user.getEmail());
                    } else {
                        Log.e(TAG, "Korisnik ili email je null.");
                        listener.onEmailFetched(null);
                    }
                } else {
                    Log.e(TAG, "Neuspješno dohvaćanje korisnika. Odgovor: " + response.message());
                    listener.onEmailFetched(null);
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Log.e(TAG, "Greška pri dohvaćanju korisnika: ", t);
                listener.onEmailFetched(null);
            }
        });
    }

    private void updateAnimal(AnimalModel updatedAnimal) {
        Log.d(TAG, "Usli u funkciju update animal" + updatedAnimal.getIdLjubimca());
        apiService.rejectAnimal(updatedAnimal.getIdLjubimca()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Životinja uspješno ažurirana.");
                    updatedAnimal.setZahtjevUdomljavanja(false);
                    // Ažuriranje UI ili povratak na prethodnu aktivnost
                } else {
                    Log.d(TAG, "PRIMJER");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "API poziv nije uspio: ", t);
            }
        });
    }

    interface OnEmailFetchedListener {
        void onEmailFetched(String email);
    }

    interface OnAnimalNameFetchedListener {
        void onAnimalNameFetched(String animalName);
    }

    */

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public void updateData(List<MyAdoptionModel> newAnimalsList) {
        this.cartModelList = newAnimalsList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, type, date, time, state, requester;
        ImageView imgUrl;
        // ImageView deleteItem, updateItem;
        // Button adoptButton;
        Button approveButton;
        Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            type = itemView.findViewById(R.id.product_type);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            state = itemView.findViewById(R.id.product_state);
            requester = itemView.findViewById(R.id.user_requesting);
            imgUrl = itemView.findViewById(R.id.img_url);
            // deleteItem = itemView.findViewById(R.id.delete);
            // updateItem = itemView.findViewById(R.id.update);
            // adoptButton = itemView.findViewById(R.id.adoptButton);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}