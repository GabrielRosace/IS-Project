package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class YourEvent extends AppCompatActivity {
    private List<Utilities.Situation> tuoi_eventi = new ArrayList<>();
    private List<Utilities.Situation> partecipi_eventi = new ArrayList<>();
    private List<Utilities.Situation> scaduti_eventi = new ArrayList<>();
    private List<Utilities.Situation> recurrent_event = new ArrayList<>();
    private String id_group;
    private String user_id;

    // metodo che permette di avere i dati aggiornati
    // anche quando si torna indietro dall'activity successiva
    @Override
    protected void onPostResume() {
        super.onPostResume();

        ConstraintLayout pr = (ConstraintLayout) findViewById(R.id.eventi_progress);
        pr.setVisibility(View.VISIBLE);

        tuoi_eventi = new ArrayList<>();
        partecipi_eventi = new ArrayList<>();
        scaduti_eventi = new ArrayList<>();
        recurrent_event = new ArrayList<>();

        String userToken = Utilities.getToken(YourEvent.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        id_group = Utilities.getPrefs(this).getString("group", "");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        TabItem tabYourEvent = (TabItem) findViewById(R.id.your_events);
        TabItem tabActiveEvent = (TabItem) findViewById(R.id.active_events);
        TabItem tabClosedEvent = (TabItem) findViewById(R.id.closed_events);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_archivio);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // caso 1 - eventi che sono tuoi
                    addRecyclerView(tuoi_eventi);
                } else if (tab.getPosition() == 1) {
                    // caso 2 - eventi a cui hai partecipato
                    addRecyclerView(partecipi_eventi);
                } else {
                    // caso 3 - eventi che sono scaduti
                    addRecyclerView(scaduti_eventi);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        this.getEvents();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_event);
    }


    // questo metodo serve per visualizzare le informazioni dell'evento ricorrente
    // usufruisce del filtro expired per vedere gli eventi creati dall'utente ma che non sono scaduti
    public void getRecTuoiAttuali() {
        Utilities.httpRequest(this, Request.Method.GET, "/recurringActivity/creator/" + id_group + "?expired=false", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                System.out.println(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj;
                    try {
                        obj = arr.getJSONObject(i);
                    } catch (JSONException e) {
                        obj = arr.getJSONArray(0).getJSONObject(i);
                    }
                    tuoi_eventi.add(new Utilities.myRecEvent(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }

    // questo metodo serve per visualizzare le informazioni dell'evento ricorrente
    // usufruisce del filtro expired per vedere gli eventi a cui l'utente partecipa ma che non sono scaduti
    public void getRecTuoiScaduti() {
        Utilities.httpRequest(this, Request.Method.GET, "/recurringActivity/creator/" + id_group + "?expired=true", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                for (int i = 0; i < arr.length(); i++) {
                    System.out.println(response);
                    JSONObject obj = arr.getJSONObject(i);
                    scaduti_eventi.add(new Utilities.myRecEvent(obj));
                    this.getRecPartecipaScaduti();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }

    // questo metodo serve per visualizzare le informazioni dell'evento ricorrente
    // usufruisce del filtro expired per vedere gli eventi a cui l'utente partecipa ma che sono scaduti
    public void getRecPartecipaScaduti() {
        Utilities.httpRequest(this, Request.Method.GET, "/recurringActivity/partecipant/" + id_group + "?expired=true", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                System.out.println(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj;
                    try {
                        obj = arr.getJSONObject(i);
                    } catch (JSONException e) {
                        obj = arr.getJSONArray(0).getJSONObject(i);
                    }
                    scaduti_eventi.add(new Utilities.myRecEvent(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }

    // questo metodo serve per visualizzare le informazioni dell'evento ricorrente
    // usufruisce del filtro expired per vedere gli eventi creati dall'utente ma che non sono scaduti
    public void getRecTuoiPartecipa() {
        Utilities.httpRequest(this, Request.Method.GET, "/recurringActivity/partecipant/" + id_group + "?expired=false", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                System.out.println(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    System.out.println(obj);
                    partecipi_eventi.add(new Utilities.myRecEvent(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }


    // questo metodo mi permette di popolare le 3 liste che riguardano
    // gli eventi con possibilità di definizione di etichette dei bambini
    // i seguenti controlli sono stati eseguiti lato front-end per rispettare la retrcompatibilità con il database
    // dell'applicazione originale
    // se io sono il creatore posso avere tre casi :
    //      - sono un repetition==true?  -> tuoi eventi
    //      - altrimenti
    //      - è un evento passato?
    //          - controllo se la data è una stringa vuota da information oppure è prima della data di fine -> tuoi eventi
    //          - altrimenti -> scaduti eventi
    // altrimenti sono un partecipante?
    //      - si, è finito ? -> si -> partecipi eventi -> altrimenti scaduti eventi
    public void getEvents() {
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + id_group + "/activities", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray tmp = new JSONArray(response);
                    YourEvent.this.getRecTuoiAttuali();
                    YourEvent.this.getRecTuoiPartecipa();
                    YourEvent.this.getRecTuoiScaduti();
                    YourEvent.this.getRecPartecipaScaduti();
                    for (int i = 0; i < tmp.length(); ++i) {
                        final String tmp_activity = tmp.getString(i);
                        String id_activity = new JSONObject(tmp.getString(i)).getString("activity_id");
                        Utilities.httpRequest(YourEvent.this, Request.Method.GET, "/groups/" + id_group + "/nekomaActivities/" + id_activity + "/information", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response1) {
                                try {
                                    JSONObject tmp = new JSONObject(response1);

                                    // caso in cui sono il creatore
                                    if (new JSONObject(tmp_activity).getString("creator_id").equals(user_id)) {
                                        if (new JSONObject(tmp_activity).getBoolean("repetition")) {

                                            String name = new JSONObject(tmp_activity).getString("name");
                                            String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                            String id_img = "nan";
                                            if (new JSONObject(tmp_activity).has("image_id")) {
                                                id_img = new JSONObject(tmp_activity).getString("image_id");
                                            }
                                            int nPart = new JSONArray(tmp.getString("parents")).length();
                                            String descrizione = new JSONObject(tmp_activity).getString("description");
                                            String end = "";
                                            String labels = "";
                                            JSONObject tmp_ = new JSONObject(tmp_activity);
                                            if (tmp_.has("labels")) {
                                                JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                if (a.length() > 0) {
                                                    for (int j = 0; j < a.length(); j++) {
                                                        labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                    }
                                                } else {
                                                    labels = ",";
                                                }
                                            } else {
                                                labels = ",";
                                            }
                                            String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                            myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                            tuoi_eventi.add(eve);

                                        } else if (!new JSONObject(tmp_activity).getBoolean("repetition")) {

                                            if (tmp.getString("end").equals("")) {
                                                String name = new JSONObject(tmp_activity).getString("name");
                                                String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                String id_img = "nan";
                                                if (new JSONObject(tmp_activity).has("image_id")) {
                                                    id_img = new JSONObject(tmp_activity).getString("image_id");
                                                }
                                                int nPart = new JSONArray(tmp.getString("parents")).length();
                                                String descrizione = new JSONObject(tmp_activity).getString("description");
                                                String end = "";
                                                String labels = "";
                                                JSONObject tmp_ = new JSONObject(tmp_activity);
                                                if (tmp_.has("labels")) {
                                                    JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                    if (a.length() > 0) {
                                                        for (int j = 0; j < a.length(); j++) {
                                                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                } else {
                                                    labels = ",";
                                                }
                                                String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                tuoi_eventi.add(eve);

                                            } else {

                                                //controllo le date
                                                String date = tmp.getString("end");
                                                Calendar cal = Calendar.getInstance();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                                cal.setTime(sdf.parse(date));

                                                if (Calendar.getInstance().before(cal)) {

                                                    String name = new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if (new JSONObject(tmp_activity).has("image_id")) {
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "";

                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if (tmp_.has("labels")) {
                                                        JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                        if (a.length() > 0) {
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        } else {
                                                            labels = ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                    myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                    tuoi_eventi.add(eve);

                                                } else {

                                                    String name = new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if (new JSONObject(tmp_activity).has("image_id")) {
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if (tmp_.has("labels")) {
                                                        JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                        if (a.length() > 0) {
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        } else {
                                                            labels = ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                    myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                    scaduti_eventi.add(eve);
                                                }
                                            }
                                        }
                                    } else {

                                        JSONArray Part = new JSONArray(tmp.getString("parents"));
                                        Boolean find = false;
                                        for (int i = 0; i < Part.length(); ++i) {
                                            System.out.println(Part.getString(i));
                                            if (Part.getString(i).equals(user_id)) {
                                                find = true;
                                            }
                                        }

                                        if (find == true) {

                                            if (tmp.getString("end").equals("")) {

                                                String name = new JSONObject(tmp_activity).getString("name");
                                                String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                String id_img = "nan";
                                                if (new JSONObject(tmp_activity).has("image_id")) {
                                                    id_img = new JSONObject(tmp_activity).getString("image_id");
                                                }
                                                int nPart = new JSONArray(tmp.getString("parents")).length();
                                                String descrizione = new JSONObject(tmp_activity).getString("description");
                                                String end = "";
                                                String labels = "";
                                                JSONObject tmp_ = new JSONObject(tmp_activity);
                                                if (tmp_.has("labels")) {
                                                    JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                    if (a.length() > 0) {
                                                        for (int j = 0; j < a.length(); j++) {
                                                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                } else {
                                                    labels = ",";
                                                }
                                                String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                tuoi_eventi.add(eve);

                                            } else {

                                                String date = tmp.getString("end");
                                                Calendar cal = Calendar.getInstance();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                                cal.setTime(sdf.parse(date));

                                                if (Calendar.getInstance().before(cal)) {

                                                    String name = new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if (new JSONObject(tmp_activity).has("image_id")) {
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if (tmp_.has("labels")) {
                                                        JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                        if (a.length() > 0) {
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        } else {
                                                            labels = ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                    myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                    partecipi_eventi.add(eve);
                                                } else {

                                                    String name = new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if (new JSONObject(tmp_activity).has("image_id")) {
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if (tmp_.has("labels")) {
                                                        JSONArray a = new JSONArray(tmp_.getString("labels"));
                                                        if (a.length() > 0) {
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        } else {
                                                            labels = ",";
                                                        }
                                                    } else {
                                                        labels = ",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");

                                                    myEventi eve = new myEventi(name, id_img, id_activity, nPart, descrizione, end, labels, ownerid);
                                                    scaduti_eventi.add(eve);

                                                }
                                            }
                                        }
                                    }
                                    if (tuoi_eventi.size() > 0) {
                                        addRecyclerView(tuoi_eventi);
                                    }
                                    ConstraintLayout pr = (ConstraintLayout) findViewById(R.id.eventi_progress);
                                    pr.setVisibility(View.GONE);
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(YourEvent.this, "ERRORE", Toast.LENGTH_SHORT).show();
                            }
                        }, new HashMap<>());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YourEvent.this, "ERRORE", Toast.LENGTH_SHORT).show();
            }
        }, new HashMap<>());
    }

    // metodo che popola la recycle view
    private void addRecyclerView(List<Utilities.Situation> list) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lista_eventi);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    // recycle view per gli eventi con etichette e per gli eventi ricorrenti
    // grazie al principio di ereditarietà, i due oggetti: myEventi e Utilities.myRecEve estendendo
    // entrambe Utilities.Situation, possono usare la stessa RecyclerView
    // ciò non sarebbe stato possibile altrimenti
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Utilities.Situation> mData;
        private LayoutInflater mInflater;


        MyRecyclerViewAdapter(Context context, List<Utilities.Situation> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_2, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {

            Utilities.Situation event = mData.get(position);
            holder.btn.setText(event.getName());

            if (event instanceof Utilities.myRecEvent) {
                new ImageDownloader(holder.myImageView).execute(event.getImage());
                System.out.println(event.getClass());
                System.out.println(event.getName());
                holder.btn.setBackgroundColor(getResources().getColor(R.color.recurrent_event, getTheme()));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent evento = new Intent(YourEvent.this, DettagliEventoRicorrente.class);
                        // System.out.println("intent: "+event.toString());
                        evento.putExtra("evento", event.toString());
                        startActivity(evento);
                    }
                });
            } else {
                System.out.println(event.getImage());

                if (event.getImage().equals("nan")) { // Non è stata specificata nessun immagine, ne setto una di default
                    System.out.println("qui dentro**************************");
                    holder.myImageView.setImageDrawable(getDrawable(R.drawable.persone));
                } else { // Scarico l'immagine dal server e la aggiungo alla view
                    System.out.println("******************Secondo");
                    Utilities.httpRequest(YourEvent.this, Request.Method.GET, "/image/" + event.getImage(), response -> {
                        String url = "";
                        try {
                            JSONObject obj = new JSONObject((String) response);
                            if (obj.has("url") && !obj.getString("url").equals("image.url")) {
                                if (obj.getString("url").contains(".svg")) {
                                    url = getString(R.string.urlnoapi) + obj.getString("path");
                                } else {
                                    url = obj.getString("url");

                                }
                            } else {
                                url = getString(R.string.urlnoapi) + obj.getString("path");
                            }
                            new ImageDownloader(holder.myImageView).execute(url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, System.err::println, new HashMap<>());
                }

                holder.btn.setBackgroundColor(getResources().getColor(R.color.purple_500, getTheme()));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent evento = new Intent(YourEvent.this, DettagliEvento.class);
                        evento.putExtra("evento", event.toString());
                        startActivity(evento);
                    }
                });
            }


        }


        @Override
        public int getItemCount() {
            return mData.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView myImageView;
            Button btn;

            ViewHolder(View itemView) {
                super(itemView);
                myImageView = itemView.findViewById(R.id.myrecycle_view_img);
                btn = itemView.findViewById(R.id.name_event);
            }
        }


        Utilities.Situation getItem(int id) {
            return mData.get(id);
        }

    }

    // Classe per il download delle immagini
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView holder;

        public ImageDownloader(ImageView holder) {
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlOfImage = strings[0];
            Bitmap logo = null;
            try {
                //todo sistemare
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holder.setImageBitmap(bitmap);
        }
    }

    // La classe myEventi implementa Utilities.Situation
    // è necessaria per fornire un codice scalabile
    private class myEventi implements Utilities.Situation {
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public final String labels;
        public final String owner_id;

        public myEventi(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String owner_id) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
            this.owner_id = owner_id;
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
}