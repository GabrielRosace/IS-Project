package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento_ricorrente);


        name = (TextView)findViewById(R.id.eventName);
        descrizione = (EditText)findViewById(R.id.description);
        nPart = (TextView) findViewById(R.id.nPart);
        btn = (Button)findViewById(R.id.button);
        img = (ImageView)findViewById(R.id.eventImage);
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

        System.out.println(evento);


        if(evento.recType.equals("daily")){
            String start = evento.start_date.substring(2,12);
            String end = evento.end_date.substring(2,12);
            recurr.setText("Questo evento si svolgerà dal "+start+" a "+end);
        }else if(evento.recType.equals("monthly")){
            recurr.setText("Questo evento si svolgerà con cadenza mensile nei giorni ");

        }else{
            System.out.println(evento.start_date.substring(2, evento.start_date.length()-2));
            String[] start = evento.start_date.substring(2, evento.start_date.length()-2).split(",");
            String[] end = evento.end_date.substring(2, evento.end_date.length()-2).split(",");
            String giorni = "";
            for (String s:start) {
                System.out.println(s);
            }

            recurr.setText("Questo evento si svolgerà con cadenza settimanale nei giorni ");
//            System.out.println("weekly");
        }


        isCreator = evento.owner_id.equals(Utilities.getUserID(this));

        if(isCreator){
            descrizione.setEnabled(true);
            btn.setText("Modifica");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> m = new HashMap<>();
                    m.put("description", descrizione.getText().toString());
                    Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.PUT, "/recurringActivity/"+evento.event_id, response -> {
                        Toast.makeText(DettagliEventoRicorrente.this, "Modifica effettuata con successo", Toast.LENGTH_SHORT).show();
                    }, System.out::println, m);
                }
            });
        }

        new ImageDownloader(img).execute(evento.img);
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
            try{
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
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