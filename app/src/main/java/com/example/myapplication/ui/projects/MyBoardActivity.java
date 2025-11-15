package com.example.myapplication.ui.projects;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMyboardBinding;
import com.example.myapplication.ui.server.Server;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyBoardActivity extends AppCompatActivity {

    private ActivityMyboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Extract the views from this activity and display
        binding = ActivityMyboardBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        setTitle(Server.selected_project.name);
        //Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set the "lists" fragment as default, and display it
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, new TasksFragment()).commit();

        //Listen to the inputs of the bottom navigation
        binding.bottomnavboard.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fr = null;

                //Choose the fragment based on the user input
                int id = item.getItemId();
                if(id == R.id.item_tasks) fr = new TasksFragment();
                else if(id == R.id.item_analytic) fr = new AnalyticFragment();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, fr).commit();
                return true;
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}