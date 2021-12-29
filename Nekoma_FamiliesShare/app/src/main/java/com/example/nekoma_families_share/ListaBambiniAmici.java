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
import android.widget.RelativeLayout;
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

public class ListaBambiniAmici extends AppCompatActivity {
    private List<Bambini> b_amici = null;
    private List<Bambini> b_miei = null;
    private String id_group;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bambini_amici);

        Toolbar to = (Toolbar) findViewById(R.id.toolbar_profilo);
        to.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        String userToken = Utilities.getToken(ListaBambiniAmici.this);
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

        // mi permette di spostarmi tra le tab
        TabLayout t = (TabLayout)findViewById(R.id.bambini_tab);
        t.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==1){
                    addRecyclerView(b_miei);
                }else{
                    addRecyclerView(b_amici);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        this.Kids(); // chiamo e popolo gli array con le informazioni

    }

    // questo metodo mi permette di popolare l'interfaccia andando a distinguere se il figlio
    // è dell'utente oppure se il figlio è di uno dei componenti del gruppo
    // popolando la recycle view
    public void Kids(){
        b_miei = new ArrayList<>();
        b_amici = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + id_group + "/children", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    String url = "/children?searchBy=ids";
                    for(int i=0;i<tmp.length();++i){
                        //todo costruire l'url
                        url += "&ids="+tmp.getString(i);
                    }
                    Utilities.httpRequest(ListaBambiniAmici.this, Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray kid = new JSONArray(response);
                                for (int i = 0; i < kid.length(); ++i) {
                                    if(!new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("user_id").equals(user_id)){
                                        b_amici.add(new Bambini(new JSONObject(kid.getString(i)).getString("child_id"), new JSONObject(kid.getString(i)).getString("given_name"), new JSONObject(kid.getString(i)).getString("family_name"), new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path")));
                                    }else{
                                        b_miei.add(new Bambini(new JSONObject(kid.getString(i)).getString("child_id"), new JSONObject(kid.getString(i)).getString("given_name"), new JSONObject(kid.getString(i)).getString("family_name"), new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path")));
                                    }
                                }
                                RelativeLayout b = (RelativeLayout) findViewById(R.id.caricamento);
                                b.setVisibility(View.GONE);
                                addRecyclerView(b_amici);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ListaBambiniAmici.this, "Richiesta non andata a buon fine", Toast.LENGTH_SHORT).show();
                        }
                    }, new HashMap<>());

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListaBambiniAmici.this, "ERRORE", Toast.LENGTH_SHORT).show();
            }
        }, new HashMap<>());
    }

    // metodo che permette di popolare la recycle view
    private void addRecyclerView(List<Bambini> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listabambiniamici);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    // nested class che permette di popolare la recycle view con la classe Bambini
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Bambini> mData;
        private LayoutInflater mInflater;


        MyRecyclerViewAdapter(Context context, List<Bambini> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item, parent, false);
            return new ViewHolder(view);
        }

        // definizione dell'OnClick savando l'id del bambino nelle preferenze
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Bambini kid = mData.get(position);
            holder.myTextView.setText(kid.name+" "+kid.surname);
            holder.btn.setText("INFO");
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences prefs = getPrefs();
                    SharedPreferences.Editor edit;
                    edit=prefs.edit();
                    String id_child=kid.id;
                    edit.putString("id_child",id_child);
                    edit.apply();
                    Intent children = new Intent(ListaBambiniAmici.this,Bambino_soloinfo.class);
                    startActivity(children);
                }
            });
            new ImageDownloader(holder).execute(getString(R.string.urlnoapi)+mData.get(position).image_path);

        }


        @Override
        public int getItemCount() {
            return mData.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            Button btn;
            ImageView img;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.info);
                btn = itemView.findViewById(R.id.recycle_view_btn);
                img =  itemView.findViewById(R.id.myrecycle_view_img);
            }
        }


        Bambini getItem(int id) {
            return mData.get(id);
        }
        private class ImageDownloader extends AsyncTask<String, Void, Bitmap>{
            ViewHolder holder;

            public ImageDownloader(ViewHolder holder) {
                this.holder = holder;
            }

            @Override
            protected Bitmap doInBackground(String... strings) {
                String urlOfImage = strings[0];
                Bitmap logo = null;
                try{
                    InputStream is = new URL(urlOfImage).openStream();
                    logo = BitmapFactory.decodeStream(is);
                }catch(Exception e){ // Catch the download exception
                    e.printStackTrace();
                }
                return logo;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                holder.img.setImageBitmap(bitmap);
            }
        }
    }

    //
    private static class Bambini{
        public final String id;
        public final String name;
        public final String surname;
        public final String image_path;
        private Bambini(String id, String name, String surname, String image_path){
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.image_path = image_path;
        }

        @Override
        public String toString() {
            return "Bambini{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", image_path='" + image_path + '\'' +
                    '}';
        }
    }


    // non più usato sostituito dall'onclick della toolbar nell'OnCreate
    public void getHomepage(View v){
        Intent  homepage= new Intent(ListaBambiniAmici.this,Homepage.class);
        startActivity(homepage);
    }

    // per le info condivise
    public SharedPreferences getPrefs(){
        SharedPreferences prefs;
        prefs=ListaBambiniAmici.this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }

    // metodo che permette di spostarsi nell'interfaccia di creazione del bambino
    public void getNewKid(View v){
        Intent  kid= new Intent(ListaBambiniAmici.this,NewChild.class);
        startActivity(kid);
    }
}