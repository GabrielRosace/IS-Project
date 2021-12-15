package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Creazione_evento extends AppCompatActivity  {
    protected static List<MyEtichette> etichette;
    private List<Button> labelSelect;
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

        labelSelect = new ArrayList<>();
        String id_group = Utilities.getPrefs(this).getString("group", "");
        Utilities.httpRequest(this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    etichette = new ArrayList<>();

                    JSONArray tmp = new JSONArray(response);
                    System.out.println(tmp);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.linearLabels);
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
        String nome = ((EditText)findViewById(R.id.inputEventName)).getText().toString();
        String luogo = ((EditText)findViewById(R.id.inputEventLcation)).getText().toString();
        String desc = ((EditText)findViewById(R.id.inputDescEvent)).getText().toString();
        String dataInizio = ((EditText)findViewById(R.id.inputEventDate)).getText().toString();
        String dataFine = ((EditText)findViewById(R.id.inputEventEndEvent)).getText().toString();
        String inizio = ((EditText) findViewById(R.id.inputEventStartTime)).getText().toString();
        String fine = ((EditText) findViewById(R.id.inputEventEndTime)).getText().toString();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Date d = format.parse(dataInizio+" "+inizio);
        String s = format2.format(d);
        Date DInizio = format2.parse(s);

        d = format.parse(dataFine+" "+fine);
        s = format2.format(d);
        Date DFine = format2.parse(s);
        System.out.println(DInizio + " " +DFine);
        String labels = "[";
        Iterator i = labelSelect.iterator();
        boolean control = true;
        while(i.hasNext()){
            Button b = (Button) i.next();
            int buttonColor = ((ColorDrawable) b.getBackground()).getColor();
            if(buttonColor == Color.GREEN) {
                if(control){
                    labels = labels + b.getText().toString();
                    control = false;
                }else
                    labels = labels + "," + b.getText().toString();

            }
        }
        labels = labels + "]";
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
        map.put("labels",labels);
        map.put("status", "accepted");
        map.put("activity_id","");

        map.put("imageUrl","");
        System.out.println("------>"+labels);

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

        Utilities.httpRequest(this, Request.Method.POST, "/groups/"+id_group+"/nekomaActivities", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("--------------POST---------------");
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Creazione_evento.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },map);

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