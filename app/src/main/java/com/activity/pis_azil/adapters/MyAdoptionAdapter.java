/*
package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
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

import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptionAdapter extends RecyclerView.Adapter<MyAdoptionAdapter.ViewHolder> {

    Context context;
    List<MyAdoptionModel> cartModelList;
    ApiService apiService;

    public MyAdoptionAdapter(Context context, List<MyAdoptionModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
        this.apiService = ApiClient.getClient().create(ApiService.class);
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

        if (cartModel.getImg_url() != null && !cartModel.getImg_url().isEmpty()) {
            Glide.with(context).load(cartModel.getImg_url()).into(holder.imgUrl);
        } else {
            holder.imgUrl.setImageResource(R.drawable.profile);
        }

        holder.name.setText(cartModel.getAnimalName());
        holder.type.setText(cartModel.getAnimalType());
        holder.date.setText(cartModel.getCurrentDate());
        holder.time.setText(cartModel.getCurrentTime());

        holder.deleteItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showDeleteConfirmationDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu brisati životinje.", Toast.LENGTH_SHORT).show()
        ));

        holder.updateItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showUpdateDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu ažurirati životinje.", Toast.LENGTH_SHORT).show()
        ));

        if (cartModel.isAdopted()) {
            holder.adoptButton.setEnabled(false);
            holder.adoptButton.setText("Udomljeno");
        } else {
            holder.adoptButton.setEnabled(true);
            holder.adoptButton.setText("Udomi sad");
            holder.adoptButton.setOnClickListener(view -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    MyAdoptionModel selectedAnimal = cartModelList.get(adapterPosition);
                    if (!selectedAnimal.isAdopted()) {
                        checkIfUserIsAdmin(selectedAnimal, adapterPosition);
                    } else {
                        Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkIfUserIsAdminThenRun(Runnable onAdmin, Runnable onNonAdmin) {
        apiService.getUserById(1).enqueue(new Callback<UserModel>() { // Pretpostavimo da je admin provjeren pomoću ID 1
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAdmin()) {
                    onAdmin.run();
                } else {
                    onNonAdmin.run();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
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
        apiService.deleteAnimal(cartModelList.get(position).getAnimalId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartModelList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Lista izbrisana", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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

        MyAdoptionModel cartModel = cartModelList.get(position);
        edtName.setText(cartModel.getAnimalName());
        edtType.setText(cartModel.getAnimalType());
        edtDate.setText(cartModel.getCurrentDate());
        edtTime.setText(cartModel.getCurrentTime());
        edtImgUrl.setText(cartModel.getImg_url());

        builder.setPositiveButton("Ažuriraj", (dialog, which) -> {
            String updatedName = edtName.getText().toString().trim();
            String updatedType = edtType.getText().toString().trim();
            String updatedDate = edtDate.getText().toString().trim();
            String updatedTime = edtTime.getText().toString().trim();
            String updatedImgUrl = edtImgUrl.getText().toString().trim();

            if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedType) || TextUtils.isEmpty(updatedDate) || TextUtils.isEmpty(updatedTime)) {
                Toast.makeText(context, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show();
            } else {
                updateAnimalDocument(position, updatedName, updatedType, updatedDate, updatedTime, updatedImgUrl);
            }
        });

        builder.setNegativeButton("Odustani", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateAnimalDocument(int position, String updatedName, String updatedType, String updatedDate, String updatedTime, String updatedImgUrl) {
        MyAdoptionModel cartModel = cartModelList.get(position);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("animalName", updatedName);
        updateData.put("animalType", updatedType);
        updateData.put("currentDate", updatedDate);
        updateData.put("currentTime", updatedTime);
        updateData.put("img_url", updatedImgUrl);

        apiService.updateAnimal(cartModel.getAnimalId(), updateData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartModel.setAnimalName(updatedName);
                    cartModel.setAnimalType(updatedType);
                    cartModel.setCurrentDate(updatedDate);
                    cartModel.setCurrentTime(updatedTime);
                    cartModel.setImg_url(updatedImgUrl);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Lista ažurirana", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsAdmin(final MyAdoptionModel selectedAnimal, final int adapterPosition) {
        apiService.getUserById(1).enqueue(new Callback<UserModel>() { // Pretpostavimo da je admin provjeren pomoću ID 1
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAdmin()) {
                    showAdoptionDialog(selectedAnimal, adapterPosition);
                } else {
                    Toast.makeText(context, "Samo admini mogu udomljavati životinje.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
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
                                adoptAnimal(selectedAnimal.getAnimalId(), selectedUserId, adopterNames.get(which));
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(context, "Ne može se dohvatiti lista udomitelja: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adoptAnimal(String animalId, String adopterId, String adopterName) {
        Map<String, Object> adoptionUpdates = new HashMap<>();
        adoptionUpdates.put("adopted", true);
        adoptionUpdates.put("adopterId", adopterId);
        adoptionUpdates.put("adopterName", adopterName);

        apiService.updateAnimal(animalId, adoptionUpdates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Životinja je udomljena za korisnika " + adopterName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Udomljavanje nije uspjelo: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Udomljavanje nije uspjelo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, type, date, time;
        ImageView imgUrl;
        ImageView deleteItem, updateItem;
        Button adoptButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            type = itemView.findViewById(R.id.product_type);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            imgUrl = itemView.findViewById(R.id.img_url);
            deleteItem = itemView.findViewById(R.id.delete);
            updateItem = itemView.findViewById(R.id.update);
            adoptButton = itemView.findViewById(R.id.adoptButton);
        }
    }
}
*/