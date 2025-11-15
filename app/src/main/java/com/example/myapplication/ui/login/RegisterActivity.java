package com.example.myapplication.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import com.example.myapplication.ui.server.Server;
import com.toxicbakery.bcrypt.Bcrypt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


import io.realm.Realm;

public class RegisterActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityRegisterBinding binding;

    private ArrayList<String> mySkills = new ArrayList<>();
    private boolean admin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Register");

        ArrayAdapter<String> adapter_skills = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Server.skills);
        binding.spinnerMySkill1.setAdapter(adapter_skills);

        mySkills.clear();
        admin = false;

        binding.checkBoxAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin = ((CompoundButton) v).isChecked();
                if(admin){
                    binding.linearLayoutMyskills.setVisibility(View.GONE);
                    binding.linearLayoutMySkillsRegister.setVisibility(View.GONE);
                    binding.linearLayoutMySkillsRegister.removeAllViews();
                    mySkills.clear();
                }
                else{
                    binding.linearLayoutMyskills.setVisibility(View.VISIBLE);
                    binding.linearLayoutMySkillsRegister.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.spinnerMySkill1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) return;

                mySkills.add(parent.getItemAtPosition(position).toString());
                View skill = getLayoutInflater().inflate(R.layout.skill_simple, null, false);

                ((TextView) skill.findViewById(R.id.textView_skillName)).setText(parent.getItemAtPosition(position).toString());
                ((ImageView) skill.findViewById(R.id.imageView_skillCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mySkills.remove(parent.getItemAtPosition(position).toString());
                        binding.linearLayoutMySkillsRegister.removeView(skill);
                    }
                });

                binding.linearLayoutMySkillsRegister.addView(skill);
                binding.spinnerMySkill1.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.enterRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = binding.userText.getText().toString();
                String password = binding.passwordText.getText().toString();
                ArrayList<String> mySkills_temp = new ArrayList<>(mySkills);
                Server.registerAccount(user, new String(Bcrypt.INSTANCE.hash(password, 10), StandardCharsets.UTF_8), mySkills_temp, admin);
                Toast.makeText(RegisterActivity.this, "Account Created",Toast.LENGTH_LONG).show();

                binding.checkBoxAdmin.setChecked(false);
                binding.userText.getText().clear();
                binding.userText.clearFocus();
                binding.passwordText.getText().clear();
                binding.passwordText.clearFocus();
                binding.linearLayoutMyskills.setVisibility(View.VISIBLE);
                binding.linearLayoutMySkillsRegister.setVisibility(View.VISIBLE);
                binding.linearLayoutMySkillsRegister.removeAllViews();
                mySkills.clear();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}