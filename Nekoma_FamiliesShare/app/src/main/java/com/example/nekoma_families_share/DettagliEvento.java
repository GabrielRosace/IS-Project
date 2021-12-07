package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DettagliEvento extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento);

        Intent i = getIntent();

        String extraData = i.getStringExtra("evento");


        String[] parsed = extraData.split("/");
        System.out.println(parsed);


        ImageView img = (ImageView) findViewById(R.id.eventImage);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        TextView endDate = (TextView) findViewById(R.id.enddate);
        TextView desc = (TextView) findViewById(R.id.description);
        TextView nPart = (TextView) findViewById(R.id.nPart);

        eventName.setText(parsed[0]);
        if(parsed[2].equals("manca")){
            img.setImageDrawable(getDrawable(R.drawable.persone));
        }

        nPart.setText(nPart.getText() + parsed[3]);

        desc.setText(parsed.length<=4?"Manca":parsed[4]);

//        TODO enddate e photo
    }
}