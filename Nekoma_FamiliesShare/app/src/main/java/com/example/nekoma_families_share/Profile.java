package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private String user_id = "";
    private EditText emailLabel;
    private EditText nameLabel;
    private EditText surnameLabel;
    private EditText phoneLabel;
    private EditText addressLabel;
    private EditText numberLabel;
    private EditText cityLabel;
    private Switch editSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_profilo);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Ottengo il token
        String userToken = Utilities.getToken(this);
        System.out.println(userToken);
        // Faccio il parse del token in modo tale da prendermi l'id dell'utente
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utilities.httpRequest(this,Request.Method.GET,"/users/"+user_id+"/profile",response -> {
            try {
                JSONObject user_response = new JSONObject((String) response);
                // user_response.getJSONObject("address") viene usato per prendere tutti oggetti contenuti nell'oggetto JSON principale in modo tale da non dover fare chiamate aggiuntive
                JSONObject user_address = user_response.getJSONObject("address");
                JSONObject user_image = user_response.getJSONObject("image");
                // Informazioni personali utente
                emailLabel = findViewById(R.id.profileEmail);
                emailLabel.setEnabled(false);
                emailLabel.setText(user_response.getString("email"));
                nameLabel = findViewById(R.id.profileName);
                nameLabel.setEnabled(false);
                nameLabel.setText(user_response.getString("given_name"));
                surnameLabel = findViewById(R.id.profileSurname);
                surnameLabel.setEnabled(false);
                surnameLabel.setText(user_response.getString("family_name"));
                phoneLabel = findViewById(R.id.profilePhone);
                phoneLabel.setEnabled(false);
                if (((user_response.getString("phone")).length()!= 0)) {
                    phoneLabel.setText(user_response.getString("phone"));
                } else {
                    phoneLabel.setHint("Non specificato");
                }
                // Informazioni su indirizzo dell'utente
                addressLabel = findViewById(R.id.profileStreet);
                addressLabel.setEnabled(false);
                if (((user_address.getString("street")).length()!= 0)) {
                    addressLabel.setText(user_address.getString("street"));
                } else {
                    addressLabel.setHint("Non specificato");
                }
                numberLabel = findViewById(R.id.profileNumber);
                numberLabel.setEnabled(false);
                if (((user_address.getString("number")).length()!= 0)) {
                    numberLabel.setText(user_address.getString("number"));
                } else {
                    numberLabel.setHint(" ");
                }
                cityLabel = findViewById(R.id.profileCity);
                cityLabel.setEnabled(false);
                if (((user_address.getString("city")).length()!= 0)) {
                    cityLabel.setText(user_address.getString("city"));
                } else {
                    cityLabel.setHint("Non specificato");
                }
                ImageDownloader image = new ImageDownloader(findViewById(R.id.profileImage));
                image.execute(getString(R.string.urlnoapi)+user_image.getString("path"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(Profile.this, error.toString(), Toast.LENGTH_LONG).show();
            System.err.println(error.getMessage());
        }, new HashMap<>());

        editSwitch = (Switch)findViewById(R.id.EditSwitch);
        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button saveChange = findViewById(R.id.profileSaveChanges);
                emailLabel.setEnabled(isChecked);
                nameLabel.setEnabled(isChecked);
                surnameLabel.setEnabled(isChecked);
                phoneLabel.setEnabled(isChecked);
                addressLabel.setEnabled(isChecked);
                saveChange.setEnabled(isChecked);
                numberLabel.setEnabled(isChecked);
                cityLabel.setEnabled(isChecked);
            }
        });
    }

    public void DeleteUser(View v){
        RequestQueue profile = Volley.newRequestQueue(this);
        String url= getString(R.string.url) + "/users/"+user_id;
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Intent login = new Intent(Profile.this,MainActivity.class);
                startActivity(login);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Profile.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+Utilities.getToken(Profile.this));
                return headers;
            }
        };
        profile.add(stringRequest);
    }

    public void EditUser(View v){
        Map<String,String> params = new HashMap<>();
        params.put("given_name",nameLabel.getText().toString());
        params.put("family_name",surnameLabel.getText().toString());
        params.put("email",emailLabel.getText().toString());
        params.put("phone",phoneLabel.getText().toString());
        params.put("phone_type","unspecified");
        params.put("visible","true");
        params.put("street",addressLabel.getText().toString());
        params.put("number",numberLabel.getText().toString());
        params.put("city",cityLabel.getText().toString());
        params.put("description","");
        params.put("contact_option","email");

        Utilities.httpRequest(this,Request.Method.PATCH,"/users/"+user_id+"/profile",response -> {
            Toast.makeText(Profile.this, "Profile edited succesfully!", Toast.LENGTH_LONG).show();
            Intent homepage = new Intent(Profile.this,Homepage.class);
            startActivity(homepage);
        },error -> {
            Toast.makeText(Profile.this, error.toString(), Toast.LENGTH_LONG).show();
            System.err.println(error.getMessage());
        },params);
    }

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


    public void getHomepage(View v){
        Intent  homepage= new Intent(Profile.this,Homepage.class);
        startActivity(homepage);
    }

}