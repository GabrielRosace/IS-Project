package com.example.nekoma_families_share;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Utilities {
    // Timeout per le richieste http
    private static final int MY_SOCKET_TIMEOUT_MS = 60000;

    // Creo lo spazio nelle shared preferences per salvare le informazioni che mi servono durante la navigazione
    public static SharedPreferences setSharedPreferences(Context context){
        SharedPreferences prefs;
        prefs=context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }
    // Ottengo queste preferenze
    public static SharedPreferences getPrefs(Context context){
        return context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
    }

    // Salvo il jwt nelle shared preferences
    public static void setToken(Context context,String token){
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit;
        edit=prefs.edit();
        edit.putString("token",token);
        edit.apply();
    }

    // Ottengo il token
    public static String getToken(Context context){
        SharedPreferences prefs = getPrefs(context);
        return  prefs.getString("token","");
    }

    /** With bearer token authentication and params */ // Metodo per semplificare le richieste http
    public static void httpRequest(Context context,int method, String endpoint , Response.Listener onSuccess, Response.ErrorListener onError, Map<String,String> params){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url= context.getString(R.string.url) + endpoint;

        StringRequest stringRequest1 = new StringRequest(method, url, onSuccess, onError){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+getToken(context));
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest1);
    }

    // Parsing del token per ottenere lo user id
    public static String getUserID(Context context){
        String user_id = "";
        String userToken = getToken(context);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user_id;
    }

    // Ottengo il group id dalle shared preferences
    public static String getGroupId(Context context) {
        return Utilities.getPrefs(context).getString("group","");
    }

}
