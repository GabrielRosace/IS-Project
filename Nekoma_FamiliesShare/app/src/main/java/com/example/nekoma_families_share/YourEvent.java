package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java.util.HashMap;
import java.util.List;

public class YourEvent extends AppCompatActivity {
    private List<myEventi> tuoi_eventi = null;
    private List<myEventi> partecipi_eventi = null;
    private List<myEventi> scaduti_eventi = null;
    private String id_group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_event);

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
                }else if(tab.getPosition()==1){
                    // caso 2 - eventi a cui hai partecipato
                }else{
                    // caso 3 - eventi che sono scaduti
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

    public void getEvents(){
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + id_group + "/activities", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    System.out.println("**********"+response);
                    JSONArray tmp = new JSONArray(response);
                    for(int i=0;i<tmp.length();++i){
                        final String tmp_activity = tmp.getString(i);
                        String id_activity= new JSONObject(tmp.getString(i)).getString("activity_id");
                        Utilities.httpRequest(YourEvent.this, Request.Method.GET, "/groups/" + id_group + "/nekomaActivities/" + id_activity +"/information", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response1) {
                                try {
                                    System.out.println("** sono le info dell'activity :"+new JSONObject(tmp_activity).getString("name") + "e le info sono " + response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.err.println(error.toString());
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
                System.err.println(error.toString());
            }
        }, new HashMap<>());
    }

    private void addRecyclerView(List<myEventi> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lista_eventi);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // recicle view
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<myEventi> mData;

        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<myEventi> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_2, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            myEventi event = mData.get(position);
            holder.btn.setText(event.nome);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent evento = new Intent(YourEvent.this, DettagliEvento.class);
                    System.out.println("intent: "+event.toString());
                    evento.putExtra("evento", event.toString());
                    startActivity(evento);
                }
            });

        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            Button btn;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.info);
                btn = itemView.findViewById(R.id.recycle_view_btn);
            }
        }

        // convenience method for getting data at click position
        myEventi getItem(int id) {
            return mData.get(id);
        }

    }
    // recicle view


    private class  myEventi{
        public final String nome;
        public final String event_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public final String labels;

        public myEventi(String nome, String img, String event_id, int nPart, String descrizione, String enddate, String labels) {
            this.nome = nome;
            this.img = img;
            this.event_id = event_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
        }

        @Override
        public String toString() {
            return nome+'/'+event_id+'/'+img+'/'+nPart+'/'+descrizione+'/'+enddate+'/'+labels;
        }
    }


    public void getHomePage(View v){
        Intent homepage = new Intent(YourEvent.this,Homepage.class);
        startActivity(homepage);
    }

    public SharedPreferences getPrefs(){
        SharedPreferences prefs;
        prefs=YourEvent.this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }
}