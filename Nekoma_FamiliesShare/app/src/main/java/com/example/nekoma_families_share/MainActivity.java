package com.example.nekoma_families_share;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View v){
        // modo per cambiare activity
        //Intent homepageA = new Intent(this,Homepage.class);
        // switchActivityIntent.putExtra("message", "From: " + FirstActivity.class.getSimpleName()); per passare parametri
        //startActivity(homepageA);
        // fine :)
        EditText email = (EditText)findViewById(R.id.email_text);
        EditText password = (EditText)findViewById(R.id.password_text);
        //COME FARE LE RICHIESTE AL SERVER
        RequestQueue login = Volley.newRequestQueue(this);
        String url= getString(R.string.url) + "/users/authenticate/email"; // R.string.url  --> perchè c'è scritto dentro string
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // solo per il token in questo caso
                SharedPreferences prefs = getPrefs();
                SharedPreferences.Editor edit;
                edit=prefs.edit();
                try {
                    JSONObject tmp = new JSONObject(response);
                    String saveToken=tmp.get("token").toString();
                    edit.putString("token",saveToken);
                    edit.apply();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                // solo per il token
                // ci spostiamo nella homepage
                Intent homepageA = new Intent(MainActivity.this,SceltaDelGruppo.class);
                startActivity(homepageA);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Le credenziali non sono corrette, riprova.", Toast.LENGTH_LONG).show();
                // System.err.println(error.getMessage());
            }
        }){
            // creare una classe anonima che estende string request :(

            @Override
            public byte[] getBody() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("email",email.getText().toString());
                params.put("password",password.getText().toString());
                params.put("language","it");
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        login.add(stringRequest);
        //COME FARE LE RICHIESTE AL SERVER FINE :')
    }
    public SharedPreferences getPrefs(){
        SharedPreferences prefs;
        prefs=MainActivity.this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }
    public void getGuide(View v){
        Intent guide = new Intent(MainActivity.this,Guide.class);
        startActivity(guide);
    }

}