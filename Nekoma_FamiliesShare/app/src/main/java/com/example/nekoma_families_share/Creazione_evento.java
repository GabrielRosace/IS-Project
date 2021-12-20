package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Creazione_evento extends AppCompatActivity  {
    protected static List<MyEtichette> etichette;
    private List<Button> labelSelect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_evento);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_eve_rec);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                        Button b = new Button(Creazione_evento.this);
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
                Toast.makeText(Creazione_evento.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },new HashMap<>());


    }

    public void showModalLabel(View v){

    }

    public void onClickCreaButton(View v) throws ParseException {
        String nome = ((EditText)findViewById(R.id.inputEventNameRic)).getText().toString();
        String luogo = ((EditText)findViewById(R.id.inputEventLcationRic)).getText().toString();
        String desc = ((EditText)findViewById(R.id.inputDescEventRic)).getText().toString();
        String dataInizio = ((EditText)findViewById(R.id.inputEventDate)).getText().toString();
        String dataFine = ((EditText)findViewById(R.id.inputEventEndEvent)).getText().toString();
        String inizio = ((EditText) findViewById(R.id.inputEventStartTime)).getText().toString();
        String fine = ((EditText) findViewById(R.id.inputEventEndTime)).getText().toString();
        if(nome.equals("") || luogo.equals("") || desc.equals("") || dataInizio.equals("") || inizio.equals("") ||dataFine.equals("") || fine.equals("")){
            Toast.makeText(Creazione_evento.this, "INSERIRE TUTTI I CAMPI", Toast.LENGTH_LONG).show();
        }else{
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            Date d = format.parse(dataInizio+" "+inizio);
            String s = format2.format(d);
            Date DInizio = format2.parse(s);

            d = format.parse(dataFine+" "+fine);
            s = format2.format(d);
            Date DFine = format2.parse(s);
            System.out.println(DInizio + " " +DFine);
            String DateEnd = format2.format(DFine);
            String DateStart = format2.format(DInizio);
            System.out.println(DateStart + " " +DateEnd);
            String labels = "[";
            Iterator i = labelSelect.iterator();
            boolean control = true;
            while(i.hasNext()){
                Button b = (Button) i.next();
                int buttonColor = ((ColorDrawable) b.getBackground()).getColor();
                if(buttonColor == Color.GREEN) {
                    for (MyEtichette m:etichette) {
                        if(m.getName().equals(b.getText().toString())){
                            if(control){
                                labels = labels = labels + m.getId();
                                control = false;
                            }else
                                labels = labels + "," + m.getId();
                        }
                    }
                }
            }
            labels = labels + "]";
            System.out.println("----->"+labels);

            HashMap<String,String> map = new HashMap<>();
            String id_group = Utilities.getPrefs(this).getString("group", "");
            map.put("group_id",id_group);
            map.put("creator_id", Utilities.getUserID(Creazione_evento.this));
            map.put("name", nome);
            map.put("color","#000000");
            map.put("description",desc);
            map.put("location", luogo);
            map.put("repetition","false");
            map.put("repetition_type", "");
            map.put("different_timeslots","false");
            map.put("labels",labels);
            map.put("status", "accepted");
            map.put("activity_id","");
            map.put("imgUrl","");

            Utilities.httpRequest(this, Request.Method.GET, "/groups?searchBy=ids&ids=" + id_group, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray user_response = new JSONArray((String) response);
                        map.put("group_name",new JSONObject(user_response.getString(0)).getString("name"));
                        System.out.println("----->"+user_response);
                    } catch (JSONException e) {
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
            String[] Hinizio = inizio.split(":");
            String[] Hfine = fine.split(":");
            HashMap<String,String> mapTime = new HashMap<>();
            mapTime.put("summary",nome);
            mapTime.put("location",luogo);
            mapTime.put("start",DateStart);
            mapTime.put("end",DateEnd);
            mapTime.put("cost","");
            mapTime.put("requiredChildren","2");
            mapTime.put("groupId",id_group);
            mapTime.put("startHour",Hinizio[0]);
            mapTime.put("link","");
            mapTime.put("requiredParents","2");
            mapTime.put("repetition","none");
            mapTime.put("activityColor","#000000");
            mapTime.put("children","[]");
            mapTime.put("externals","[]");
            mapTime.put("endHour",Hfine[0]);
            mapTime.put("category","");
            mapTime.put("status","ongoing");
            mapTime.put("parents","[]");
            Utilities.httpRequest(this, Request.Method.POST, "/groups/"+id_group+"/nekomaActivities", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject tmp = null;
                    try {
                        tmp = new JSONObject(response);
                        mapTime.put("activityId",tmp.getString("id"));
                        System.out.println("----->"+mapTime);
                        Utilities.httpRequest(Creazione_evento.this, Request.Method.POST, "/groups/"+id_group+"/nekomaActivities/"+tmp.getString("id")+"/date", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Intent homepageA = new Intent(Creazione_evento.this,Homepage.class);
                                startActivity(homepageA);
                            }
                        },new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Creazione_evento.this, error.toString(), Toast.LENGTH_LONG).show();
                                System.err.println(error.getMessage());
                            }
                        },mapTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Creazione_evento.this, error.toString(), Toast.LENGTH_LONG).show();
                    System.err.println(error.getMessage());
                }
            },map);

        }







    }

    public  static class SelectorLabels extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_fragment_view, null);
            LinearLayout ll = (LinearLayout) content.findViewById(R.id.labelsList);


            builder.setView(inflater.inflate(R.layout.dialog_fragment_view, null))
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

    public static class MyEtichette {
        private String name;
        private String id;

        MyEtichette(String name, String id){
            this.id = id;
            this.name = name;
        }

        public String getName(){
            return this.name;
        }
        public String getId(){
            return this.id;
        }

    }
}