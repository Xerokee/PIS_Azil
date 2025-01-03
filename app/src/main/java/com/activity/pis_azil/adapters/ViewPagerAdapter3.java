package com.activity.pis_azil.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.activity.pis_azil.fragments.AdoptedAnimalActivityFragment;
import com.activity.pis_azil.fragments.AdoptedAnimalDetailFragment;
import com.activity.pis_azil.fragments.AdoptedAnimalGalleryFragment;
import com.activity.pis_azil.fragments.AnimalActivityFragment;
import com.activity.pis_azil.fragments.AnimalDetailFragment;
import com.activity.pis_azil.fragments.AnimalGalleryFragment;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UserModel;

public class ViewPagerAdapter3 extends FragmentStateAdapter {

    AnimalModel detailedAnimal;
    UserModel currentUser;
    IsBlockedAnimalModel animalModel;
    String udomitelj;
    int animalId;

    public ViewPagerAdapter3(@NonNull FragmentActivity fragmentActivity, AnimalModel da, int animalId, String u){
        super(fragmentActivity);
        detailedAnimal = da;
        this.animalId = animalId;
        udomitelj = u;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 1:
                return new AdoptedAnimalGalleryFragment(detailedAnimal);
            case 2:
                return new AdoptedAnimalActivityFragment(animalId);
            default:
                return new AdoptedAnimalDetailFragment(animalId, detailedAnimal, udomitelj);

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
