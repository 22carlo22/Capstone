package com.example.myapplication.ui.reservation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentReservationBinding;
import com.example.myapplication.databinding.FragmentReservationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReservationFragment extends Fragment {

    private FragmentReservationBinding binding;

    public ReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Extract the views from this fragment and display it
        binding = FragmentReservationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //listen to the inputs of the bottom navigation
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, new MyReservationFragment()).commit();
        binding.bottomnavReservation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fr = null;

                //Set the fragment accordingly
                int id = item.getItemId();
                if(id == R.id.item_myR) fr = new MyReservationFragment();
                else if(id == R.id.item_addR) fr = new NewReservationFragment();

                getFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, fr).commit();
                return true;
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}