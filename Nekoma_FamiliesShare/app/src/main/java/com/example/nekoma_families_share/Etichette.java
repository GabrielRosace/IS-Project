package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabItem;
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
import java.util.Map;

public class Etichette extends AppCompatActivity {
    private List<myEtichette> etichette = new ArrayList<>();
    private LinearLayoutManager grouplistManager = new LinearLayoutManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etichette);

        Toolbar t = (Toolbar) findViewById(R.id.etichette_toolbar);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView grouplist = (RecyclerView) findViewById(R.id.etichette_g);
        String id_group = Utilities.getPrefs(this).getString("group", "");
        Utilities.httpRequest(this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    System.out.println(tmp);
                    System.out.println(response);
                    for(int i=0;i<tmp.length();++i){
                        myEtichette nuovo = new myEtichette(new JSONObject(tmp.getString(i)).getString("name"),new JSONObject(tmp.getString(i)).getString("label_id"));
                        etichette.add(nuovo);
                        System.out.println("etichette ******"+etichette.get(i));
                    }
                    MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(Etichette.this, etichette);

                    grouplist.setLayoutManager(grouplistManager);
                    grouplist.setAdapter(adapter);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        }, new HashMap<>());
    }

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<myEtichette> mData;

        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<myEtichette> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        // inflates the row layout from xml when needed
        @NonNull
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_1, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            myEtichette name = mData.get(position);
            holder.myTextView.setText(name.name);
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //todo fai la delete
                    RecyclerView grouplist = (RecyclerView) findViewById(R.id.etichette_g);
                    // String id_group = Utilities.getPrefs(Etichette.this).getString("group", "");
                    Utilities.httpRequest(Etichette.this, Request.Method.DELETE, "/label/"+name.id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(Etichette.this, "ELIMINATA", Toast.LENGTH_SHORT).show();
                            String id_group = Utilities.getPrefs(Etichette.this).getString("group", "");
                            Utilities.httpRequest(Etichette.this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response1) {
                                    try{
                                        etichette = new ArrayList<>();
                                        JSONArray tmp = new JSONArray(response1);
                                        System.out.println(tmp);
                                        for(int i=0;i<tmp.length();++i){
                                            myEtichette nuovo = new myEtichette(new JSONObject(tmp.getString(i)).getString("name"),new JSONObject(tmp.getString(i)).getString("label_id"));
                                            etichette.add(nuovo);
                                        }
                                        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(Etichette.this, etichette);

                                        grouplist.setLayoutManager(grouplistManager);
                                        grouplist.setAdapter(adapter);
                                    }catch(JSONException e){
                                        e.printStackTrace();
                                    }
                                }

                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                                    System.err.println(error.getMessage());
                                }
                            }, new HashMap<>());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                            System.err.println(error.getMessage());
                        }
                    },new HashMap<>());
                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
            MyRecyclerViewAdapter.ViewHolder holder;

            public ImageDownloader(MyRecyclerViewAdapter.ViewHolder holder) {
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
            }
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            ImageButton btn;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.etichette_id);
                btn = itemView.findViewById(R.id.delete_etichette);
            }
        }

        // convenience method for getting data at click position
        myEtichette getItem(int id) {
            return mData.get(id);
        }

    }

    public void addLable(View v){
        etichette = new ArrayList<>();
        RecyclerView grouplist = (RecyclerView) findViewById(R.id.etichette_g);
        // gli serve id utente, id gruppo  e etichetta
        HashMap<String,String> data = new HashMap<>();
        data.put("group_id",Utilities.getPrefs(this).getString("group", ""));
        String user_id;
        String userToken = Utilities.getToken(Etichette.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try{
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
            data.put("user_id",user_id);
        }catch (JSONException e){
            e.printStackTrace();
        }
        EditText add = (EditText) findViewById(R.id.aggiu_eti);
        String save = add.getText().toString();
        data.put("name",save);
        Utilities.httpRequest(this, Request.Method.POST, "/label", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(Etichette.this, response, Toast.LENGTH_SHORT).show();
                String id_group = Utilities.getPrefs(Etichette.this).getString("group", "");
                Utilities.httpRequest(Etichette.this, Request.Method.GET, "/label/"+id_group, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response1) {
                        try{
                            JSONArray tmp = new JSONArray(response1);
                            System.out.println(tmp);
                            for(int i=0;i<tmp.length();++i){
                                myEtichette nuovo = new myEtichette(new JSONObject(tmp.getString(i)).getString("name"),new JSONObject(tmp.getString(i)).getString("label_id"));
                                etichette.add(nuovo);
                            }
                            MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(Etichette.this, etichette);

                            grouplist.setLayoutManager(grouplistManager);
                            grouplist.setAdapter(adapter);
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.err.println(error.getMessage());
                    }
                }, new HashMap<>());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },data);


    }
    private class myEtichette {
        public final String name;
        public final String id;
        myEtichette(String name, String id){
            this.name=name;
            this.id=id;
        }
    }
}