package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Etichette extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etichette);

        // non funziona

        HashMap<String,String> id_group = new HashMap<>();
        System.out.println(Utilities.getPrefs(this).getString("group", ""));
        id_group.put("group_id", Utilities.getPrefs(this).getString("group", ""));
        Utilities.httpRequest(this, Request.Method.GET, "/label", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray tmp = new JSONArray(response);
                    System.out.println(tmp);
                    System.out.println(response);

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
        }, id_group);

        // non funziona

        /*RequestQueue queue = Volley.newRequestQueue(this);
        String url= this.getString(R.string.url) + "/label";

        StringRequest stringRequest1 = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println(error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+Utilities.getToken(Etichette.this));
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return new JSONObject("{\"group_id\":\"61acf9c4908415ca04000001\"}").toString().getBytes();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(stringRequest1);*/
    }

    public void homepage(View v){
        Intent homepage = new Intent(Etichette.this,Homepage.class);
        startActivity(homepage);
    }

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData;

        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        // inflates the row layout from xml when needed
        @NonNull
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_group, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            String name = mData.get(position);
            holder.myTextView.setText(name);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
            Button btn;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.recycle_view_text);
                btn = itemView.findViewById(R.id.recycle_view_btn);
            }
        }

        // convenience method for getting data at click position
        String getItem(int id) {
            return mData.get(id);
        }

    }

    public void addLable(View v){
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Etichette.this, error.toString(), Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        },data);
    }
}