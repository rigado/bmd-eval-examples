package com.rigado.bmdeval.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
