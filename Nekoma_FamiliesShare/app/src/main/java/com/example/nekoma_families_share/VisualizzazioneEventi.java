package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizzazione_eventi);
        userid = Utilities.getUserID(this);
        groupid = Utilities.getPrefs(this).getString("group","");


        ChipGroup chipGroup = (ChipGroup)findViewById(R.id.chipgroup);

        //ChipGroup.OnCheckedChangeListener()
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if(checkedId == R.id.attività){
                    getActivities();
                }else if(checkedId == R.id.servPersona){
                    getServPerson();
                }else{
                    getRecommendedActivities();
                }
            }
        });
        getActivities();
    }

    private void getActivities(){
        List<Evento> activities = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/groups/"+groupid+"/activities", reason -> {
            try {
                JSONArray array = new JSONArray((String) reason);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject e = new JSONObject(array.get(i).toString());
                    activities.add(new Evento(e.getString("name"), "manca", e.getString("activity_id"), 10, e.getString("description")));
                }
                addRecyclerView(activities);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
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
        addRecyclerView(activities);
    }


    private void getRecommendedActivities(){ // TODO not implemented yet
        addRecyclerView(new ArrayList<>());
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
                    System.out.println("intent: "+eve.toString());
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


    public class Evento{
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;

        public Evento(String nome, String img, String event_id, int nPart, String descrizione) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
        }

        @Override
        public String toString() {
            return nome+'/'+event_id+'/'+img+'/'+nPart+'/'+descrizione;
        }
    }

}