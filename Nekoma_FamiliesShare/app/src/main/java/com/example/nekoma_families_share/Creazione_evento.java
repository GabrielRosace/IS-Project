package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Creazione_evento extends AppCompatActivity  {
    protected static List<MyEtichette> etichette;
    private List<Button> labelSelect;
    private Date startDate;
    private Date endDate;
    private Button buttonStartDate;
    private Button buttonEndDate;
    private DatePickerDialog dataStartPicker;
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
        buttonStartDate = (Button) findViewById(R.id.startDateEvent);
        buttonEndDate = (Button) findViewById(R.id.endDateEvent);
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

    public void eventEndDate(View v){
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                }
                buttonEndDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);

            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dataStartPicker = new DatePickerDialog(this,dateListener,year,month,day);
        dataStartPicker.show();
    }

    public void eventStartDate(View v){
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                }
                buttonStartDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);

            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataStartPicker = new DatePickerDialog(this,dateListener,year,month,day);
        dataStartPicker.show();
    }
    private LocalDate getStartWeek(String sDate){
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDate = LocalDate.parse(sDate,formatter);
        switch (firstDate.getDayOfWeek().toString()){
            case "TUESDAY":
                firstDate = firstDate.minusDays(1);
                break;
            case "WEDNESDAY":
                firstDate = firstDate.minusDays(2);
                break;
            case "THURSDAY":
                firstDate = firstDate.minusDays(3);
                break;
            case "FRIDAY":
                firstDate = firstDate.minusDays(4);
                break;
            case "SATURDAY":
                firstDate = firstDate.minusDays(5);
                break;
            case "SUNDAY":
                firstDate = firstDate.minusDays(6);
                break;
        }
        return firstDate;
    }

    public void onClickCreaButton(View v) throws ParseException {
        String nome = ((EditText)findViewById(R.id.inputEventNameRic)).getText().toString();
        String luogo = ((EditText)findViewById(R.id.eventLocation)).getText().toString();
        String desc = ((EditText)findViewById(R.id.inputDescEventRic)).getText().toString();

        if(nome.equals("") || luogo.equals("") || desc.equals("") || buttonEndDate.getText().toString().equals("") || buttonStartDate.getText().toString().equals("")){
            Toast.makeText(Creazione_evento.this, "INSERIRE TUTTI I CAMPI", Toast.LENGTH_LONG).show();
        }else{
            String s = buttonStartDate.getText().toString();
            String s2 = buttonEndDate.getText().toString();
            String startD = s+"T00:00:00Z";
            String endD = s2+"T00:00:00Z";
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
            System.out.println("-----> desc "+ desc);

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

            HashMap<String,String> mapTime = new HashMap<>();
            mapTime.put("summary",nome);
            mapTime.put("location",luogo);
            mapTime.put("start",startD);
            mapTime.put("end",endD);
            mapTime.put("cost","");
            mapTime.put("requiredChildren","2");
            mapTime.put("groupId",id_group);
            mapTime.put("startHour","10");
            mapTime.put("link","");
            mapTime.put("requiredParents","2");
            mapTime.put("repetition","none");
            mapTime.put("activityColor","#000000");
            mapTime.put("children","[]");
            mapTime.put("externals","[]");
            mapTime.put("endHour","10");
            mapTime.put("category","");
            mapTime.put("status","ongoing");
            mapTime.put("parents","[]");
            mapTime.put("description",desc);



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
                                Toast.makeText(Creazione_evento.this, "Evento aggiunto correttamente", Toast.LENGTH_LONG).show();
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