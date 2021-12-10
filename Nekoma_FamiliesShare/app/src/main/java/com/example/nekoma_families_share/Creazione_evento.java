package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Creazione_evento extends AppCompatActivity {
    private List<MyEtichette> etichette;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_evento);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar7);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String id_group = Utilities.getPrefs(this).getString("group", "");
        Utilities.httpRequest(this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    etichette = new ArrayList<>();
                    JSONArray tmp = new JSONArray(response);
                    System.out.println(tmp);
                    for(int i=0;i<tmp.length();++i) {
                        Creazione_evento.MyEtichette nuovo = new Creazione_evento.MyEtichette(new JSONObject(tmp.getString(i)).getString("name"), new JSONObject(tmp.getString(i)).getString("label_id"));
                        etichette.add(nuovo);
                    }
                    Spinner spinner = (Spinner) findViewById(R.id.spinnerLabelsEvent);
                    String[] stringLabel = new String[etichette.size()];
                    int i=0;
                    for (MyEtichette l:etichette) {
                        stringLabel[i] = l.getName();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Creazione_evento.this,android.R.layout.simple_spinner_item, stringLabel);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Creazione_evento.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },new HashMap<>());

    }

    public void onClickNextButton(View v){

    }

    private static class MyEtichette {
        private String name;
        private String id;

        MyEtichette(String name, String id){
            this.id = id;
            this.name = name;
        }

        private String getName(){
            return this.name;
        }

    }
}