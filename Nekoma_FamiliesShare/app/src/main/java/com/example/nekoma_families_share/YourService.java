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

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class YourService extends AppCompatActivity {
    private List<myService> tuoi_servizi = new ArrayList<>();
    private List<myService> partecipi_servizi = new ArrayList<>();
    private List<myService> scaduti_servizi = new ArrayList<>();
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

    public void setmyService(){
        // popolare nel caso in cui sia tuo
    }

    public void setPartecipi_servizi(){
        // popolare nel caso in cui partecipi
    }

    public void setScaduti_servizi(){
        // popolare nel caso in cui l'evento sia scaduto
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_service);
    }

    // metodo che popola la recycle view
    private void addRecyclerView(List<myService> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.servizi_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // recycle view
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<myService> mData;
        private LayoutInflater mInflater;


        MyRecyclerViewAdapter(Context context, List<myService> data) {
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
            myService service = mData.get(position);
            // System.out.println(" ************************* questo è event: "+event.toString());
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


        myService getItem(int id) {
            return mData.get(id);
        }

    }

    private class myService{
        public final String nome;
        public final String service_id;
        public final String img;
        public final int nPart;
        public final String descrizione;
        public final String enddate;
        public final String labels;
        public final String owner_id;
        public myService(String nome, String img, String service_id, int nPart, String descrizione, String enddate, String labels, String owner_id) {
            this.nome = nome;
            this.img = img;
            this.service_id = service_id;
            this.nPart = nPart;
            this.descrizione = descrizione;
            this.enddate = enddate;
            this.labels = labels;
            this.owner_id = owner_id;
        }

        @Override
        public String toString() {
            return nome+'/'+service_id+'/'+img+'/'+nPart+'/'+descrizione+'/'+enddate+'/'+labels+'/'+owner_id;
        }
    }

}