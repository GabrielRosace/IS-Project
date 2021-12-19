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

public class YourEvent extends AppCompatActivity {
    private List<myEventi> tuoi_eventi = new ArrayList<>();
    private List<myEventi> partecipi_eventi = new ArrayList<>();
    private List<myEventi> scaduti_eventi = new ArrayList<>();
    private List<Utilities.myRecEvent> recurrent_event = new ArrayList<>();
    private String id_group;
    private String user_id;

    // metodo che permette di avere i dati aggiornati
    // anche quando si torna indietro dall'activity successiva
    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        }catch(JSONException e){
            e.printStackTrace();
        }
        id_group = Utilities.getPrefs(this).getString("group","");

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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    // caso 1 - eventi che sono tuoi
                    addRecyclerView(tuoi_eventi, new ArrayList<>());
                }else if(tab.getPosition()==1){
                    // caso 2 - eventi a cui hai partecipato
                    addRecyclerView(partecipi_eventi, new ArrayList<>());
                }else if(tab.getPosition()==2){
                    // caso 3 - eventi che sono scaduti
                    addRecyclerView(scaduti_eventi, new ArrayList<>());
                }else{
                    addRecyclerView(new ArrayList<>(), recurrent_event);
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
        this.getRec();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_event);
    }


    // questo metodo serve per visualizzare le informazioni dell'evento ricorrente
    public void getRec(){
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/services?filterBy=recurrent", response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    recurrent_event.add(new Utilities.myRecEvent(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.out::println, new HashMap<>());
    }


    // questo metodo mi permette di popolare le 3 liste che riguardano
    // gli eventi con possibilità di definizione di etichette dei bambini
    public void getEvents(){
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + id_group + "/activities", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for(int i=0;i<tmp.length();++i){
                        final String tmp_activity = tmp.getString(i);
                        String id_activity= new JSONObject(tmp.getString(i)).getString("activity_id");
                        Utilities.httpRequest(YourEvent.this, Request.Method.GET, "/groups/" + id_group + "/nekomaActivities/" + id_activity +"/information", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response1) {
                                try {
                                    JSONObject tmp = new JSONObject(response1);

                                    // caso in cui sono il creatore
                                    if(new JSONObject(tmp_activity).getString("creator_id").equals(user_id)){
                                        //se io sono il creatore posso avere tre casi :
                                        //      - sono un repetition==true?  -> tuoi eventi (fatto)
                                        //      - altrimenti
                                        //      - è un evento passato?
                                        //          - controllo se la data è una stringa vuota da information oppure è prima della data di fine -> tuoi eventi (fatto)
                                        //          - altrimenti -> scaduti eventi (fatto)
                                        // altrimenti sono un partecipante?
                                        //      - si, è finito ? -> si -> partecipi eventi -> altrimenti scaduti eventi
                                        // System.out.println("primo caso");
                                        if(new JSONObject(tmp_activity).getBoolean("repetition")){
                                            String name= new JSONObject(tmp_activity).getString("name");
                                            String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                            String id_img = "nan";
                                            if(new JSONObject(tmp_activity).has("image_id")){
                                                id_img = new JSONObject(tmp_activity).getString("image_id");
                                            }
                                            int nPart = new JSONArray(tmp.getString("parents")).length();
                                            String descrizione = new JSONObject(tmp_activity).getString("description");
                                            String end = "";
                                            String labels = "";
                                            JSONObject tmp_ = new JSONObject(tmp_activity);
                                            if(tmp_.has("labels")){
                                                JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                if(a.length()>0){
                                                    for (int j = 0; j < a.length(); j++) {
                                                        labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                    }
                                                }else{
                                                    labels =",";
                                                }
                                            }else{
                                                labels=",";
                                            }
                                            String ownerid = new JSONObject(tmp_activity).getString("creator_id");
                                            myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels,ownerid);
                                            tuoi_eventi.add(eve);
                                        }else if(!new JSONObject(tmp_activity).getBoolean("repetition")){

                                            if(tmp.getString("end").equals("")){
                                                String name= new JSONObject(tmp_activity).getString("name");
                                                String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                String id_img = "nan";
                                                if(new JSONObject(tmp_activity).has("image_id")){
                                                    id_img = new JSONObject(tmp_activity).getString("image_id");
                                                }
                                                int nPart = new JSONArray(tmp.getString("parents")).length();
                                                String descrizione = new JSONObject(tmp_activity).getString("description");
                                                String end = "";
                                                String labels = "";
                                                JSONObject tmp_ = new JSONObject(tmp_activity);
                                                if(tmp_.has("labels")){
                                                    JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                    if(a.length()>0){
                                                        for (int j = 0; j < a.length(); j++) {
                                                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                        }
                                                    }else{
                                                        labels =",";
                                                    }
                                                }else{
                                                    labels=",";
                                                }
                                                String ownerid = new JSONObject(tmp_activity).getString("creator_id");
                                                myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels, ownerid);
                                                tuoi_eventi.add(eve);
                                            }else{
                                                String date =tmp.getString("end");
                                                Calendar cal = Calendar.getInstance();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                                cal.setTime(sdf.parse(date));
                                                if(Calendar.getInstance().before(cal)){
                                                    String name= new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if(new JSONObject(tmp_activity).has("image_id")){
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "todo";

                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if(tmp_.has("labels")){
                                                        JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                        if(a.length()>0){
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        }else{
                                                            labels =",";
                                                        }
                                                    }else{
                                                        labels=",";
                                                    }
                                                    String ownerid= new JSONObject(tmp_activity).getString("creator_id");
                                                    myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels, ownerid);
                                                    tuoi_eventi.add(eve);
                                                }else{
                                                    String name= new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if(new JSONObject(tmp_activity).has("image_id")){
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "todo";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if(tmp_.has("labels")){
                                                        JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                        if(a.length()>0){
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        }else{
                                                            labels =",";
                                                        }
                                                    }else{
                                                        labels=",";
                                                    }
                                                    String ownerid= new JSONObject(tmp_activity).getString("creator_id");
                                                    myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels,ownerid);
                                                    scaduti_eventi.add(eve);
                                                }
                                            }
                                        }
                                    }else{
                                        JSONArray  Part = new JSONArray(tmp.getString("parents"));
                                        Boolean find = false;
                                        for (int i=0;i<Part.length();++i){
                                            System.out.println(Part.getString(i));
                                            if(Part.getString(i).equals(user_id)){
                                                find = true;
                                            }
                                        }
                                        if(find==true){
                                            if(tmp.getString("end").equals("") /*quando sistemato da togliere*/){
                                                String name= new JSONObject(tmp_activity).getString("name");
                                                String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                String id_img = "nan";
                                                if(new JSONObject(tmp_activity).has("image_id")){
                                                    id_img = new JSONObject(tmp_activity).getString("image_id");
                                                }
                                                int nPart = new JSONArray(tmp.getString("parents")).length();
                                                String descrizione = new JSONObject(tmp_activity).getString("description");
                                                String end = "";
                                                String labels = "";
                                                JSONObject tmp_ = new JSONObject(tmp_activity);
                                                if(tmp_.has("labels")){
                                                    JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                    if(a.length()>0){
                                                        for (int j = 0; j < a.length(); j++) {
                                                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                        }
                                                    }else{
                                                        labels =",";
                                                    }
                                                }else{
                                                    labels=",";
                                                }
                                                String ownerid= new JSONObject(tmp_activity).getString("creator_id");
                                                myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels,ownerid);
                                                tuoi_eventi.add(eve);
                                            }else{
                                                String date =tmp.getString("end");
                                                Calendar cal = Calendar.getInstance();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                                cal.setTime(sdf.parse(date));
                                                if(Calendar.getInstance().before(cal)){
                                                    String name= new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if(new JSONObject(tmp_activity).has("image_id")){
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "todo";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if(tmp_.has("labels")){
                                                        JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                        if(a.length()>0){
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        }else{
                                                            labels =",";
                                                        }
                                                    }else{
                                                        labels=",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");
                                                    myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels,ownerid);
                                                    partecipi_eventi.add(eve);
                                                }else{
                                                    String name= new JSONObject(tmp_activity).getString("name");
                                                    String id_activity = new JSONObject(tmp_activity).getString("activity_id");
                                                    String id_img = "nan";
                                                    if(new JSONObject(tmp_activity).has("image_id")){
                                                        id_img = new JSONObject(tmp_activity).getString("image_id");
                                                    }
                                                    int nPart = new JSONArray(tmp.getString("parents")).length();
                                                    String descrizione = new JSONObject(tmp_activity).getString("description");
                                                    String end = "todo";
                                                    String labels = "";
                                                    JSONObject tmp_ = new JSONObject(tmp_activity);
                                                    if(tmp_.has("labels")){
                                                        JSONArray a= new JSONArray(tmp_.getString("labels"));
                                                        if(a.length()>0){
                                                            for (int j = 0; j < a.length(); j++) {
                                                                labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                                                            }
                                                        }else{
                                                            labels =",";
                                                        }
                                                    }else{
                                                        labels=",";
                                                    }
                                                    String ownerid = new JSONObject(tmp_activity).getString("creator_id");
                                                    myEventi eve = new myEventi(name,id_img,id_activity,nPart,descrizione,end,labels, ownerid);
                                                    scaduti_eventi.add(eve);
                                                }
                                            }
                                        }
                                    }
                                    ConstraintLayout pr = (ConstraintLayout) findViewById(R.id.eventi_progress);
                                    pr.setVisibility(View.GONE);
                                    if(tuoi_eventi.size()>0){
                                        addRecyclerView(tuoi_eventi, new ArrayList<>());
                                    }
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(YourEvent.this, "ERRORE", Toast.LENGTH_SHORT).show();
                                // System.err.println(error.toString());
                            }
                        }, new HashMap<>());
                    }

                }catch(JSONException e){
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
    private void addRecyclerView(List<myEventi> list, List<Utilities.myRecEvent> reclist){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lista_eventi);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list, reclist);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    // recycle view per gli eventi normali e per gli eventi ricorrenti in base a cosa viene passato nella add
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Utilities.myRecEvent> recEve;
        private List<myEventi> mData;
        private LayoutInflater mInflater;


        MyRecyclerViewAdapter(Context context, List<myEventi> data, List<Utilities.myRecEvent> recEve) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.recEve = recEve;
        }


        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_2, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            if(this.mData.size()>0){
                myEventi event = mData.get(position);
                // System.out.println(" ************************* questo è event: "+event.toString());
                holder.btn.setText(event.nome);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent evento = new Intent(YourEvent.this, DettagliEvento.class);
                        // System.out.println("intent: "+event.toString());
                        evento.putExtra("evento", event.toString());
                        startActivity(evento);
                    }
                });
            }else {
                Utilities.myRecEvent event = recEve.get(position);
                // System.out.println(" ************************* questo è event: "+event.toString());
                holder.btn.setText(event.nome);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent evento = new Intent(YourEvent.this, DettagliEventoRicorrente.class);
                        // System.out.println("intent: "+event.toString());
                        evento.putExtra("evento", event.toString());
                        startActivity(evento);
                    }
                });
            }


        }


        @Override
        public int getItemCount() {
            return mData.size() + recEve.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            Button btn;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.info);
                btn = itemView.findViewById(R.id.name_event);
            }
        }


        myEventi getItem(int id) {
            return mData.get(id);
        }
        Utilities.myRecEvent getItemRec(int id){
            return recEve.get(id);
        }

    }
    // recycle view

    // permette di usare gli eventi in modo più semplice
    private class  myEventi{
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
            return nome+'/'+event_id+'/'+img+'/'+nPart+'/'+descrizione+'/'+enddate+'/'+labels+'/'+owner_id;
        }
    }
}