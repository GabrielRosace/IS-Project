package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Creazione_date_evetno_ricorrente extends AppCompatActivity {
    private List<CheckBox> days;
    private DatePickerDialog dataStartPicker;
    private DatePickerDialog dataEndPicker;
    private Button startDate;
    private Button endDate;
    private String name;
    private String location;
    private String desc;
    private String labels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_date_evetno_ricorrente);
        days = new ArrayList<>();
        days.add((CheckBox) findViewById(R.id.checkLun));
        days.add((CheckBox) findViewById(R.id.checkMar));
        days.add((CheckBox) findViewById(R.id.checkMer));
        days.add((CheckBox) findViewById(R.id.checkGio));
        days.add((CheckBox) findViewById(R.id.checkVen));
        days.add((CheckBox) findViewById(R.id.checkSab));
        days.add((CheckBox) findViewById(R.id.checkDom));
        startDate = (Button) findViewById(R.id.startDateEventRic);
        endDate = (Button) findViewById(R.id.endDateEventRic);
        initStartDatePicker();
        initEndDatePicker();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
            location = extras.getString("location");
            desc = extras.getString("desc");
            labels = extras.getString("labels");
            //The key argument here must match that used in the other activity
        }

    }
    public void openDateStartPicker(View v){
        dataStartPicker.show();
    }

    public void openDateEndPicker(View v){
        dataEndPicker.show();
    }

    public void createEvent(View v){
        HashMap<String,String> map = new HashMap<>();
        String id_group = Utilities.getPrefs(this).getString("group", "");
        map.put("group_id",id_group);
        map.put("creator_id", Utilities.getUserID(Creazione_date_evetno_ricorrente.this));
        map.put("name", name);
        map.put("color","purple");
        map.put("description",desc);
        map.put("location", location);
        map.put("labels",labels);
        map.put("status", "false");
        map.put("activity_id","");
        map.put("imgUrl","");

        for (CheckBox c: days) {
            if(c.isChecked()){
                //System.out.println("----->"+c.getText().toString());
            }
        }

        Utilities.httpRequest(Creazione_date_evetno_ricorrente.this, Request.Method.POST, "/recurringActivity", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject tmp = new JSONObject(response);
                    System.out.print(tmp.toString());
                  //  map.put("activityId", tmp.getString("id"));
                    Ut
                }catch (JSONException e) {
                    e.printStackTrace();
                }

               // Intent homepageA = new Intent(Creazione_date_evetno_ricorrente.this,Homepage.class);
               // startActivity(homepageA);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Creazione_date_evetno_ricorrente.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },map);
    }
    private void initStartDatePicker() {
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
                startDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);
                //metodo che calcola tutte le date utili della settimana in base alle scelte
                //startWeek = getWeekDates(startDate);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataStartPicker = new DatePickerDialog(this,dateListener,year,month,day);
    }
    private void initEndDatePicker() {
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
                endDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);
                //metodo che calcola tutte le date utili della settimana in base alle scelte
                //startWeek = getWeekDates(startDate);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataEndPicker = new DatePickerDialog(this,dateListener,year,month,day);
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
}
