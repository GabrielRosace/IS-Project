package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Bambino_soloinfo extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private List<String> labels;
    private List<String> id_labels_spinner;
    private List<String> childLabels ;
    private ArrayAdapter dataSpinner;
    private List<myEtichette> my_etichette = new ArrayList<>();
    private LinearLayoutManager grouplistManager = new LinearLayoutManager(this);
    private Boolean isChild = false;
    private Spinner etichette ;
    private String id_child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bambino_soloinfo);

        childLabels = new ArrayList<>();
        etichette =  (Spinner) findViewById(R.id.spinner_etichette);

        Toolbar t = (Toolbar) findViewById(R.id.bambinotoolbar);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // questa chiamata permette di avere l'id del bambino salvato nelle preferenze
        // preso dall'interfaccia precedente
        id_child = getKid();

        // permette di avere lo user id grazie al token
        String user_id;
        String userToken = Utilities.getToken(Bambino_soloinfo.this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try{
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");

            // questa chiamata permette di avere le informazioni del bambino quali: info del bambino, del genitore, le label
            Utilities.httpRequest(this,Request.Method.GET,"/children?ids[]="+id_child+"&searchBy=ids",new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONArray kid = new JSONArray(response);
                        for(int i=0;i<kid.length();++i){

                            // Popolare gli oggetti dell'interfaccia : caso in cui il bambino sia figlio dell'utente loggato
                            if(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("user_id").equals(user_id)){
                                isChild = true;
                                Bambini bambino = new Bambini(new JSONObject(kid.getString(i)).getString("_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path"));
                                ImageView img_bambino = (ImageView) findViewById(R.id.img_bambino);
                                new ImageDownloader(img_bambino).execute(getString(R.string.urlnoapi)+bambino.image_path);
                                TextView nome_bambino = (TextView) findViewById(R.id.nome_bambino);
                                nome_bambino.setText(bambino.name+" "+bambino.surname);
                                TextView nome = (TextView) findViewById(R.id.nome);
                                nome.setText("Nato il:");
                                LinearLayout card_genitore = (LinearLayout) findViewById(R.id.foto_genitore);
                                card_genitore.setVisibility(View.GONE);
                                TextView nome_genitore = (TextView) findViewById(R.id.nome_genitore);
                                String date =new JSONObject(kid.getString(i)).getString("birthdate");
                                String[] parts = date.split("T");
                                Date data = new SimpleDateFormat("yyyy-MM-dd").parse(parts[0]);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(data);
                                nome_genitore.setText(calendar.get(Calendar.DAY_OF_MONTH)+":"+(calendar.get(Calendar.MONTH)+1)+":"+calendar.get(Calendar.YEAR));

                                //aggoungi etichette al bambino
                                LinearLayout layout = (LinearLayout) findViewById(R.id.aggiunta_etichette);
                                layout.setVisibility(View.VISIBLE);

                                // popolazione spinner
                                if(new JSONObject(kid.getString(i)).has("labels")){
                                    JSONArray my_tmp = new JSONArray( new JSONObject(kid.getString(i)).getString("labels"));
                                    for(int y =0;y<my_tmp.length();++y){
                                        myEtichette et = new myEtichette(new JSONObject(my_tmp.getString(y)).getString("name"),new JSONObject(my_tmp.getString(y)).getString("label_id"),true);
                                        my_etichette.add(et);
                                    }
                                }

                                labels = new ArrayList<>();
                                id_labels_spinner = new ArrayList<>();

                                // chiamata per popolare lo spinner con le etichette decise dal capogruppo
                                Utilities.httpRequest(Bambino_soloinfo.this,Request.Method.GET,"/label/"+Utilities.getPrefs(Bambino_soloinfo.this).getString("group",""),response2 -> {
                                    try {
                                        JSONArray user_response = new JSONArray((String) response2);
                                        int cnt=0;
                                        for (int j = 0; j < user_response.length(); j++) {
                                            JSONObject obj = user_response.getJSONObject(j);
                                            if(!my_etichette.contains(new myEtichette(obj.getString("name"),"",true))){
                                                labels.add(obj.getString("name"));
                                                id_labels_spinner.add(obj.getString("label_id"));
                                            }
                                        }
                                        etichette.setOnItemSelectedListener(Bambino_soloinfo.this);
                                        dataSpinner = new ArrayAdapter(Bambino_soloinfo.this, R.layout.support_simple_spinner_dropdown_item);
                                        dataSpinner.addAll(labels);
                                        dataSpinner.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                                        etichette.setAdapter(dataSpinner);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }, error -> {
                                    Toast.makeText(Bambino_soloinfo.this, error.toString(), Toast.LENGTH_LONG).show();
                                }, new HashMap<>());
                                // fine spinner

                                TextView text_gender = (TextView) findViewById(R.id.genere_);
                                if(!new JSONObject(kid.getString(i)).getString("gender").equals("unspecified")){
                                    if(new JSONObject(kid.getString(i)).getString("gender").equals("Male") || new JSONObject(kid.getString(i)).getString("gender").equals("male")){
                                        text_gender.setText("Maschio");
                                    }else{
                                        text_gender.setText("Femmina");
                                    }
                                }
                                TextView text_allergie = (TextView) findViewById(R.id.allergie_);
                                if(!new JSONObject(kid.getString(i)).getString("allergies").equals("")){
                                    text_allergie.setText(new JSONObject(kid.getString(i)).getString("allergies"));
                                }
                                TextView text_altreinfo = (TextView) findViewById(R.id.altre_info);
                                if(!new JSONObject(kid.getString(i)).getString("other_info").equals("")){
                                    text_altreinfo.setText(new JSONObject(kid.getString(i)).getString("other_info"));
                                }
                                TextView text_bisgoni = (TextView) findViewById(R.id.bisogni_);
                                if(!new JSONObject(kid.getString(i)).getString("special_needs").equals("")){
                                    text_bisgoni.setText(new JSONObject(kid.getString(i)).getString("special_needs"));
                                }
                                addLabel();
                                addRecyclerView(my_etichette);
                            }else{
                                // Popolare oggetti dell'interfaccia : caso in cui il bambino sia figlio di un componente del gruppo

                                Bambini bambino = new Bambini(new JSONObject(kid.getString(i)).getString("_id"),new JSONObject(kid.getString(i)).getString("given_name"),new JSONObject(kid.getString(i)).getString("family_name"),new JSONObject(new JSONObject(kid.getString(i)).getString("image")).getString("path"));
                                ImageView img_bambino = (ImageView) findViewById(R.id.img_bambino);
                                new ImageDownloader(img_bambino).execute(getString(R.string.urlnoapi)+bambino.image_path);
                                TextView nome_bambino = (TextView) findViewById(R.id.nome_bambino);
                                nome_bambino.setText(bambino.name+" "+bambino.surname);
                                TextView nome_genitore = (TextView) findViewById(R.id.nome_genitore);
                                nome_genitore.setText(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("given_name")+" "+new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("family_name"));
                                ImageView img_genitore = (ImageView) findViewById(R.id.img_genitore);
                                new ImageDownloader(img_genitore).execute(getString(R.string.urlnoapi)+new JSONObject(new JSONObject(new JSONObject(kid.getString(i)).getString("parent")).getString("image")).getString("path"));
                                TextView text_gender = (TextView) findViewById(R.id.genere_);
                                if(!new JSONObject(kid.getString(i)).getString("gender").equals("unspecified")){
                                    text_gender.setText(new JSONObject(kid.getString(i)).getString("gender"));
                                }
                                TextView text_allergie = (TextView) findViewById(R.id.allergie_);
                                if(!new JSONObject(kid.getString(i)).getString("allergies").equals("")){
                                    text_allergie.setText(new JSONObject(kid.getString(i)).getString("allergies"));
                                }
                                TextView text_altreinfo = (TextView) findViewById(R.id.altre_info);
                                if(!new JSONObject(kid.getString(i)).getString("other_info").equals("")){
                                    text_altreinfo.setText(new JSONObject(kid.getString(i)).getString("other_info"));
                                }
                                TextView text_bisgoni = (TextView) findViewById(R.id.bisogni_);
                                if(!new JSONObject(kid.getString(i)).getString("special_needs").equals("")){
                                    text_bisgoni.setText(new JSONObject(kid.getString(i)).getString("special_needs"));
                                }
                                if(new JSONObject(kid.getString(i)).has("labels")){
                                    JSONArray my_tmp = new JSONArray( new JSONObject(kid.getString(i)).getString("labels"));
                                    for(int y =0;y<my_tmp.length();++y){
                                        myEtichette et = new myEtichette(new JSONObject(my_tmp.getString(y)).getString("name"),new JSONObject(my_tmp.getString(y)).getString("label_id"),false);
                                        my_etichette.add(et);
                                    }
                                }

                                addRecyclerView(my_etichette); // necessario per popolare la recycle view con le etichette del bambino
                            }
                        }

                        RelativeLayout b = (RelativeLayout) findViewById(R.id.caricamento_id);
                        b.setVisibility(View.GONE);

                    }catch(JSONException | ParseException e){
                        e.printStackTrace();
                    }
                }
            },new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Bambino_soloinfo.this, error.toString(), Toast.LENGTH_LONG).show();
                }

            },new HashMap<>());
        }catch(JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // questa nested class permette di facilitare la definizione del bambino nell'interfaccia
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

    // questa nested class permette di caricare l'immagine del genitore e del bambino
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        public final ImageView image;
        public ImageDownloader(ImageView image) {
            this.image = image;
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
            image.setImageBitmap(bitmap);
        }
    }

    // metodo che permette di popolare la recycle view
    private void addRecyclerView(List<myEtichette> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.label_info);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // funzione che permette di aggiungere un'etichetta grazie alla chiamata post al suo interno,
    // a sua volta permette inoltre di aggiornare la recycle view delle etichette del bambino,
    // rimuovendo ogni qualvolta l'utente aggiunge un etichetta al proprio figlio la rimuove dallo spinner
    public void addLabel(){
        Button aggiungi_etichetta = (Button) findViewById(R.id.add_etichetta);
        aggiungi_etichetta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rimossa;
                childLabels.add(id_labels_spinner.get(etichette.getSelectedItemPosition()));
                rimossa = id_labels_spinner.get(etichette.getSelectedItemPosition());
                dataSpinner.remove(etichette.getSelectedItem());
                id_labels_spinner.remove(etichette.getSelectedItemPosition());
                if(id_labels_spinner.size()==0){
                    aggiungi_etichetta.setEnabled(false);
                }
                Map<String, String> add_label = new HashMap<>();
                add_label.put("child_id",id_child);
                add_label.put("label_id",rimossa);
                Utilities.httpRequest(Bambino_soloinfo.this, Request.Method.POST, "/label/child", new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        Toast.makeText(Bambino_soloinfo.this, "Etichetta aggiunta", Toast.LENGTH_SHORT).show();
                        Utilities.httpRequest(Bambino_soloinfo.this, Request.Method.GET, "/label/child/" + id_child, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String responsee) {

                                try{

                                    Toast.makeText(Bambino_soloinfo.this, "AGGIUNTA ETICHETTA", Toast.LENGTH_SHORT).show();
                                    JSONArray tmp = new JSONArray(responsee);
                                    my_etichette = new ArrayList<>();
                                    for(int y =0;y<tmp.length();++y){
                                        myEtichette et = new myEtichette(new JSONObject(tmp.getString(y)).getString("name"),new JSONObject(tmp.getString(y)).getString("label_id"),true);
                                        my_etichette.add(et);
                                    }
                                    addRecyclerView(my_etichette);

                                }catch (JSONException e){
                                    Toast.makeText(Bambino_soloinfo.this, "Qualcosa non va", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Bambino_soloinfo.this, "ERRORE", Toast.LENGTH_SHORT).show();
                            }

                        }, new HashMap<>());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Bambino_soloinfo.this, "Qualcosa è andato storto", Toast.LENGTH_SHORT).show();
                    }

                }, add_label);
            }
        });
    }

    // non più usato permetteva di tornare all'interfaccia precedente
    // sostituito con la chiamata all'onClik nell'onCreate della Toolbar
    public void getLista(View v) {
        Intent homepage = new Intent(Bambino_soloinfo.this, ListaBambiniAmici.class);
        startActivity(homepage);
    }

    // metodo che chiama dalle preferenze l'id del bambino precedentemente salvato
    public String getKid(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return  prefs.getString("id_child","");
    }

    // nested class che permette di adattare la recyle view con i parametri da noi decisi quali
    // in questo caso le etichette
    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<myEtichette> mData;
        private LayoutInflater mInflater;

        // dati sono passati al costruttore
        MyRecyclerViewAdapter(Context context, List<myEtichette> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        // aggiugge le righe al layout dell'xml quando necessario
        @NonNull
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_1, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        // lega i dati alla View in ogni riga
        // in questo particolare caso permette di eliminare l'etichetta all'onClik del bottone legandolo ad ogni riga (ogni riga corrisponde ad un particolare id)
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            myEtichette name = mData.get(position);
            holder.myTextView.setText(name.name);
            if(name.parent == true){
                holder.btn.setVisibility(View.VISIBLE);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utilities.httpRequest(Bambino_soloinfo.this, Request.Method.DELETE, "/label/child/" + name.id +"/"+ id_child, new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                recreate();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Bambino_soloinfo.this, "ERRORE", Toast.LENGTH_SHORT).show();
                            }
                        }, new HashMap<>());
                    }
                });
            }
        }

        // numero totale delle righe della view
        @Override
        public int getItemCount() {
            return mData.size();
        }

        //memorizza e ricicla le visualizzazioni mentre vengono fatte scorrere fuori dallo schermo
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView myTextView;
            ImageButton btn;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.etichette_id);
                btn = itemView.findViewById(R.id.delete_etichette);
            }
        }

        // metodo conveniente per ottenere i dati nella posizione del clic
        myEtichette getItem(int id) {
            return mData.get(id);
        }

    }

    // nested class che permette di facilitare l'uso delle etichette
    private class myEtichette {
        public final String name;
        public final String id;
        public final Boolean parent;
        myEtichette(String name, String id, Boolean parent){
            this.name=name;
            this.id=id;
            this.parent= parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            myEtichette that = (myEtichette) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}