package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaBambiniAmici extends AppCompatActivity {
    private List<String> save = new ArrayList<>();
    private List<Bambini> l = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bambini_amici);
        //lista degli utenti nel gruppo


        RecyclerView grouplist = (RecyclerView) findViewById(R.id.listabambini);
        LinearLayoutManager grouplistManager = new LinearLayoutManager(this);

        RequestQueue groupuser = Volley.newRequestQueue(this);
        // sostituire 61acf9c4908415ca04000001 con l'id del gruppo

        String id_group = "61acf9c4908415ca04000001";
        // String id_group = Utilities.getPrefs("group");
        String url = getString(R.string.url) + "/groups/"+id_group+"/children"; // lista di tutti i bambini che sono dentro al gruppo
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray tmp = new JSONArray(response);
                    for(int i=0; i<tmp.length();++i){
                        RequestQueue groupkids = Volley.newRequestQueue(ListaBambiniAmici.this);
                        String urlq = getString(R.string.url) + "/children?ids[]="+tmp.getString(i)+"&searchBy=ids"; //info un bambino
                        StringRequest getBambinoInfo = new StringRequest(Request.Method.GET, urlq, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONArray kid = new JSONArray(response);
                                    for(int i=0;i<kid.length();++i){
                                        l.add(new Bambini(new JSONObject(kid.getString(i)).getString("child_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path")));
                                    }
                                    MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(ListaBambiniAmici.this, l);
                                    grouplist.setLayoutManager(grouplistManager);
                                    grouplist.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(ListaBambiniAmici.this, error.toString(), Toast.LENGTH_LONG).show();
                                System.err.println(error.getMessage());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String,String> headers = new HashMap<String,String>();
                                headers.put("Authorization","Bearer "+getToken());
                                return headers;
                            }
                        };
                        groupkids.add(getBambinoInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListaBambiniAmici.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+getToken());
                return headers;
            }
        };
        groupuser.add(stringRequest);
    }

    protected String getToken(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return  prefs.getString("token","");
    }

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<Bambini> mData;

        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<Bambini> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
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

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            Button btn;
            ImageView img;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.info);
                btn = itemView.findViewById(R.id.recycle_view_btn);
                img =  itemView.findViewById(R.id.recycle_view_img);
            }
        }

        // convenience method for getting data at click position
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
    }



    public void getHomepage(View v){
        Intent  homepage= new Intent(ListaBambiniAmici.this,Homepage.class);
        startActivity(homepage);
    }
    public SharedPreferences getPrefs(){
        SharedPreferences prefs;
        prefs=ListaBambiniAmici.this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }

}