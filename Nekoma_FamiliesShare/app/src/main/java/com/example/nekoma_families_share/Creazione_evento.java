package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Creazione_evento extends AppCompatActivity  {
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

    public void showModalLabel(View v){
        DialogFragment newFragment = new SelectorLabels();
        newFragment.show(getSupportFragmentManager(), "missiles");
    }

    public void onClickNextButton(View v){

    }

    private class SelectorLabels extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("PROVA")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

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