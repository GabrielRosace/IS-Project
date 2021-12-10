package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Creazione_evento extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_evento);
    }

    public void onClickNextButton(View v){
        EditText name = (EditText) findViewById(R.id.inputEventName);
        EditText date = (EditText) findViewById(R.id.inputEventDate);
        EditText location = (EditText) findViewById(R.id.inputEventLcation);
        Intent i = new Intent(Creazione_evento.this,Creazione_evento2.class);
        i.putExtra("nameEvent",name.getText().toString());
        i.putExtra("dateEvent",date.getText().toString());
        i.putExtra("locationEvent",location.getText().toString());
        startActivity(i);
    }
}