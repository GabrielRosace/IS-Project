package com.example.nekoma_families_share;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Utilities {
    public static SharedPreferences setSharedPreferences(Context context){
        SharedPreferences prefs;
        prefs=context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return prefs;
    }

    public static SharedPreferences getPrefs(Context context){
        return context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
    }


    public static void setToken(Context context,String token){
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit;
        edit=prefs.edit();
        edit.putString("token",token);
        edit.apply();
    }


    public static String getToken(Context context){
        SharedPreferences prefs = getPrefs(context);
        return  prefs.getString("token","");
    }

    /** With bearer token authentication and params */
    public static void httpRequest(Context context,int method, String endpoint , Response.Listener onSuccess, Response.ErrorListener onError, Map<String,String> params){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url= context.getString(R.string.url) + endpoint;

        StringRequest stringRequest1 = new StringRequest(method, url, onSuccess, onError){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+getToken(context));
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(stringRequest1);
    }


    //TODO parse token

    /*public static class ImageDownloader<T> extends AsyncTask<String, Void, Bitmap> {
        T holder;
        Consumer<Bitmap> postExecute;

        public ImageDownloader(T holder, Consumer<Bitmap> postExecute) {
            this.holder = holder;
            this.postExecute = postExecute;
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
            postExecute.accept(bitmap);
        }
    }*/
}
