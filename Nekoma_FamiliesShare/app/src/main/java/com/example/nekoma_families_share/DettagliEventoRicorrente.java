package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DettagliEventoRicorrente extends AppCompatActivity {

    private String extraData;
    private Utilities.myRecEvent evento;

    private TextView name;
    private EditText descrizione;
    private TextView nPart;
    private Button btn;
    private ImageView img;

    private TextView recurr;

    private boolean isCreator;

    public DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento_ricorrente);


        name = (TextView) findViewById(R.id.eventName);
        descrizione = (EditText) findViewById(R.id.description);
        nPart = (TextView) findViewById(R.id.nPart);
        btn = (Button) findViewById(R.id.button);
        img = (ImageView) findViewById(R.id.eventImage);
        recurr = (TextView) findViewById(R.id.textView31);

//        ricorrenza = (TextView)findViewById(R.id.ricorrenza);
//        start_date = (TextView)findViewById(R.id.start_date);
//        end_date = (TextView)findViewById(R.id.end_date);


        // Aggiunta dell'evento torna indietro nella toolbar
        Toolbar t = (Toolbar) findViewById(R.id.toolbar2);
        t.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        // Ottengo informazioni dall'activity precedente
        extraData = intent.getStringExtra("evento");


        evento = new Utilities.myRecEvent(extraData);

        name.setText(evento.nome);
        descrizione.setText(evento.descrizione);
        nPart.append(String.valueOf(evento.nPart));

//        System.out.println(evento);


        if (evento.recType.equals("daily")) {
            String start = evento.start_date.substring(2, 12);
            String end = evento.end_date.substring(2, 12);
            recurr.setText("Questo evento si svolgerà dal " + start + " a " + end);
        } else if (evento.recType.equals("monthly")) {
            String[] start = evento.start_date.substring(1, evento.start_date.length() - 1).split(",");
            String[] end = evento.end_date.substring(1, evento.end_date.length() - 1).split(",");

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

            recurr.setText("Questo evento si svolgerà con cadenza mensile nei giorni " + giorni + " a partire da " + firstMonth + " fino a " + lastMonth);

        } else {
            String[] start = evento.start_date.substring(1, evento.start_date.length() - 1).split(",");
            String[] end = evento.end_date.substring(1, evento.end_date.length() - 1).split(",");
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

            recurr.setText("Questo evento si svolgerà con cadenza settimanale nei giorni " + giorni + " dal " + start[0].substring(1, 11) + " al " + end[end.length - 1].substring(1, 11));
        }


        isCreator = evento.owner_id.equals(Utilities.getUserID(this));


        initDatePicker();

        if (isCreator) {
            descrizione.setEnabled(true);
            btn.setText("Modifica");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> m = new HashMap<>();
                    m.put("description", descrizione.getText().toString());
                    Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.PUT, "/recurringActivity/" + evento.event_id, response -> {
                        Toast.makeText(DettagliEventoRicorrente.this, "Modifica effettuata con successo", Toast.LENGTH_SHORT).show();
                    }, System.out::println, m);
                }
            });
        }else{
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDatePicker(v);
                }
            });
        }



        new ImageDownloader(img).execute(evento.img);
    }


    public void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;
                Map<String,String> m = new HashMap<>();
                m.put("activity_id", evento.event_id);
                m.put("days", date);
                Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.POST, "/partecipant", response -> {
                    Toast.makeText(DettagliEventoRicorrente.this, "Partipazione effettuata", Toast.LENGTH_SHORT).show();
                    recreate();
                }, response -> {
                    Toast.makeText(DettagliEventoRicorrente.this, "Errore, partecipazione non aggiunta", Toast.LENGTH_SHORT).show();
                }, m);
            }
        };
        Calendar calendar = Calendar.getInstance();

//        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(startDate.getText().toString()));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePicker = new DatePickerDialog(this,dateListener,year,month,day);
    }


    public void openDatePicker(View v){
        datePicker.show();
    }

    // Classe che permette di scaricare le immagini dell'evento
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


}