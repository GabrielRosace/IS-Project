package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;

public class Homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_archivio);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile = new Intent(Homepage.this,Profile.class);
                startActivity(profile);
            }
        });


        String id_group = Utilities.getPrefs(this).getString("group","");
        String user_id;
        String userToken = Utilities.getToken(Homepage.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try{
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");

            // permette di vedere se l'utente è il capogruppo

            Utilities.httpRequest(this, Request.Method.GET, "/groups?searchBy=ids&ids=" + id_group, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONArray tmp = new JSONArray(response);
                        if(new JSONObject(tmp.getString(0)).getString("owner_id").equals(user_id)){
                            ImageView img = (ImageView) findViewById(R.id.tag_etichette);
                            img.setVisibility(View.VISIBLE);
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent etichette = new Intent(Homepage.this,Etichette.class);
                                    startActivity(etichette);
                                }
                            });
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            },new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Homepage.this, error.toString(), Toast.LENGTH_LONG).show();
                    // System.err.println(error.getMessage());
                }
            },new HashMap<>());
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    protected String getToken(){
        SharedPreferences prefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        return  prefs.getString("token","");
    }

    // tutti i seguenti metodi sono necessari per spostarsi attraverso le interfacce

    public void getBambini(View v){
        Intent bambino = new Intent(Homepage.this,ListaBambiniAmici.class);
        startActivity(bambino);
    }

    public void getGroup(View v){
        Intent group = new Intent(Homepage.this,SceltaDelGruppo.class);
        startActivity(group);
    }



    public void getActivities(View v){
        Intent activities = new Intent(Homepage.this, VisualizzazioneEventi.class);
        startActivity(activities);
    }

    public void getEvent(View v){
        Intent event = new Intent(Homepage.this,YourEvent.class);
        startActivity(event);
    }

    public void getCreateEvent(View v){
        Intent event = new Intent(Homepage.this,Creazione_evento.class);
        startActivity(event);
    }

    public void getService(View v){
        // Toast.makeText(Homepage.this, "COMING SOON", Toast.LENGTH_LONG).show();
        Intent service = new Intent(Homepage.this,YourService.class);
        startActivity(service);
    }

}