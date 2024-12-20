package com.activity.pis_azil.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.activity.pis_azil.fragments.GalerijaFragment;
import com.activity.pis_azil.fragments.OsnovniPodaciFragment;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UserModel;

public class ViewPagerAdapter extends FragmentStateAdapter {

    AnimalModel detailedAnimal;
    UserModel currentUser;
    IsBlockedAnimalModel animalModel;
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, AnimalModel da, UserModel cu, IsBlockedAnimalModel am){
        super(fragmentActivity);
        detailedAnimal = da;
        currentUser=cu;
        animalModel=am;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new OsnovniPodaciFragment(detailedAnimal, currentUser, animalModel);
            case 1:
                return new GalerijaFragment(detailedAnimal);
            default:
                return new OsnovniPodaciFragment(detailedAnimal, currentUser, animalModel);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
