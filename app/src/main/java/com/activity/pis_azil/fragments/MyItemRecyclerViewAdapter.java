package com.activity.pis_azil.fragments;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activity.pis_azil.fragments.placeholder.PlaceholderContent.PlaceholderItem;
import com.activity.pis_azil.databinding.FragmentRejectedAnimalsBinding;

import java.util.List;



import com.activity.pis_azil.models.RejectAdoptionModelRead;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<RejectAdoptionModelRead> mValues;

    public MyItemRecyclerViewAdapter(List<RejectAdoptionModelRead> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentRejectedAnimalsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RejectAdoptionModelRead currentItem = mValues.get(position);
        holder.mIdView.setText(currentItem.getId_korisnika().toString());
        holder.mContentView.setText(currentItem.getIme_ljubimca());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;

        public ViewHolder(FragmentRejectedAnimalsBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }
    }
}
