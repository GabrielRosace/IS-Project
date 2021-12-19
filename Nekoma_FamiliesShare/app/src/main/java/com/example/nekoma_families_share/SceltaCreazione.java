package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SceltaCreazione extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scelta_creazione);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_scelta);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void getEvento(View v){
        Intent event = new Intent(SceltaCreazione.this,Creazione_evento.class);
        startActivity(event);
    }
    public void getServizio(View v){
        Intent servizio = new Intent(SceltaCreazione.this,NuovoServizio.class);
        startActivity(servizio);
    }
    public void getEventoRicorrent(View v){
        /*Intent event = new Intent(SceltaCreazione.this,Creazione_evento.class);
        startActivity(event);*/
        Toast.makeText(SceltaCreazione.this, "COMING SOON", Toast.LENGTH_SHORT).show();
    }
}