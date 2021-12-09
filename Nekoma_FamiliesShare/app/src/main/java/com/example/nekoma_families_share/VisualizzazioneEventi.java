package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisualizzazioneEventi extends AppCompatActivity {

    private String userid;
    private String groupid;
    private List<Evento> activities = null;
    private List<String> child_pref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizzazione_eventi);
        userid = Utilities.getUserID(this);
        groupid = Utilities.getPrefs(this).getString("group","");

        Toolbar t = (Toolbar) findViewById(R.id.toolbar6);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ChipGroup chipGroup = (ChipGroup)findViewById(R.id.chipgroup);

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if(checkedId == R.id.attività){
                    addRecyclerView(activities);
                }else if(checkedId == R.id.servPersona){
                    getServPerson();
                }else{
                    addRecyclerView(getRecommendedActivities());
                }
            }
        });
        getActivities(()->{
            addRecyclerView(activities);
        });
        getMyChildPref();
    }

    private void getMyChildPref(){
        String my_id = Utilities.getUserID(this);
        child_pref = new ArrayList<>();
        Utilities.httpRequest(this,Request.Method.GET, "/users/"+my_id+"/children", response -> {
            try {
                JSONArray arr = new JSONArray(response.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject child = new JSONObject(arr.getString(i)).getJSONObject("child");
                    if(child.has("labels")){ // Parsing child label
                        JSONArray labs = child.getJSONArray("labels");
                        for (int j = 0; j < labs.length(); j++) {
                            if(labs.get(j) != null){
                                child_pref.add(labs.getString(j));
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {}, new HashMap<>());
    }

    private void getActivities(Runnable r){
        activities = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/groups/"+groupid+"/activities", reason -> {
            try {
                JSONArray array = new JSONArray((String) reason);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject e = new JSONObject(array.get(i).toString());

                    String labels = "";
                    String labels_ids = "";

                    if(e.has("labels")){
                        JSONArray a = new JSONArray(e.getString("labels"));
                        for (int j = 0; j < a.length(); j++) {
                            labels += new JSONObject(a.get(j).toString()).getString("name") + ",";
                            labels_ids += new JSONObject(a.get(j).toString()).getString("label_id") + ",";
                        }
                    }
//                    System.out.println(e.getString("creator_id"));

                    activities.add(new Evento(e.getString("name"), "TODO", e.getString("activity_id"), 10 /*TODO*/, e.getString("description"), "TODO", labels,labels_ids,e.getString("creator_id")));
                }

                r.run();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, new String(error.networkResponse.data), Toast.LENGTH_SHORT).show();
        }, new HashMap<>());
    }

    private void getServPerson(){ //TODO not implemented yet
        List<Evento> activities = new ArrayList<>();
        /*Utilities.httpRequest(this, Request.Method.GET, "/groups/"+groupid+"/activities", reason -> {
            try {
                JSONArray array = new JSONArray((String) reason);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject e = new JSONObject(array.get(i).toString());
                    activities.add(new Evento(e.getString("name"), "manca", e.getString("activity_id")));
                    addRecyclerView(activities);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
        }, new HashMap<>());*/
        Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        addRecyclerView(activities);
    }


    private List<Evento> getRecommendedActivities(){
        List<Evento> recommendedActivities = new ArrayList<>();
        if(child_pref != null){
            for (Evento a:activities) {
                String[] lab = a.labels_ids.split(",");
                for (String l:lab) {
                    if(l!=""){
                        if(child_pref.contains(l)){
                            recommendedActivities.add(a);
                        }
                    }
                }
            }
        }else{
            recommendedActivities = new ArrayList<>(activities);
        }
        if(recommendedActivities.isEmpty()){
            Toast.makeText(this, "Non ci sono eventi consigliati...", Toast.LENGTH_SHORT).show();
        }
        return recommendedActivities;
    }




    private void addRecyclerView(List<Evento> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activities_recycle_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VisualizzazioneEventi.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(VisualizzazioneEventi.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    public void goBack(View v){
        Intent homepage = new Intent(VisualizzazioneEventi.this, Homepage.class);
        startActivity(homepage);
    }



    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

        private List<Evento> eventoList;
        private LayoutInflater mInflater;

        public MyRecyclerViewAdapter(Context context, List<Evento> eventoList) {
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
            final Evento eve = eventoList.get(position);
            holder.tv.setText(eve.nome);
            holder.btn.setText("Info");
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent evento = new Intent(VisualizzazioneEventi.this, DettagliEvento.class);
//                    System.out.println("intent: "+eve.toString());
                    evento.putExtra("evento", eve.toString());
                    startActivity(evento);
                }
            });
            holder.img.setImageDrawable(getDrawable(R.drawable.persone)); //TODO cambia immagine
        }

        @Override
        public int getItemCount() {
            return eventoList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{
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


    public static class Evento{
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public final String labels;
        public final String labels_ids;
        public final String owner_id;

        public Evento(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels, String labels_ids,String owner_id) {
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

        public Evento(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels,String owner_id){
//            this.nome = nome;
//            this.img = img;
//            this.event_id = event_id;
//            this.nPart = nPart;
//            this.descrizione = descrizione;
//            this.enddate = enddate;
//            this.labels = labels;
//            this.labels_ids = null;
            this(nome,img,event_id,nPart,descrizione,enddate,labels,null, owner_id);
        }

        public static Evento getEventoFromString(String toParse){
            String[] parsed = toParse.split("/");
//            System.out.println(parsed.length);
            if(parsed.length<=6){ //? Forse 7?
                return new Evento(parsed[0],parsed[2],parsed[1],Integer.parseInt(parsed[3]),parsed[4],parsed[5],"",parsed[6]);
            }
            return new Evento(parsed[0],parsed[2],parsed[1],Integer.parseInt(parsed[3]),parsed[4],parsed[5],parsed[6],parsed[7]);
        }

        @Override
        public String toString() {
            return nome+'/'+event_id+'/'+img+'/'+nPart+'/'+descrizione+'/'+enddate+'/'+labels+'/'+owner_id;
        }
    }

}