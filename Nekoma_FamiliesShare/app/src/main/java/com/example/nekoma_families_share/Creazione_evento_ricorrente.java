package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Creazione_evento_ricorrente extends AppCompatActivity {
    private List<Button> labelSelect;
    protected static List<Creazione_evento.MyEtichette> etichette;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_evento_ricorrente);
        labelSelect = new ArrayList<>();
        String id_group = Utilities.getPrefs(this).getString("group", "");
        Utilities.httpRequest(this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    etichette = new ArrayList<>();

                    JSONArray tmp = new JSONArray(response);
                    System.out.println(tmp);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.linearLabelsRic);
                    for(int i=0;i<tmp.length();++i) {
                        Creazione_evento.MyEtichette nuovo = new Creazione_evento.MyEtichette(new JSONObject(tmp.getString(i)).getString("name"), new JSONObject(tmp.getString(i)).getString("label_id"));
                        etichette.add(nuovo);
                        Button b = new Button(Creazione_evento_ricorrente.this);
                        labelSelect.add(b);
                        b.setText(nuovo.getName());
                        b.setBackgroundColor(Color.LTGRAY);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int buttonColor = ((ColorDrawable) b.getBackground()).getColor();
                                if(buttonColor == Color.LTGRAY) {
                                    b.setBackgroundColor(Color.GREEN);
                                }else
                                    b.setBackgroundColor(Color.LTGRAY);
                            }
                        });
                        ll.addView(b);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Creazione_evento_ricorrente.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },new HashMap<>());

        Button b = (Button) findViewById(R.id.nextButton);
        b.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i = new Intent(Creazione_evento_ricorrente.this, Creazione_date_evetno_ricorrente.class);
                 i.putExtra("name", ((EditText) (findViewById(R.id.inputEventNameRic))).getText().toString());
                 i.putExtra("location", ((EditText) (findViewById(R.id.inputEventLcationRic))).getText().toString());
                 i.putExtra("desc", ((EditText) (findViewById(R.id.inputDescEventRic))).getText().toString());
                 Iterator it = labelSelect.iterator();
                 String labels = "[";
                 boolean control = true;
                 while (it.hasNext()) {
                     Button b = (Button) it.next();
                     int buttonColor = ((ColorDrawable) b.getBackground()).getColor();
                     if (buttonColor == Color.GREEN) {
                         for (Creazione_evento.MyEtichette m : etichette) {
                             if (m.getName().equals(b.getText().toString())) {
                                 if (control) {
                                     labels = labels = labels + m.getId();
                                     control = false;
                                 } else
                                     labels = labels + "," + m.getId();
                             }
                         }
                     }
                 }

                 labels = labels + "]";
                 i.putExtra("lables", labels);
                 startActivity(i);
             }
        });
        //necessario per tornare alla schermata precedente
        Toolbar t = (Toolbar) findViewById(R.id.toolbar_eve_rec);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

