package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myapplication.MainActivity;
import com.example.myapplication.databinding.ActivityLoginBinding;
import com.example.myapplication.ui.server.Server;
import com.toxicbakery.bcrypt.Bcrypt;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class LoginActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityLoginBinding binding;
    MongoCollection<Document> col;

    public static User user;
    public static ObjectId user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Log In");
        Realm.init(this);
        App app = new App(new AppConfiguration.Builder("triggers-rkctpfc").build());
        app.loginAsync(Credentials.anonymous(), new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                user = app.currentUser();
                Server.user = user;
                Server.readSkills();
            }
        });

        binding.refreshLayoutLogin.setEnabled(false);
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.user.getText().toString();
                String password = binding.password.getText().toString();
                col = Server.getCol("users");

                binding.refreshLayoutLogin.setEnabled(true);
                binding.refreshLayoutLogin.setRefreshing(true);
                col.findOne(new Document().append("username", username)).getAsync(result -> {
                    if(result.get() == null || !Bcrypt.INSTANCE.verify(password, result.get().getString("password").getBytes(StandardCharsets.UTF_8))){
                        Toast.makeText(getApplicationContext(),"Incorrect username or password",Toast.LENGTH_LONG).show();
                        binding.loginStatus.setText("Incorrect username or password!");
                        binding.refreshLayoutLogin.setRefreshing(false);
                        binding.refreshLayoutLogin.setEnabled(false);
                    }
                    else{
                        user_id = result.get().getObjectId("_id");
                        Server.user_id = user_id;
                        Server.username = username;
                        Server.admin = result.get().getBoolean("admin");

                       Server.readAll();

                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.refreshLayoutLogin.setRefreshing(false);
                                binding.refreshLayoutLogin.setEnabled(false);

                                binding.user.getText().clear();
                                binding.password.getText().clear();
                                binding.user.clearFocus();
                                binding.password.clearFocus();
                                Toast.makeText(LoginActivity.this,"Login successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }, 2000);
                    }
                });

            }
        });

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.user.getText().clear();
                binding.password.getText().clear();
                binding.user.clearFocus();
                binding.password.clearFocus();
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}