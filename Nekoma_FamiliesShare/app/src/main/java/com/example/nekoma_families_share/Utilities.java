package com.example.nekoma_families_share;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.util.List;
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

    // Metodo per semplificare le richieste http con autenticazione e parametri
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
        public final String recType;
        public final String start_date;
        public final String end_date;
        public final String recurrence;


        public myService(String service_id, String owner_id, String nome, String descrizione, String location, String pattern, String car_space, String lend_obj, String lend_time, String pickuplocation, String img, String nPart, String recType, String start_date, String end_date, String recurrence) {
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
            this.recType = recType;
            this.start_date = start_date;
            this.end_date = end_date;
            this.recurrence = recurrence;
        }

        //        Creazione di un servizio a partire dal json che arriva dal server
        public myService(JSONObject obj) throws JSONException {
            this(obj.getString("service_id"), obj.getString("owner_id"), obj.getString("name"), obj.has("description") ? obj.getString("description") : "", obj.getString("location"), obj.getString("pattern"), obj.has("car_space") ? obj.getString("car_space") : "", obj.has("lend_obj") ? obj.getString("lend_obj") : "", obj.has("lend_time") ? obj.getString("lend_time") : "", obj.has("pickuplocation") ? obj.getString("pickuplocation") : "", obj.getString("img"), obj.getString("nPart"), obj.getString("type"), obj.getJSONArray("start_date").toString(), obj.getJSONArray("end_date").toString(), obj.getString("recurrence"));
        }

        //        Creazione di un servizio a partire da una stringa nel seguente formato  ...$...$...$...
        public myService(String s) {
            String[] strings = s.split("\\$");
            String dateLend = "";
            if(!strings[8].equals("")){
                String[] lendTime = strings[8].split("T");
                String[] settingDate = lendTime[0].split("-");
                dateLend = settingDate[1]+"-"+settingDate[2]+"-"+settingDate[0];
            }
            this.service_id = strings[0];
            this.owner_id = strings[1];
            this.nome = strings[2];
            this.descrizione = strings[3];
            this.location = strings[4];
            this.pattern = strings[5]; // il pattern che sceglie l'utente quando lo sceglie
            this.car_space = strings[6];
            this.lend_obj = strings[7]; // l'oggetto dato in prestito
            this.lend_time = dateLend; // quando va restituito l'oggetto preso in prestito
            this.pickuplocation = strings[9]; // il posto in cui devi andare a prendere qualcosa per un terzo
            this.img = strings[10];
            this.nPart = strings[11];
            this.recType = strings[12];
            this.start_date = strings[13];
            this.end_date = strings[14];
            this.recurrence = strings[15];
        }
        public static boolean conteinService(List<myService> l, myService s){
            System.out.println("ENTER -->"+s.service_id);
            for(myService o: l){
                System.out.println("--> "+o.service_id+" -->"+s.service_id);
                if(o.service_id.equals(s.service_id))
                    return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return service_id + '$' + owner_id + '$' + nome + '$' + descrizione + '$' + location + '$' + pattern + '$' + car_space + '$' + lend_obj + '$' + lend_time + '$' + pickuplocation + '$' + img + '$' + nPart + '$' + recType + '$' + start_date + '$' + end_date + '$' + recurrence;
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

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof myRecEvent) {
                myRecEvent o = (myRecEvent) obj;
                return this.event_id.equals(o.event_id);
            } else {
                return false;
            }
        }

        public final String descrizione;
        public final String enddate;
        public String labels;
        public final String owner_id;
        public final String recType;
        public final String start_date;
        public final String end_date;

        public myRecEvent(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String owner_id, String recType, String start_date, String end_date) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
            this.owner_id = owner_id;
            this.recType = recType;
            this.start_date = start_date;
            this.end_date = end_date;
        }

        //        Creazione dei un evento ricorrente a partire dall'json che mi arriva dal server
        public myRecEvent(JSONObject obj) throws JSONException {
            JSONObject recAct = null;
            if (obj.has("partecipant")) {
                this.nPart = obj.getJSONObject("partecipant").length();
                obj = obj.getJSONArray("event").getJSONObject(0);
            } else {
                this.nPart = 10;
            }
            if (obj.has("RecurringActivity") && !obj.getJSONArray("RecurringActivity").isNull(0)) {
                try {
                    recAct = obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0);
                } catch (JSONException e) {
                    recAct = obj.getJSONArray("RecurringActivity").getJSONObject(0);
                }
                this.nome = recAct.getString("name");
                this.img = recAct.getString("image_url");
                this.descrizione = recAct.getString("description");
                this.owner_id = recAct.getString("creator_id");
                this.labels = recAct.getString("labels");
            } else {
                this.nome = obj.getString("name");
                this.img = obj.getString("image_url");
                this.descrizione = obj.getString("description");
                this.owner_id = obj.getString("creator_id");
                this.labels = obj.getString("labels");
            }
            if (obj.has("Recurrence")) {
                this.recType = obj.getJSONArray("Recurrence").getJSONObject(0).getString("type");
                this.start_date = obj.getJSONArray("Recurrence").getJSONObject(0).getString("start_date");
                this.end_date = obj.getJSONArray("Recurrence").getJSONObject(0).getString("end_date");
                this.event_id = obj.getJSONArray("Recurrence").getJSONObject(0).getString("activity_id");
                this.enddate = ".";
            } else if (obj.has("type")) {
                this.recType = obj.getString("type");
                this.start_date = obj.getString("start_date");
                this.end_date = obj.getString("end_date");
                this.event_id = obj.getString("activity_id");
                this.enddate = ".";
            } else if (obj.has("RecurringActivity") && obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0).has("Recurrence")) {
                this.recType = obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0).getJSONArray("Recurrence").getJSONObject(0).getString("type");
                this.start_date = obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0).getJSONArray("Recurrence").getJSONObject(0).getString("start_date");
                this.end_date = obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0).getJSONArray("Recurrence").getJSONObject(0).getString("end_date");
                this.event_id = obj.getJSONArray("RecurringActivity").getJSONArray(0).getJSONObject(0).getJSONArray("Recurrence").getJSONObject(0).getString("activity_id");
                this.enddate = ".";
            } else {
                this.recType = "";
                this.start_date = "";
                this.end_date = "";
                this.event_id = "";
                this.enddate = ".";
            }
        }

        //        Creazione dell'evento ricorrente a partire da una stringa formattata in questo modo  ...$...$...$..$...
        public myRecEvent(String s) {
            String[] parsed = s.split("\\$");
            this.nome = parsed[0];
            this.event_id = parsed[1];
            this.img = parsed[2];
            this.nPart = Integer.parseInt(parsed[3]);
            this.descrizione = parsed[4];
            this.enddate = parsed[5];
            this.labels = parsed[6];
            this.owner_id = parsed[7];
            this.recType = parsed[8];
            this.start_date = parsed[9];
            this.end_date = parsed[10];
        }

        @Override
        public String toString() {
            return nome + '$' + event_id + '$' + img + '$' + nPart + '$' + descrizione + '$' + enddate + '$' + labels + '$' + owner_id + '$' + recType + '$' + start_date + '$' + end_date;
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

    // Situation è il 'genitore' di evento,evento ricorrente e servizio. Queste attività sono accomunate da questi 3 metodi
    public interface Situation {
        String getName();

        String getImage();

        String getLabels_id();
    }
}
