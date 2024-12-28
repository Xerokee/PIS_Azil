package com.activity.pis_azil.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.activity.pis_azil.fragments.AnimalDetailFragment;
import com.activity.pis_azil.fragments.AnimalActivityFragment;
import com.activity.pis_azil.fragments.AnimalGalleryFragment;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UserModel;

public class ViewPagerAdapter2 extends FragmentStateAdapter {

    AnimalModel detailedAnimal;
    UserModel currentUser;
    IsBlockedAnimalModel animalModel;
    int animalId;

    public ViewPagerAdapter2(@NonNull FragmentActivity fragmentActivity, AnimalModel da, UserModel cu, int animalId){
        super(fragmentActivity);
        detailedAnimal = da;
        currentUser=cu;
        this.animalId = animalId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 1:
                return new AnimalGalleryFragment(detailedAnimal);
            case 2:
                return new AnimalActivityFragment(detailedAnimal, currentUser);
            default:
                return new AnimalDetailFragment(animalId, detailedAnimal, currentUser, animalModel);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
