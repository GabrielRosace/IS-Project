package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class DettagliServizzio extends AppCompatActivity {
    private TextView name;
    private TextView location;
    private TextView type;
    private TextView date;
    private TextView forType;
    private TextView forTypeTitle;
    private TextView rec;
    private TextView recType;
    private Button buttonS;
    private EditText desc;
    private ImageView img;
    private TextView lendTimeT;
    private TextView lendTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_servizzio);

        Toolbar t = (Toolbar) findViewById(R.id.toolbarService);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String groupId = Utilities.getGroupId(DettagliServizzio.this);
        String extras = getIntent().getStringExtra("servizio");
        String[] data  = extras.split("\\$");
        img = (ImageView) findViewById(R.id.imageSevice);
        name = (TextView) findViewById(R.id.textName);
        location = (TextView) findViewById(R.id.locationService);
        type = (TextView) findViewById(R.id.typeService);
        forType = (TextView) findViewById(R.id.editForType);
        forTypeTitle = (TextView) findViewById(R.id.editForTypeTitle);
        date = (TextView) findViewById(R.id.duration);
        rec = (TextView) findViewById(R.id.recurenceType);
        recType = (TextView) findViewById(R.id.recurrenceTypeText);
        buttonS = (Button) findViewById(R.id.buttonService);
        desc = (EditText) findViewById(R.id.desceMultiLineService);
        lendTimeT = (TextView) findViewById(R.id.lendTime);
        lendTime = (TextView) findViewById(R.id.lendTime2);
        lendTime.setVisibility(View.GONE);
        lendTimeT.setVisibility(View.GONE);
        new ImageDownloader(img).execute(data[10]);
        for(int i=0;i<data.length;i++){
            System.out.println("<--->"+data[i]);
        }
        name.setText(data[2]+" ");
        location.setText(data[4]+" ");
        desc.setText(data[3]+" ");

        if(data[5].equals("car")){
            type.setText("car");
            forType.setText(data[6]);
            forTypeTitle.setText("Posti auto disponibili:");
            lendTime.setVisibility(View.GONE);
            lendTimeT.setVisibility(View.GONE);
        }else if(data[5].equals("lend")){
            if(data[15].equals("true")){
                lendTime.setVisibility(View.VISIBLE);
                lendTimeT.setVisibility(View.VISIBLE);
                lendTime.setText(data[8]);
            }
            forType.setText(data[7]);
            type.setText("lend");
            forTypeTitle.setText("Ogetto in prestito:");

        }else if(data[5].equals("pickup")){
            forType.setText(data[9]);
            forTypeTitle.setText("luogo di ritrovo:");
            type.setText("pickup");
            lendTime.setVisibility(View.GONE);
            lendTimeT.setVisibility(View.GONE);
        }
        if(data[15].equals("true")){
            recType.setText(data[12]);
            date.setText(data[13]+data[14]);
        }else{
            rec.setVisibility(View.GONE);
            recType.setVisibility(View.GONE);
        }
        if(data[1].equals(Utilities.getUserID(DettagliServizzio.this))){
            buttonS.setText("Modifica");
            desc.setEnabled(true);
            buttonS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,String> map = new HashMap();
                    map.put("name", name.getText().toString());
                    map.put("description", desc.getText().toString());
                    map.put("location", location.getText().toString());
                    map.put("pattern", type.getText().toString());
                    if(data[5].equals("car")){
                        map.put( "car_space",data[6] );
                    }else if(data[5].equals("lend")){
                        map.put( "lend_obj", data[7]);
                        map.put( "lend_time",data[8]);
                    }else if(data[5].equals("pickup")){
                        map.put( "pickuplocation", data[9]);
                    }
                    map.put("img",data[10]);
                    Utilities.httpRequest(DettagliServizzio.this, Request.Method.PATCH,"/groups/"+groupId+"/service/"+data[0], response -> {
                        finish();
                    }, System.err::println, map);

                }
            });
        }else{
            buttonS.setText("Smetti di parecipare");
            desc.setEnabled(false);
            buttonS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,String> map = new HashMap();
                    map.put("activity_id", data[0]);
                    map.put("user_id",Utilities.getUserID(DettagliServizzio.this));
                    Utilities.httpRequest(DettagliServizzio.this, Request.Method.DELETE,"/groups/"+groupId+"/service/"+data[0]+"/partecipate", response -> {
                        finish();
                    }, System.err::println,map);
                }
            });

        }
    }
    private static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView holder;

        public ImageDownloader(ImageView holder) {
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlOfImage = strings[0];
            Bitmap logo = null;
            try {
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holder.setImageBitmap(bitmap);
        }
    }
}