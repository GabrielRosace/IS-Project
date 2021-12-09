package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;

import java.util.HashMap;


public class DettagliEvento extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento);


        Toolbar t = (Toolbar) findViewById(R.id.toolbar2);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent i = getIntent();

        String extraData = i.getStringExtra("evento");


        String[] parsed = extraData.split("/");


        ImageView img = (ImageView) findViewById(R.id.eventImage);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        TextView endDate = (TextView) findViewById(R.id.enddate);
        TextView desc = (TextView) findViewById(R.id.description);
        TextView nPart = (TextView) findViewById(R.id.nPart);


        eventName.setText(parsed[0]);
        if(parsed[2].equals("manca")){
            img.setImageDrawable(getDrawable(R.drawable.persone));
        }else{
            img.setImageDrawable(getDrawable(R.drawable.persone));
        }

        nPart.setText(nPart.getText() + parsed[3]);

        desc.setText(parsed[4]);
        endDate.setText(endDate.getText() + " "+ parsed[5]);

//        TODO enddate e photo e preferenze

        String group_id = Utilities.getPrefs(this).getString("group","");

        if(parsed.length>6){ // Se ci sono etichette
            String labels = parsed[6];
            System.out.println(labels);
        }

    }
}