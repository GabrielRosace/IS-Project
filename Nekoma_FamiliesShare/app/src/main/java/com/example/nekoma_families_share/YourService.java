package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageWriter;
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
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class YourService extends AppCompatActivity {

    private List<Utilities.myService> tuoi_servizi = new ArrayList<>();
    private List<Utilities.myService> partecipi_servizi = new ArrayList<>();
    private List<Utilities.myService> scaduti_servizi = new ArrayList<>();
    private String id_group;
    private String user_id;


    // le operazioni vengono effettuate nella onPostResume e non nella
    // onCreate in quanto , nel caso in cui l'utente acceda alla view successiva,
    // quando si ritorna nell'activity corrente,
    // i componenti ri riaggiornano con le nuove informazioni nel caso ci siano
    // quindi per esempio nel caso in cui l'utente aggiorni la sua partecipazione
    // quando si ritornerà alla medesima schermata la sua visualizzazione sarà aggiornata
    @Override
    protected void onPostResume() {
        super.onPostResume();
        ConstraintLayout pr = (ConstraintLayout) findViewById(R.id.progress_service);
        pr.setVisibility(View.VISIBLE);
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
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
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
    }

    // necessario per creare l'oggetto che poi verrà passato tramite le shared preference
    // a dettagli servizio
    public Utilities.myService populateService (JSONObject response) throws JSONException{
        return new Utilities.myService(response);
    }

    // Questo chiamata permette grazie ai filtri di richiamare i servizi dell'utente che
    // che non sono scaduti, quindi ancora validi.
    // questa funzione usufruisce della classe in utilities che parsa direttamente l'oggetto,
    // per l'activity successiva
    public void setmyService(){
        Utilities.httpRequest(this, Request.Method.GET,"/groups/"+this.id_group+ "/service?creator=me&time=next", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        tuoi_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                    addRecyclerView(tuoi_servizi);
                    YourService.this.setPartecipi_servizi();
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


    // Questo chiamata permette grazie ai filtri di richiamare i servizi a cui l'utente partecipa l'utente che
    // che non sono scaduti, quindi ancora validi
    // questa funzione usufruisce della classe in utilities che parsa direttamente l'oggetto,
    // per l'activity successiva
    public void setPartecipi_servizi(){
        Utilities.httpRequest(this, Request.Method.GET, "/groups/"+this.id_group+"/service?partecipant=me&time=next", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        partecipi_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                    YourService.this.setScaduti_servizi();
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

    // Questo chiamata permette grazie ai filtri di richiamare i servizi a cui l'utente partecipa l'utente, e che ha creato che
    // che non sono scaduti, quindi ancora validi
    // questa funzione usufruisce della classe in utilities che parsa direttamente l'oggetto,
    // per l'activity successiva
    public void setScaduti_servizi(){
        Utilities.httpRequest(this, Request.Method.GET, "/groups/"+this.id_group+"/service?creator=me&partecipant=me&time=expired", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    for (int i=0;i<tmp.length();++i){
                        scaduti_servizi.add(populateService(tmp.getJSONObject(i)));
                    }
                    ConstraintLayout pr = (ConstraintLayout) findViewById(R.id.progress_service);
                    pr.setVisibility(View.GONE);
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
    public void setRicorrentiServizi(){

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


    // recycle view, al momento dell'onClick sono stati distinti
    // servizi con ricorrenza oppure servizi senza ricorrenza
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
            new ImageDownloader(holder.myImgView).execute(service.getImage());
            if(service.recurrence.equals("true")){
                holder.btn.setBackgroundColor(getResources().getColor(R.color.recurrent_event,getTheme()));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo mettere dettaglio servizio ricorrente
                        Intent servizio = new Intent(YourService.this, DettagliServizio.class);
                        servizio.putExtra("servizio", service.toString());
                        startActivity(servizio);
                    }
                });
            }else{
                holder.btn.setBackgroundColor(getResources().getColor(R.color.purple_500,getTheme()));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo mettere dettaglio servizio
                        Intent servizio = new Intent(YourService.this, DettagliServizio.class);
                        servizio.putExtra("servizio", service.toString());
                        startActivity(servizio);
                    }
                });
            }


        }


        @Override
        public int getItemCount() {
            return mData.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView myImgView;
            Button btn;

            ViewHolder(View itemView) {
                super(itemView);
                myImgView = itemView.findViewById(R.id.myrecycle_view_img);
                btn = itemView.findViewById(R.id.name_event);
            }
        }


        Utilities.myService getItem(int id) {
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

}