package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

public class DettagliEventoRicorrente extends AppCompatActivity {

    private String extraData;
    private Utilities.myRecEvent evento;

    private TextView name;
    private EditText descrizione;
    private TextView nPart;
    private Button btn;
    private ImageView img;

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

        isCreator = evento.owner_id.equals(Utilities.getUserID(this));

        if(isCreator){
            descrizione.setEnabled(true);
            btn.setText("Modifica");
        }

        new ImageDownloader(img).execute(evento.img);
    }



    // Classe che permette di scaricare le immagini dell'evento
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
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