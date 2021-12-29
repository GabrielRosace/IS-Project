package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Creazione_date_evetno_ricorrente extends AppCompatActivity {
    private List<CheckBox> days;
    private List<String> datesStart;
    private List<String> datesEnd;
    private DatePickerDialog dataStartPicker;
    private DatePickerDialog dataEndPicker;
    private Button startDate;
    private String name;
    private String location;
    private String desc;
    private String labels;
    private Spinner spinner;
    private String reccurenceSelceted;
    private TextView titelDay;
    private TextView titleNumber;
    private EditText number;
    private LocalDate startLocalDate;
    private TextView datesMonth;
    private TextView NB;
    private Button buttonCreate;
    private List<String> printMonthDate = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_date_evetno_ricorrente);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar_data_event_ric);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
        days = new ArrayList<>();
        datesStart = new ArrayList<>();
        datesEnd = new ArrayList<>();
        buttonCreate = (Button) findViewById(R.id.createEventRicButton);
        buttonCreate.setEnabled(false);
        number = (EditText) findViewById(R.id.frequenceNumber);
        NB = (TextView) findViewById(R.id.nb);
        titleNumber = (TextView) findViewById(R.id.titleFrequenceNumber);
        datesMonth = (TextView) findViewById(R.id.viewDatesMonthly);
        titelDay = (TextView) findViewById(R.id.titleDay);
        days.add((CheckBox) findViewById(R.id.checkLun));
        days.add((CheckBox) findViewById(R.id.checkMar));
        days.add((CheckBox) findViewById(R.id.checkMer));
        days.add((CheckBox) findViewById(R.id.checkGio));
        days.add((CheckBox) findViewById(R.id.checkVen));
        days.add((CheckBox) findViewById(R.id.checkSab));
        days.add((CheckBox) findViewById(R.id.checkDom));
        NB.setVisibility(View.GONE);
        startDate = (Button) findViewById(R.id.startDateEventRic);
        initStartDatePicker();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
            location = extras.getString("location");
            desc = extras.getString("desc");
            labels = extras.getString("labels");
            //The key argument here must match that used in the other activity
        }
        spinner = (Spinner) findViewById(R.id.spinnerCadenzaEvento);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.cadenza, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startDate.setText("seleziona una data");
                switch(adapter.getItem(i).toString()){
                    case "Giornaliera":
                        titleNumber.setVisibility(View.GONE);
                        number.setVisibility(View.GONE);
                        titelDay.setVisibility(View.GONE);
                        NB.setVisibility(View.GONE);
                        for(CheckBox c : days){
                            c.setVisibility(View.GONE);
                        }
                        datesMonth.setText("");
                        for (int j=0;j<printMonthDate.size();j++){
                            printMonthDate.remove(j);
                        }
                        break;
                    case "Mensile":
                        titleNumber.setText("Inserire il numero di mesi");
                        titleNumber.setVisibility(View.VISIBLE);
                        number.setVisibility(View.VISIBLE);
                        titelDay.setVisibility(View.GONE);
                        NB.setVisibility(View.VISIBLE);
                        for(CheckBox c : days){
                            c.setVisibility(View.GONE);
                        }
                        datesMonth.setText("");
                        break;
                    case "Settimanale":
                        titleNumber.setText("Inserire il numero di settimane");
                        NB.setVisibility(View.GONE);
                        titelDay.setVisibility(View.VISIBLE);
                        titleNumber.setVisibility(View.VISIBLE);
                        number.setVisibility(View.VISIBLE);
                        for(CheckBox c : days){
                            c.setVisibility(View.VISIBLE);
                        }
                        datesMonth.setText("");
                        for (int j=0;j<printMonthDate.size();j++){
                            printMonthDate.remove(j);
                        }
                        break;
                }
                reccurenceSelceted = adapter.getItem(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                titelDay.setVisibility(View.GONE);
                for(CheckBox c : days){
                    c.setVisibility(View.GONE);
                }
            }
        });

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
        String d = setDate();
        String arr[] = d.split("/");
        if(arr[1].equals("[]") || arr[2].equals("[]"))
            Toast.makeText(Creazione_date_evetno_ricorrente.this,"dati non inseriti correttamente", Toast.LENGTH_LONG).show();
        else {
            map.put("group_id", id_group);
            map.put("creator_id", Utilities.getUserID(Creazione_date_evetno_ricorrente.this));
            map.put("name", name);
            map.put("color", "purple");
            map.put("description", desc);
            map.put("location", location);
            map.put("labels", labels);
            map.put("status", "false");
            map.put("activity_id", "");
            map.put("image_url", "");
            map.put("type", arr[0]);
            map.put("start_date", arr[1]);
            map.put("end_date", arr[2]);
            for (String s :
                    map.keySet()) {
                System.out.println(map.get(s));
            }
            Utilities.httpRequest(Creazione_date_evetno_ricorrente.this, Request.Method.POST, "/recurringActivity", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Intent homepageA = new Intent(Creazione_date_evetno_ricorrente.this, Homepage.class);
                    startActivity(homepageA);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Creazione_date_evetno_ricorrente.this, error.toString(), Toast.LENGTH_LONG).show();
                    System.err.println(error.getMessage());
                }
            }, map);
        }
    }
    private String setDate() {
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int shift = ((reccurenceSelceted.equals("Settimanale")) ? 7 : 1) * new Integer(number.getText().toString().equals("0") ? "1" : number.getText().toString());
        String s = new String();
        int i =0;
        boolean err = false;
        System.out.println("setDATE");
        if(reccurenceSelceted.equals("Settimanale")){
            s=s+"weekly/";
            for(CheckBox c: days){
                if(c.isChecked()){
                    switch (c.getText().toString()){
                        case "Lun":
                            datesStart.add(startLocalDate.format(formatter));
                            datesEnd.add(startLocalDate.plusDays(shift).format(formatter));
                            break;
                        case "Mar":
                            datesStart.add(startLocalDate.plusDays(1).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(1+shift).format(formatter));
                            break;
                        case "Mer":
                            datesStart.add(startLocalDate.plusDays(2).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(2+shift).format(formatter));
                            break;
                        case "Gio":
                            datesStart.add(startLocalDate.plusDays(3).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(3+shift).format(formatter));
                            break;
                        case "Ven":
                            datesStart.add(startLocalDate.plusDays(4).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(4+shift).format(formatter));
                            break;
                        case "Sab":
                            datesStart.add(startLocalDate.plusDays(5).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(5+shift).format(formatter));
                            break;
                        case "Dom":
                            datesStart.add(startLocalDate.plusDays(6).format(formatter));
                            datesEnd.add(startLocalDate.plusDays(6+shift).format(formatter));
                            break;
                    }
                }

            }
        }else if(reccurenceSelceted.equals("Mensile")){
            s=s+"monthly/";
            for(String date: datesStart){
                LocalDate d = LocalDate.parse(date,formatter);
                datesEnd.add(d.plusMonths(shift).format(formatter));
            }
        }else if(reccurenceSelceted.equals("Giornaliera")){
            s=s+"daily/";
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            datesStart.add(startLocalDate.format(formatter));
            datesEnd.add(startLocalDate.plusDays(7).format(formatter));

        }
        s=s+"[";
        for(String c:datesStart) {
            if (i == 0)
                s = s + c;
            else
                s = s + "," + c;
            i++;
        }
        i=0;
        s=s+"]/[";
        for(String c:datesEnd){
            if(i==0)
                s=s+c;
            else
                s=s+","+c;
            i++;
        }
        s=s+"]";
        System.out.println(s);

        return s;
    }
    private void initStartDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;
                String print = dayOfMonth+"-"+month+"-"+year;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                    print ="0"+dayOfMonth+"-0"+month+"-"+year;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                    print = "0"+dayOfMonth+"-"+month+"-"+year;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                    print = dayOfMonth+"-0"+month+"-"+year;
                }
                if(reccurenceSelceted.equals("Mensile")){
                    DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate d = LocalDate.parse(date,formatter);
                    if(datesStart.contains(d.format(formatter))) {
                        datesStart.remove(d.format(formatter));
                        printMonthDate.remove(print);
                    }else{
                        datesStart.add(d.format(formatter));
                        printMonthDate.add(print);
                    }
                    datesMonth.setText("");
                    for (String s:printMonthDate) {
                        datesMonth.append(s+", ");
                    }
                }else{
                    datesMonth.setText("");
                    startDate.setText(print);
                    //calcolo data inizio e fine della settimana
                    startLocalDate = getStartWeek(date);
                }
                buttonCreate.setEnabled(true);
            }
                //metodo che calcola tutte le date utili della settimana in base alle scelte
                //startWeek = getWeekDates(startDate);
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataStartPicker = new DatePickerDialog(this,dateListener,year,month,day);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        dataStartPicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        dataStartPicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
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
