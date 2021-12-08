package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Bambino_soloinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bambino_soloinfo);
        Toolbar t = (Toolbar) findViewById(R.id.bambinotoolbar);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String id_child = getKid();
        String user_id;
        String userToken = Utilities.getToken(Bambino_soloinfo.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try{
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
            Utilities.httpRequest(this,Request.Method.GET,"/children?ids[]="+id_child+"&searchBy=ids",new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONArray kid = new JSONArray(response);
                        for(int i=0;i<kid.length();++i){
                            System.out.println("sono kid(i):" + kid.getString(i));
                            if(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("user_id").equals(user_id)){
                                System.out.println("sono qui dentro il caso in cui sia mio figlio");
                                Bambini bambino = new Bambini(new JSONObject(kid.getString(i)).getString("_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path"));
                                ImageView img_bambino = (ImageView) findViewById(R.id.img_bambino);
                                new ImageDownloader(img_bambino).execute(getString(R.string.urlnoapi)+bambino.image_path);
                                TextView nome_bambino = (TextView) findViewById(R.id.nome_bambino);
                                nome_bambino.setText(bambino.name+" "+bambino.surname);
                                TextView nome = (TextView) findViewById(R.id.nome);
                                nome.setText("Nato il:");
                                LinearLayout card_genitore = (LinearLayout) findViewById(R.id.foto_genitore);
                                card_genitore.setVisibility(View.GONE);
                                TextView nome_genitore = (TextView) findViewById(R.id.nome_genitore);
                                String date =new JSONObject(kid.getString(i)).getString("birthdate");
                                String[] parts = date.split("T");
                                Date data = new SimpleDateFormat("yyyy-MM-dd").parse(parts[0]);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(data);
                                nome_genitore.setText(calendar.get(Calendar.DAY_OF_MONTH)+":"+calendar.get(Calendar.MONTH)+1+":"+calendar.get(Calendar.YEAR));

                                //aggoungi etichette al bambino
                                LinearLayout layout = (LinearLayout) findViewById(R.id.aggiunta_etichette);
                                layout.setVisibility(View.VISIBLE);
                                Spinner etichette = (Spinner) findViewById(R.id.spinner_etichette);
                                Button aggiungi_etichetta = (Button) findViewById(R.id.add_etichetta);

                                TextView text_gender = (TextView) findViewById(R.id.genere_);
                                if(!new JSONObject(kid.getString(i)).getString("gender").equals("unspecified")){
                                    if(new JSONObject(kid.getString(i)).getString("gender").equals("Male") || new JSONObject(kid.getString(i)).getString("gender").equals("male")){
                                        text_gender.setText("Maschio");
                                    }else{
                                        text_gender.setText("Femmina");
                                    }
                                }
                                TextView text_allergie = (TextView) findViewById(R.id.allergie_);
                                if(!new JSONObject(kid.getString(i)).getString("allergies").equals("")){
                                    text_allergie.setText(new JSONObject(kid.getString(i)).getString("allergies"));
                                }
                                TextView text_altreinfo = (TextView) findViewById(R.id.altre_info);
                                if(!new JSONObject(kid.getString(i)).getString("other_info").equals("")){
                                    text_altreinfo.setText(new JSONObject(kid.getString(i)).getString("other_info"));
                                }
                                TextView text_bisgoni = (TextView) findViewById(R.id.bisogni_);
                                if(!new JSONObject(kid.getString(i)).getString("special_needs").equals("")){
                                    text_bisgoni.setText(new JSONObject(kid.getString(i)).getString("special_needs"));
                                }
                            }else{
                                Bambini bambino = new Bambini(new JSONObject(kid.getString(i)).getString("_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path"));
                                ImageView img_bambino = (ImageView) findViewById(R.id.img_bambino);
                                new ImageDownloader(img_bambino).execute(getString(R.string.urlnoapi)+bambino.image_path);
                                TextView nome_bambino = (TextView) findViewById(R.id.nome_bambino);
                                nome_bambino.setText(bambino.name+" "+bambino.surname);
                                TextView nome_genitore = (TextView) findViewById(R.id.nome_genitore);
                                nome_genitore.setText(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("given_name")+" "+new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("family_name"));
                                ImageView img_genitore = (ImageView) findViewById(R.id.img_genitore);
                                new ImageDownloader(img_genitore).execute(getString(R.string.urlnoapi)+new JSONObject(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("image")).getString("path"));
                                TextView text_gender = (TextView) findViewById(R.id.genere_);
                                if(!new JSONObject(kid.getString(i)).getString("gender").equals("unspecified")){
                                    text_gender.setText(new JSONObject(kid.getString(i)).getString("gender"));
                                }
                                TextView text_allergie = (TextView) findViewById(R.id.allergie_);
                                if(!new JSONObject(kid.getString(i)).getString("allergies").equals("")){
                                    text_allergie.setText(new JSONObject(kid.getString(i)).getString("allergies"));
                                }
                                TextView text_altreinfo = (TextView) findViewById(R.id.altre_info);
                                if(!new JSONObject(kid.getString(i)).getString("other_info").equals("")){
                                    text_altreinfo.setText(new JSONObject(kid.getString(i)).getString("other_info"));
                                }
                                TextView text_bisgoni = (TextView) findViewById(R.id.bisogni_);
                                if(!new JSONObject(kid.getString(i)).getString("special_needs").equals("")){
                                    text_bisgoni.setText(new JSONObject(kid.getString(i)).getString("special_needs"));
                                }
                            }
                        }

                    }catch(JSONException | ParseException e){
                        e.printStackTrace();
                    }
                }
            },new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Bambino_soloinfo.this, error.toString(), Toast.LENGTH_LONG).show();
                    System.err.println(error.getMessage());
                }
            },new HashMap<>());
        }catch(JSONException e){
            e.printStackTrace();
        }

    }
    private static class Bambini{
        public final String id;
        public final String name;
        public final String surname;
        public final String image_path;

        private Bambini(String id, String name, String surname, String image_path){
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.image_path = image_path;
        }
    }
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        public final ImageView image;
        public ImageDownloader(ImageView image) {
            this.image = image;
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
            image.setImageBitmap(bitmap);
        }
    }
    public void getLista(View v) {
        Intent homepage = new Intent(Bambino_soloinfo.this, ListaBambiniAmici.class);
        startActivity(homepage);
    }
    public String getToken(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return  prefs.getString("token","");
    }
    public String getKid(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return  prefs.getString("id_child","");
    }
}