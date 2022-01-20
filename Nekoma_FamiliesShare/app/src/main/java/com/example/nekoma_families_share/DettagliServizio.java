package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DettagliServizio extends AppCompatActivity {
    private Utilities.myService service;
    private TextView name;
    private TextView location;
    private TextView date;
    private TextView forType;
    private TextView forTypeTitle;
    private TextView rec;
    private TextView recType;
    private TextView dateSelecteString;
    private Button buttonS;
    private FloatingActionButton addData;
    private EditText desc;
    private ImageView img;
    public ImageView delete;
    private TextView lendTimeT;
    private TextView lendTime;
    private  String groupId;
    public DatePickerDialog datePicker;
    public ConstraintLayout layout;
    private TextView startText;
    private TextView endText;
    private TextView end;
    private TextView start;
    private List<String> dateSelect;
    private List<String> dateSelectDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_servizio);

        Toolbar t = (Toolbar) findViewById(R.id.toolbarService);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initDatePicker();
        addData= (FloatingActionButton) findViewById(R.id.addDateService);
        dateSelect = new ArrayList<>();
        dateSelectDB = new ArrayList<>();
        layout = (ConstraintLayout) findViewById(R.id.progressLayoutService);
        groupId = Utilities.getGroupId(DettagliServizio.this);
        String extras = getIntent().getStringExtra("servizio");
        service = new Utilities.myService(extras);
        String[] data  = extras.split("\\$");
        delete = (ImageView) findViewById(R.id.deleteImg);
        dateSelecteString= (TextView) findViewById(R.id.dateSelected);
        img = (ImageView) findViewById(R.id.imageSevice);
        name = (TextView) findViewById(R.id.textName);
        location = (TextView) findViewById(R.id.locationService);
        forType = (TextView) findViewById(R.id.editForType);
        forTypeTitle = (TextView) findViewById(R.id.editForTypeTitle);
        rec = (TextView) findViewById(R.id.recurenceType);
        recType = (TextView) findViewById(R.id.recurrenceTypeText);
        buttonS = (Button) findViewById(R.id.buttonService);
        desc = (EditText) findViewById(R.id.desceMultiLineService);
        lendTimeT = (TextView) findViewById(R.id.lendTime);
        lendTime = (TextView) findViewById(R.id.lendTime2);
        startText = (TextView) findViewById(R.id.startText);
        endText = (TextView) findViewById(R.id.endText);
        start = (TextView) findViewById(R.id.start);
        end = (TextView) findViewById(R.id.end);
        lendTime.setVisibility(View.GONE);
        lendTimeT.setVisibility(View.GONE);
        buttonS.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        addData.setVisibility(View.GONE);
        new ImageDownloader(img).execute(service.img);
//        for(int i=0;i<data.length;i++){
//            System.out.println("<--->"+data[i]);
//        }
        name.setText(this.service.nome);
        location.setText(this.service.location);
        desc.setText(service.descrizione);
        if(service.pattern.equals("car")){
            forType.setText(service.car_space);
            forTypeTitle.setText("Posti auto disponibili:");
            lendTime.setVisibility(View.GONE);
            lendTimeT.setVisibility(View.GONE);
        }else if(service.pattern.equals("lend")){
            lendTime.setVisibility(View.VISIBLE);
            lendTimeT.setVisibility(View.VISIBLE);
            String[] s = service.lend_time.split("-");
            lendTime.setText(s[1]+"-"+s[0]+"-"+s[2]);
            forType.setText(service.lend_obj);
            forTypeTitle.setText("Ogetto in prestito:");
        }else if(service.pattern.equals("pickup")){
            forType.setText(service.pickuplocation);
            forTypeTitle.setText("luogo di ritrovo:");
            lendTime.setVisibility(View.GONE);
            lendTimeT.setVisibility(View.GONE);
        }
        if(service.recurrence.equals("true")){
            startText.setVisibility(View.GONE);
            endText.setVisibility(View.GONE);
            this.start.setVisibility(View.GONE);
            this.end.setVisibility(View.GONE);
            recType.setText(service.recType);
            if(service.recType.equals("monthly")){
                String[] start = service.start_date.substring(1, service.start_date.length() - 1).split(",");
                String[] end = service.end_date.substring(1, service.end_date.length() - 1).split(",");

                String giorni = "";
                for (String s : start) {
                    s = s.substring(1, 11);
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    try {
                        c.setTime(sdf.parse(s));
                        giorni += c.get(Calendar.DAY_OF_MONTH) + ",";
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                giorni = giorni.substring(0, giorni.length() - 1);

                Date startDate, endDate;
                String firstMonth = "", lastMonth = "";
                try {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    c.setTime(Objects.requireNonNull(s.parse(start[0].substring(1, 11))));

                    firstMonth = getMonth(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);


                    c.setTime(Objects.requireNonNull(s.parse(end[0].substring(1, 11))));
                    lastMonth = getMonth(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);


                } catch (ParseException e) {
                    e.printStackTrace();
                }

                recType.setText("Questo servizio si svolgerà con cadenza mensile nei giorni " + giorni + " a partire da " + firstMonth + " fino a " + lastMonth);
            }
            if(service.recType.equals("weekly")){
                String[] start = service.start_date.substring(1, service.start_date.length() - 1).split(",");
                String[] end = service.end_date.substring(1, service.end_date.length() - 1).split(",");
                String giorni = "";
                for (String s : start) {
                    s = s.substring(1, 11);
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    try {
                        c.setTime(sdf.parse(s));
                        switch (c.get(Calendar.DAY_OF_WEEK)) {
                            case 1:
                                giorni += "Domenica,";
                                break;
                            case 2:
                                giorni += "Lunedì,";
                                break;
                            case 3:
                                giorni += "Martedì,";
                                break;
                            case 4:
                                giorni += "Mercoledì,";
                                break;
                            case 5:
                                giorni += "Giovedì,";
                                break;
                            case 6:
                                giorni += "Venerdì,";
                                break;
                            case 7:
                                giorni += "Sabato,";
                                break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                giorni = giorni.substring(0, giorni.length() - 1);

                recType.setText("Questo evento si svolgerà con cadenza settimanale nei giorni " + giorni + " dal " + start[0].substring(1, 11) + " al " + end[end.length - 1].substring(1, 11));

            }
            if(service.recType.equals("daily")){
                String start = service.start_date.substring(2, 12);
                String end = service.end_date.substring(2, 12);
                recType.setText("Questo evento si svolgerà dal " + start + " a " + end);
            }

        }else{
            String[] start = service.formatDate(service.start_date.substring(2,service.start_date.length())).split("-");
            String[] end = service.formatDate(service.end_date.substring(2,service.end_date.length())).split("-");
//            System.out.println("data ----> "+ service.start_date.substring(2,service.start_date.length()));
            this.end.setText(end[1]+"-"+end[0]+"-"+end[2]);
            this.start.setText(start[1]+"-"+start[0]+"-"+start[2]);
            startText.setVisibility(View.VISIBLE);
            endText.setVisibility(View.VISIBLE);
            this.start.setVisibility(View.VISIBLE);
            this.end.setVisibility(View.VISIBLE);
            rec.setVisibility(View.GONE);
            recType.setVisibility(View.GONE);
        }
        if(service.owner_id.equals(Utilities.getUserID(DettagliServizio.this))){
            buttonS.setText("Modifica");
            addData.setVisibility(View.GONE);
            desc.setEnabled(true);
            buttonS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,String> map = new HashMap();
                    map.put("name", name.getText().toString());
                    map.put("description", desc.getText().toString());
                    map.put("location", location.getText().toString());
                    map.put("pattern",service.recType);
                    if(service.pattern.equals("car")){
                        map.put( "car_space",service.car_space );
                    }else if(service.pattern.equals("lend")){
                        map.put( "lend_obj", service.lend_obj);
                        map.put( "lend_time",service.lend_time);
                    }else if(service.pattern.equals("pickup")){
                        map.put( "pickuplocation", service.pickuplocation);
                    }
                    map.put("img",service.img);
                    Utilities.httpRequest(DettagliServizio.this, Request.Method.PATCH,"/groups/"+groupId+"/service/"+service.service_id, response -> {
                        finish();
                    }, System.err::println, map);
                }
            });
            delete.setVisibility(View.VISIBLE);
            buttonS.setVisibility(View.VISIBLE);
            layout.setVisibility(View.GONE);
        }else{

            HashMap<String,String> m = new HashMap<>();
            List<Utilities.myService> l = new ArrayList<>();
            m.put("paretcipant", Utilities.getUserID(DettagliServizio.this));
            Utilities.httpRequest(DettagliServizio.this, Request.Method.GET,"/groups/"+groupId+"/service?partecipant=me", response -> {
                try {
                    JSONArray arr = new JSONArray((String) response);
//                    System.out.println("ciao ");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        l.add(new Utilities.myService(obj));
//                        System.out.println("----->---->"+l.toString());
                    }
                    if(!Utilities.myService.conteinService(l,this.service)){
                        buttonS.setText("Partecipa");
                        if(service.recurrence.equals("true"))
                            addData.setVisibility(View.VISIBLE);
                        desc.setEnabled(false);
                        buttonS.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) { //apre calendar
                                Map<String, String> myMap = new HashMap<>();
                                String s="[";
                                Iterator i = dateSelectDB.iterator();

                                while(i.hasNext()){
                                    String sup = (String) i.next();
                                    s = (i.hasNext()) ? s+sup+"," : s+sup;
                                }
                                s = s+"]";
                                myMap.put("days", s);
                                Utilities.httpRequest(DettagliServizio.this, Request.Method.POST, "/groups/"+groupId+"/service/"+service.service_id+"/partecipate", response -> {
                                    Toast.makeText(DettagliServizio.this, "Partecipazione effettuata", Toast.LENGTH_SHORT).show();
                                    recreate();
                                }, response -> {
                                    Toast.makeText(DettagliServizio.this, "Errore, partecipazione non aggiunta", Toast.LENGTH_SHORT).show();
                                }, myMap);
                            }
                        });
                        buttonS.setVisibility(View.VISIBLE);
                        layout.setVisibility(View.GONE);
                    }else{
                        addData.setVisibility(View.GONE);
                        buttonS.setText("Smetti di partecipare");
                        desc.setEnabled(false);
                        buttonS.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                HashMap<String,String> map = new HashMap();
                                map.put("activity_id", service.service_id);
                                map.put("user_id",Utilities.getUserID(DettagliServizio.this));
                                Utilities.httpRequest(DettagliServizio.this, Request.Method.DELETE,"/groups/"+groupId+"/service/"+service.service_id+"/partecipate", response -> {
                                    finish();
                                }, System.err::println,map);
                            }
                        });
                        buttonS.setVisibility(View.VISIBLE);
                        layout.setVisibility(View.GONE);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }, System.err::println,m);

        }
    }
    public void addDate(View v){
        openDatePicker(v);
    }
    public void deleteService(View v){
        Utilities.httpRequest(DettagliServizio.this, Request.Method.DELETE,"/groups/"+groupId+"/service/"+service.service_id, response -> {
            Toast.makeText(DettagliServizio.this, "Servizio eliminato con successo", Toast.LENGTH_SHORT).show();
//            recreate();
            finish();
        }, System.err::println, new HashMap<>());
    }
    public void openDatePicker(View v) {
        datePicker.show();
    }
    private String getMonth(int month) {
        String firstMonth = "";
        switch (month) {
            case Calendar.JANUARY:
                firstMonth = "Gennaio";
                break;
            case Calendar.FEBRUARY:
                firstMonth = "Febbraio";
                break;
            case Calendar.MARCH:
                firstMonth = "Marzo";
                break;
            case Calendar.APRIL:
                firstMonth = "Aprile";
                break;
            case Calendar.MAY:
                firstMonth = "Maggio";
                break;
            case Calendar.JUNE:
                firstMonth = "Giugno";
                break;
            case Calendar.JULY:
                firstMonth = "Luglio";
                break;
            case Calendar.AUGUST:
                firstMonth = "Agosto";
                break;
            case Calendar.SEPTEMBER:
                firstMonth = "Settembre";
                break;
            case Calendar.OCTOBER:
                firstMonth = "Ottobre";
                break;
            case Calendar.NOVEMBER:
                firstMonth = "Novembre";
                break;
            case Calendar.DECEMBER:
                firstMonth = "Dicembre";
                break;

        }
        return firstMonth;
    }
    public void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String s = year + "-" + month + "-" + dayOfMonth;
                String print = dayOfMonth+"-"+month+"-"+year;
                if(dateSelectDB.contains(s)){
                    dateSelectDB.remove(s);
                    dateSelect.remove(print);
                }else{
                    dateSelect.add(print);
                    dateSelectDB.add(s);
                }

                dateSelecteString.setText(" ");
                for(String o : dateSelect){
                    dateSelecteString.append(o+" ");
                }
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //Blocco il calendario in modo tale che l'utente possa selezionare solamente i giorni del mese corrente
        datePicker = new DatePickerDialog(this,dateListener,year,month,day);
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