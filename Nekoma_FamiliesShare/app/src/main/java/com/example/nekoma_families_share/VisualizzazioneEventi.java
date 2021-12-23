package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisualizzazioneEventi extends AppCompatActivity {

    private String userid;
    private String groupid;
    private List<Utilities.Situation> activities = null;
    private List<String> child_pref = null;
    public ConstraintLayout progress_layout;
    public ProgressBar progress_bar;

    // Permette di ricaricare le informazioni una volta che si torna indietro dall'activity successiva
    @Override
    protected void onPostResume() {
        super.onPostResume();

        userid = Utilities.getUserID(this);
        groupid = Utilities.getPrefs(this).getString("group", "");

        Toolbar t = (Toolbar) findViewById(R.id.toolbar6);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Inizializzo i filtri per quanto riguarda la visualizzazione degli eventi
        ChipGroup chipGroup = (ChipGroup) findViewById(R.id.chipgroup);

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId == R.id.attività) {
                    addRecyclerView(activities);
                } else if (checkedId == R.id.servPersona) {
                    getServices("pickup");
                } else if (checkedId == R.id.consigliate) {
                    List<Utilities.Situation> rec = getRecommendedActivities();
                    for (String label:child_pref) {
                        Utilities.httpRequest(VisualizzazioneEventi.this, Request.Method.GET, "/recurringActivity/"+label, response -> {
                            try{
                                JSONArray arr = new JSONArray((String) response);
                                for (int i = 0; i < arr.length(); i++) {
                                    System.out.println(arr.getJSONObject(i)); //TODO non so se funzia
                                    Utilities.myRecEvent eve = new Utilities.myRecEvent(arr.getJSONObject(i));
                                    if(!rec.contains(eve)){
                                        rec.add(eve);
                                    }
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }, System.err::println, new HashMap<>());
                    }
                    addRecyclerView(rec);
                } else if (checkedId == R.id.servRicorrenti) {
                    getRecurring();
                } else if (checkedId == R.id.prestito) {
                    getServices("lend");
                } else if (checkedId == R.id.carsharing) {
                    getServices("car");
                }
            }
        });

        // Aggiungo i dati alla view
        getActivities(() -> {
            addRecyclerView(activities);
        });
        getMyChildPref();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizzazione_eventi);
        // Schermata di caricamento
        progress_layout = (ConstraintLayout) findViewById(R.id.progress_layout);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    // Ottengo gli interessi dei figli del visualizzatore della view
    private void getMyChildPref() {
        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        String my_id = Utilities.getUserID(this);
        child_pref = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/users/" + my_id + "/children", response -> {
            try {
                JSONArray arr = new JSONArray(response.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject child = new JSONObject(arr.getString(i)).getJSONObject("child");
                    if (child.has("labels")) { // Parsing child label
                        JSONArray labs = child.getJSONArray("labels");
                        for (int j = 0; j < labs.length(); j++) {
                            if (labs.get(j) != null) {
                                child_pref.add(labs.getString(j));
                            }
                        }
                    }
                }
//                progress_layout.setVisibility(View.GONE);
//                progress_bar.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        }, new HashMap<>());
    }

    // Ottengo tutte le attività, le salvo nella struttura e infine le aggiungo alla recycle view
    private void getActivities(Runnable r) {
        activities = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + groupid + "/activities", reason -> {
            try {
                JSONArray array = new JSONArray((String) reason);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject e = new JSONObject(array.get(i).toString());

                    String labels = "";
                    String labels_ids = "";

                    if (e.has("labels")) {
                        JSONArray a = new JSONArray(e.getString("labels"));
                        for (int j = 0; j < a.length(); j++) {
                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                            labels_ids += new JSONObject(a.get(j).toString()).getString("label_id") + ",";
                        }
                    }

                    String img = "nan";
                    if (e.has("image_id")) {
                        img = e.getString("image_id");
                    }

                    activities.add(new Evento(e.getString("name"), img, e.getString("activity_id"), 10 /*TODO*/, e.getString("description"), "TODO", labels, labels_ids, e.getString("creator_id")));
                }

                Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/services?filterBy=recurrent", response -> {
                    try {
                        JSONArray arr = new JSONArray((String) response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            activities.add(new Utilities.myRecEvent(obj));
                        }
                        r.run();
                        progress_bar.setVisibility(View.GONE);
                        progress_layout.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },System.out::println, new HashMap<>());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, new String(error.networkResponse.data), Toast.LENGTH_SHORT).show();
        }, new HashMap<>());
    }

    private void getServices(String pattern) {
        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        List<Utilities.Situation> activities = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/service?pattern="+pattern, response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                for (int i = 0; i < arr.length(); i++) {
                    activities.add(new Utilities.myService(arr.getJSONObject(i)));
                }
                addRecyclerView(activities);
                progress_layout.setVisibility(View.GONE);
                progress_bar.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }

    private void getRecurring() {
        List<Utilities.Situation> services = new ArrayList<>();

        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);

        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/services?filterBy=recurrent", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    services.add(new Utilities.myRecEvent(obj));
                }

                Utilities.httpRequest(VisualizzazioneEventi.this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/service", response1 -> {
                    try {
                        JSONArray arr1 = new JSONArray((String) response1);
                        for (int j = 0; j < arr1.length(); j++) {
                            JSONObject obj1 = arr1.getJSONObject(j);
                            services.add(new Utilities.myService(obj1));
                        }
                        addRecyclerView(services);
                        progress_layout.setVisibility(View.GONE);
                        progress_bar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, System.out::println, new HashMap<>());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }

    // Ottengo le attività che possono interessare ai figli
    private List<Utilities.Situation> getRecommendedActivities() {
        List<Utilities.Situation> recommendedActivities = new ArrayList<>();

        if (child_pref != null) {
            for (Utilities.Situation a : activities) {
                String[] lab = a.getLabels_id().split(",");
                for (String l : lab) {
                    if (l != "") {
                        if (child_pref.contains(l) && !recommendedActivities.contains(a)) {
                            recommendedActivities.add(a);
                        }
                    }
                }
            }
        } else {
            recommendedActivities = new ArrayList<>(activities);
        }
        if (recommendedActivities.isEmpty()) {
            Toast.makeText(this, "Non ci sono eventi consigliati...", Toast.LENGTH_SHORT).show();
        }
        return recommendedActivities;
    }


    private void addRecyclerView(List<Utilities.Situation> list) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activities_recycle_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VisualizzazioneEventi.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(VisualizzazioneEventi.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


//    public void goBack(View v) {
//        Intent homepage = new Intent(VisualizzazioneEventi.this, Homepage.class);
//        startActivity(homepage);
//    }

    // Classe per la gestione della recycle view
    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Utilities.Situation> eventoList;
        private LayoutInflater mInflater;

        public MyRecyclerViewAdapter(Context context, List<Utilities.Situation> eventoList) {
            this.eventoList = eventoList;
            this.mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_visualizzazione_eventi, parent, false);
            return new VisualizzazioneEventi.MyRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Utilities.Situation eve = eventoList.get(position);
            holder.tv.setText(eve.getName());
            holder.btn.setText("Info");


            if (eve instanceof Evento) {
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent evento = new Intent(VisualizzazioneEventi.this, DettagliEvento.class);
                        evento.putExtra("evento", eve.toString());
                        startActivity(evento);
                    }
                });
            } else if (eve instanceof Utilities.myRecEvent) {
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent evento = new Intent(VisualizzazioneEventi.this, DettagliEventoRicorrente.class);
                        evento.putExtra("evento", eve.toString());
                        startActivity(evento);
                    }
                });
            } else if (eve instanceof Utilities.myService) {
//                TODO
//                Intent evento = new Intent(VisualizzazioneEventi.this, DettagliEventoRicorrente.class);
//                evento.putExtra("servizio", eve.toString());
//                startActivity(evento);

//                Toast.makeText(VisualizzazioneEventi.this, "Chiedi ad alberto di farlo", Toast.LENGTH_SHORT).show();
            }

            // Se è presente scarico l'immagine e la aggiungo, altrimenti uso una di default
            if (eve.getImage().equals("nan")) {
                holder.img.setImageDrawable(getDrawable(R.drawable.persone));
            } else if (eve instanceof Utilities.myService || eve instanceof Utilities.myRecEvent) {
                new ImageDownloader(holder.img).execute(eve.getImage());
            } else {
                Utilities.httpRequest(VisualizzazioneEventi.this, Request.Method.GET, "/image/" + eve.getImage(), response -> {
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
                        new ImageDownloader(holder.img).execute(url);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    System.err.println(error);
                }, new HashMap<>());

            }
        }

        @Override
        public int getItemCount() {
            return eventoList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            Button btn;
            ImageView img;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.tv = itemView.findViewById(R.id.recycle_view_text);
                this.btn = itemView.findViewById(R.id.recycle_view_btn);
                this.img = itemView.findViewById(R.id.recycle_view_img);
            }
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

    // Model di evento
    public static class Evento implements Utilities.Situation {
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public String labels;
        public final String labels_ids;
        public final String owner_id;

        public Evento(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String labels_ids, String owner_id) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
            this.labels_ids = labels_ids;
            this.owner_id = owner_id;
        }

        public Evento(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String owner_id) {
            this(nome, img, event_id, nPart, descrizione, enddate, labels, null, owner_id);
        }

        // Parsing della string in Evento
        public static Evento getEventoFromString(String toParse) {
            String[] parsed = toParse.split("/");

            if (parsed.length <= 6) {
                return new Evento(parsed[0], parsed[2], parsed[1], Integer.parseInt(parsed[3]), parsed[4], parsed[5], "", parsed[6]);
            }
            return new Evento(parsed[0], parsed[2], parsed[1], Integer.parseInt(parsed[3]), parsed[4], parsed[5], parsed[6], parsed[7]);
        }

        // Trasformo da evento a stringa così da poter inviare il dato tramite intent
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
            return this.labels_ids;
        }
    }


}