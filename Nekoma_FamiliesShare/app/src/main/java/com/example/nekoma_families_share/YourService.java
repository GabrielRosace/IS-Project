package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class YourService extends AppCompatActivity {

    private List<Utilities.myService> tuoi_servizi = new ArrayList<>();
    private List<Utilities.myService> partecipi_servizi = new ArrayList<>();
    private List<Utilities.myService> scaduti_servizi = new ArrayList<>();
    private String id_group;
    private String user_id;

    @Override
    protected void onPostResume() {
        super.onPostResume();

        tuoi_servizi = new ArrayList<>();
        partecipi_servizi = new ArrayList<>();
        scaduti_servizi = new ArrayList<>();

        //parsing del token per prendere lo user_id

        String userToken = Utilities.getToken(this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        }catch(JSONException e){
            e.printStackTrace();
        }

        // prende l'id del gruppo dalla memoria condivisa
        id_group = Utilities.getPrefs(this).getString("group","");

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_servizi);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.service_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    // caso 1 - servizi che sono tuoi
                    addRecyclerView(tuoi_servizi);
                }else if(tab.getPosition()==1){
                    // caso 2 - servizi a cui hai partecipato
                    addRecyclerView(partecipi_servizi);
                }else{
                    // caso 3 - servizi che sono scaduti
                    addRecyclerView(scaduti_servizi);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // chiamata per popolare le liste
        this.setmyService();
        this.setPartecipi_servizi();
        this.setScaduti_servizi();
    }

    // necessario per creare l'oggetto che poi verrà passato tramite le shared preference
    // a dettagli servizio
    public Utilities.myService populateService (JSONObject response) throws JSONException{
        String service_id = response.getString("service_id");
        String owner_id = response.getString("owner_id");
        String nome = response.getString("name");
        String descrizione = response.getString("descrizione");
        String location = (response.has("location"))?response.getString("location"):"";
        String pattern = (response.has("pattern"))?response.getString("pattern"):"";
        String car_space = (response.has("car_space"))?response.getString("carspace"):"";
        String lend_obj = (response.has("lend_obj"))?response.getString("lend_obj"):"";
        String lend_time = (response.has("lend_time"))?response.getString("lend_time"):"";
        String pickuplocation = (response.has("pickuplocation"))?response.getString("pickuplocation"):"";
        String img = (response.has("img"))?response.getString("img"):"";
        String nPart = (response.has("nPart"))?response.getString("nPart"):"";
        String recurrence = (response.has("recurrence"))?response.getString("recurrence"):"";
        return new Utilities.myService(service_id,owner_id,nome,descrizione,location,pattern,car_space,lend_obj,lend_time,pickuplocation,img,nPart,recurrence);
    }

    // Chiamata nel caso in cui il servizio sia tuo
    public void setmyService(){
        Utilities.httpRequest(this, Request.Method.GET, "/service?creator=me&time=next", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        tuoi_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YourService.this, "Non hai servizi", Toast.LENGTH_SHORT).show();
            }
        }, new HashMap<>());
    }

    // Chiamata nel caso in cui il servizio in cui partecipi
    public void setPartecipi_servizi(){
        Utilities.httpRequest(this, Request.Method.GET, "/service?partecipant=me&time=next", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        partecipi_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YourService.this, "Non ci sono servizi a cui partecipi", Toast.LENGTH_SHORT).show();
            }
        }, new HashMap<>());
    }

    // Chiamata nel caso in cui il servizio sia scaduto
    public void setScaduti_servizi(){
        Utilities.httpRequest(this, Request.Method.GET, "/service?creator=me&partecipant=me&time=expired", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        scaduti_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YourService.this, "Non ci sono servizi", Toast.LENGTH_SHORT).show();
            }
        }, new HashMap<>());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_service);
    }

    // metodo che popola la recycle view
    private void addRecyclerView(List<Utilities.myService> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.servizi_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // recycle view
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Utilities.myService> mData;
        private LayoutInflater mInflater;


        MyRecyclerViewAdapter(Context context, List<Utilities.myService> data) {
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
            Utilities.myService service = mData.get(position);
            holder.btn.setText(service.nome);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //todo mettere dettaglio servizio
                    Intent servizio = new Intent(YourService.this, DettagliEvento.class);
                    // System.out.println("intent: "+event.toString());
                    servizio.putExtra("servizio", service.toString());
                    startActivity(servizio);
                }
            });

        }


        @Override
        public int getItemCount() {
            return mData.size();
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


        Utilities.myService getItem(int id) {
            return mData.get(id);
        }

    }



}