package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceltaDelGruppo extends AppCompatActivity {

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData;
        private List<String> mFoto;

        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<String> data, List<String> foto) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.mFoto = foto;
        }

        // inflates the row layout from xml when needed
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_group, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String name = mData.get(position);
            holder.myTextView.setText(name);
            holder.btn.setText("Entra");
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit().putString("group",name);
                    Intent homepageA = new Intent(SceltaDelGruppo.this,Homepage.class);
                    startActivity(homepageA);
                }
            });

            new ImageDownloader(holder).execute(getString(R.string.urlnoapi)+mFoto.get(position));
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
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


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            Button btn;
            ImageView img;
            //Context mcontext;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.recycle_view_text);
                btn = itemView.findViewById(R.id.recycle_view_btn);
                img =  itemView.findViewById(R.id.recycle_view_img);
                //mcontext = itemView.getContext();
            }
        }

        // convenience method for getting data at click position
        String getItem(int id) {
            return mData.get(id);
        }

    }


//    protected String getToken(){
//        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
//        return  prefs.getString("token","");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scelta_del_gruppo);

        ArrayList<String> groupName = new ArrayList<>();
        ArrayList<String> groupPhoto = new ArrayList<>();

        String user_id;
        String userToken = Utilities.getToken(SceltaDelGruppo.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");

            Utilities.httpRequest(SceltaDelGruppo.this, Request.Method.GET,"/users/" + user_id + "/groups", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray res = new JSONArray(response);
                        for (int i = 0; i < res.length(); i++) {
                            String group_id = new JSONObject(res.getString(i)).get("group_id").toString();
                            Utilities.httpRequest(SceltaDelGruppo.this,Request.Method.GET,"/groups/"+group_id,response1 -> {
                                try {
                                    JSONObject object = new JSONObject((String) response1);
                                    groupName.add(object.getString("name"));
                                    groupPhoto.add(new JSONObject(object.getString("image")).getString("path"));

                                    RecyclerView grouplist = (RecyclerView) findViewById(R.id.grouplist);
                                    LinearLayoutManager grouplistManager = new LinearLayoutManager(SceltaDelGruppo.this);
                                    MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(SceltaDelGruppo.this, groupName,groupPhoto);

                                    grouplist.setLayoutManager(grouplistManager);
                                    grouplist.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }, error1 -> {
                                Toast.makeText(SceltaDelGruppo.this, error1.toString(), Toast.LENGTH_LONG).show();
                                System.err.println(error1.getMessage());
                            }, new HashMap<>());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> {

            }, new HashMap<>());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}