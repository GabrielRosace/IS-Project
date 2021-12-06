package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }
    protected String getToken(){
        SharedPreferences prefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        return  prefs.getString("token","");
    }
    public void getBambini(View v){
        Intent bambino = new Intent(Homepage.this,ListaBambiniAmici.class);
        startActivity(bambino);
    }
    public void logout(View v){
        Intent logout = new Intent(Homepage.this,MainActivity.class);
        startActivity(logout);
    }

}