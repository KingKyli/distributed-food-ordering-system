package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        // In WelcomeActivity.java
        btnGetStarted.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    ServerConnection.init("172.20.10.3", 5000);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });


    }
}
