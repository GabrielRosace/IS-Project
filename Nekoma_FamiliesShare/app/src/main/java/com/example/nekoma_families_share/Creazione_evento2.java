package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class Creazione_evento2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_evento2);
        Intent i = getIntent();
    }
    private static class Evento {
        private String name;
        private String data;
        private String location;
        private String type;
        private List<String> labels;
        private String desc;
    }
}