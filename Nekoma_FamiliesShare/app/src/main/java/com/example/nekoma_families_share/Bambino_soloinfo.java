package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Bambino_soloinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bambino_soloinfo);
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
                            if(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("_id").equals(user_id)){
                                Bambini bambino = new Bambini(new JSONObject(kid.getString(i)).getString("_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path"));
                                ImageView img_bambino = (ImageView) findViewById(R.id.img_bambino);
                                new ImageDownloader(img_bambino).execute(getString(R.string.urlnoapi)+bambino.image_path);
                                TextView nome_bambino = (TextView) findViewById(R.id.nome_bambino);
                                nome_bambino.setText(bambino.name+" "+bambino.surname);
                                ImageView img_genitore = (ImageView) findViewById(R.id.img_genitore);
                                CardView card_genitore = (CardView) findViewById(R.id.card_genitore);
                                card_genitore.setVisibility(View.GONE);
                                img_genitore.setVisibility(View.GONE);
                                /*TextView nome = (TextView) findViewById(R.id.nome);
                                nome.setText(new );*/
                                TextView nome_genitore = (TextView) findViewById(R.id.nome_genitore);
                                nome_genitore.setText(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("given_name")+" "+new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("family_name"));
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
                            }
                        }

                    }catch(JSONException e){
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