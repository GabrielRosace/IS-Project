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

import org.json.JSONArray;
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
    public static SharedPreferences setSharedPreferences(Context context) {
        SharedPreferences prefs;
        prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }

    // Ottengo queste preferenze
    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
    }

    // Salvo il jwt nelle shared preferences
    public static void setToken(Context context, String token) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit;
        edit = prefs.edit();
        edit.putString("token", token);
        edit.apply();
    }

    // Ottengo il token
    public static String getToken(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString("token", "");
    }

    /**
     * With bearer token authentication and params
     */ // Metodo per semplificare le richieste http
    public static void httpRequest(Context context, int method, String endpoint, Response.Listener onSuccess, Response.ErrorListener onError, Map<String, String> params) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = context.getString(R.string.url) + endpoint;

        StringRequest stringRequest1 = new StringRequest(method, url, onSuccess, onError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getToken(context));
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
    public static String getUserID(Context context) {
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
        return Utilities.getPrefs(context).getString("group", "");
    }

    public static class myService implements Situation {
        public final String service_id;
        public final String owner_id;
        public final String nome;
        public final String descrizione;
        public final String location;
        public final String pattern;
        public final String car_space;
        public final String lend_obj;
        public final String lend_time;
        public final String pickuplocation;
        public final String img;
        public final String nPart;
        public final String recurrence;

        public myService(String service_id, String owner_id, String nome, String descrizione, String location, String pattern, String car_space, String lend_obj, String lend_time, String pickuplocation, String img, String nPart, String recurrence) {
            this.service_id = service_id;
            this.owner_id = owner_id;
            this.nome = nome;
            this.descrizione = descrizione;
            this.location = location;
            this.pattern = pattern; // il pattern che sceglie l'utente quando lo sceglie
            this.car_space = car_space;
            this.lend_obj = lend_obj; // l'oggetto dato in prestito
            this.lend_time = lend_time; // quando va restituito l'oggetto preso in prestito
            this.pickuplocation = pickuplocation; // il posto in cui devi andare a prendere qualcosa per un terzo
            this.img = img;
            this.nPart = nPart;
            this.recurrence = recurrence;
        }

        public myService(JSONObject obj) throws JSONException {
            this("service_id"/*TODO*/, "owner_id"/*TODO*/, obj.getString("name"), obj.getString("description"), obj.getString("location"), obj.getString("pattern"),obj.getString("car_space"),obj.getString("lend_obj"), obj.getString("lend_time"), obj.getString("pickuplocation"), obj.getString("image_url"), "nPart"/*TODO*/, "Recurrence"/*TODO*/ );
        }

        @Override
        public String toString() {
            return service_id + '/' + owner_id + '/' + nome + '/' + descrizione + '/' + location + '/' + pattern + '/' + car_space + '/' + lend_obj + '/' + lend_time + '/' + pickuplocation + '/' + img + '/' + nPart + '/' + recurrence;
        }

        @Override
        public String getName() {
            return this.nome;
        }

        @Override
        public String getImage() {
            return this.img;
        }

        @Override
        public String getLabels_id() {
            return "";
        }
    }

    public static class myRecEvent implements Situation {
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public final String labels;
        public final String owner_id;
        public final String recurrence;

        public myRecEvent(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String owner_id, String recurrence) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
            this.owner_id = owner_id;
            this.recurrence = recurrence;
        }

        public myRecEvent(JSONObject obj) throws JSONException {
            JSONObject recAct = null;
            recAct = obj.getJSONArray("RecurringActivity").getJSONObject(0);
            this.nome = recAct.getString("name");
            this.img = recAct.getString("image_url");
            this.event_id = obj.getString("activity_id");
            this.nPart = 10/*TODO*/;
            this.descrizione = recAct.getString("description");
            this.enddate = "" /*TODO*/;
            this.labels = ""/*TODO*/;
            this.owner_id = recAct.getString("creator_id");
            this.recurrence = ""/*TODO*/;
        }

        @Override
        public String toString() {
            return nome + '/' + event_id + '/' + img + '/' + nPart + '/' + descrizione + '/' + enddate + '/' + labels + '/' + owner_id;
        }

        @Override
        public String getName() {
            return this.nome;
        }

        @Override
        public String getImage() {
            return this.img;
        }

        @Override
        public String getLabels_id() {
            return this.labels;
        }
    }

    public interface Situation {
        String getName();

        String getImage();

        String getLabels_id();
    }
}
