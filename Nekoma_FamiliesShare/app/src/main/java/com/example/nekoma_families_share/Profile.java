package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private String user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Ottengo il token
        String userToken = getToken();
        System.out.println("USER TOKEN: "+userToken);
        // Faccio il parse del token in modo tale da prendermi l'id dell'utente
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        System.out.println("Body");
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
            System.out.println(user_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Faccio richiesta al server in modo tale da potermi prendere le informazioni dell'utente
        RequestQueue profile = Volley.newRequestQueue(this);
        String url= getString(R.string.url) + "/users/"+user_id+"/profile";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject user_response = new JSONObject(response);
                    // user_response.getJSONObject("address") viene usato per prendere tutti oggetti contenuti nell'oggetto JSON principale in modo tale da non dover fare chiamate aggiuntive
                    JSONObject user_address = user_response.getJSONObject("address");
                    JSONObject user_image = user_response.getJSONObject("image");
                    //System.out.println("USER PROFILE DATA "+user_response);
                    //System.out.println("USER ADDRESS "+user_address);
                    //System.out.println("USER IMAGE "+user_image);
                    // Informazioni personali utente
                    EditText emailLabel = findViewById(R.id.profileEmail);
                    EditText nameLabel = findViewById(R.id.profileName);
                    EditText surnameLabel = findViewById(R.id.profileSurname);
                    EditText phoneLabel = findViewById(R.id.profilePhone);
                    EditText addressLabel = findViewById(R.id.profileStreet);
                    EditText numberLabel = findViewById(R.id.profileNumber);
                    EditText cityLabel = findViewById(R.id.profileCity);
                    Switch edit = findViewById(R.id.EditSwitch);
                    emailLabel.setEnabled(false);
                    nameLabel.setEnabled(false);
                    surnameLabel.setEnabled(false);
                    phoneLabel.setEnabled(false);
                    emailLabel.setText(user_response.getString("email"));
                    nameLabel.setText(user_response.getString("given_name"));
                    surnameLabel.setText(user_response.getString("family_name"));
                    phoneLabel.setText((user_response.getString("phone")).length()==0?"Non specificato":user_response.getString("phone"));
                    // Informazioni su indirizzo dell'utente
                    addressLabel.setEnabled(false);
                    numberLabel.setEnabled(false);
                    cityLabel.setEnabled(false);
                    addressLabel.setText((user_address.getString("street").length()==0?"Non specificato":user_address.getString("street")));
                    numberLabel.setText((user_address.getString("number").length()==0?"":user_address.getString("number")));
                    cityLabel.setText((user_address.getString("city")).length()==0?"Non specificato":user_response.getString("city"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                headers.put("Authorization","Bearer "+userToken);
                return headers;
            }
        };
        profile.add(stringRequest);

        Switch editSwitch = (Switch)findViewById(R.id.EditSwitch);
        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println("Check!");
                EditText emailLabel = findViewById(R.id.profileEmail);
                EditText nameLabel = findViewById(R.id.profileName);
                EditText surnameLabel = findViewById(R.id.profileSurname);
                EditText phoneLabel = findViewById(R.id.profilePhone);
                EditText addressLabel = findViewById(R.id.profileStreet);
                EditText numberLabel = findViewById(R.id.profileNumber);
                EditText cityLabel = findViewById(R.id.profileCity);
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

    protected String getToken(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return  prefs.getString("token","");
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
                headers.put("Authorization","Bearer "+getToken());
                return headers;
            }
        };
        profile.add(stringRequest);
    }

    public void EditUser(View v){
        EditText emailLabel = findViewById(R.id.profileEmail);
        EditText nameLabel = findViewById(R.id.profileName);
        EditText surnameLabel = findViewById(R.id.profileSurname);
        EditText phoneLabel = findViewById(R.id.profilePhone);
        EditText addressLabel = findViewById(R.id.profileStreet);
        EditText numberLabel = findViewById(R.id.profileNumber);
        EditText cityLabel = findViewById(R.id.profileCity);
        RequestQueue profile = Volley.newRequestQueue(this);
        String url= getString(R.string.url) + "/users/"+user_id+"/profile";
        StringRequest stringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(Profile.this, "Profile edited succesfully!", Toast.LENGTH_LONG).show();
                Intent homepage = new Intent(Profile.this,Homepage.class);
                startActivity(homepage);
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
                headers.put("Authorization","Bearer "+getToken());
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
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
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        profile.add(stringRequest);
    }



}