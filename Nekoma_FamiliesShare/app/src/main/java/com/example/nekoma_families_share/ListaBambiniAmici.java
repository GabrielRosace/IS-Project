package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ListaBambiniAmici extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bambini_amici);
    }

    RequestQueue childlist = Volley.newRequestQueue(this);
}