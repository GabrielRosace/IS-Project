package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DettagliEvento extends AppCompatActivity {

    private String activity_id = null;
    private String creator_id = null;
    private boolean isCreator = false;

    private ArrayAdapter dataSpinner;
    private List<String> labelsId;
    private Spinner spinner;
    private List<String> selectedLabel;

    private List<String> eventLabels;

    private EditText endDate;
    private EditText startDate;
    private EditText desc;


    public void deleteActivity(View v){
        Toast.makeText(this, "Questa activity sparisce: "+activity_id, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento);



        Toolbar t = (Toolbar) findViewById(R.id.toolbar2);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();

        String extraData = intent.getStringExtra("evento");



        ImageView img = (ImageView) findViewById(R.id.eventImage);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        endDate = (EditText) findViewById(R.id.endDate);
        startDate = (EditText) findViewById(R.id.startdate);
        desc = (EditText) findViewById(R.id.description);
        TextView nPart = (TextView) findViewById(R.id.nPart);
        Button misc = (Button) findViewById(R.id.button);
        Button aggiungiLabel = (Button) findViewById(R.id.label_btn);
        spinner = (Spinner) findViewById(R.id.spinner);



        VisualizzazioneEventi.Evento evento = VisualizzazioneEventi.Evento.getEventoFromString(extraData); // Parsing intent params


        activity_id = evento.event_id;
        creator_id = evento.owner_id;

        isCreator = Utilities.getUserID(this).equals(creator_id);

        if(isCreator){
            findViewById(R.id.delete_action).setVisibility(View.VISIBLE);
            misc.setText("Modifica");
            desc.setEnabled(true);
            startDate.setEnabled(true);
            endDate.setEnabled(true);
            findViewById(R.id.add_label).setVisibility(View.VISIBLE);
            misc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modify_event();
                }
            });
        }else{
            misc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    join_event();
                }
            });
        }

        if(evento.labels.equals("")){
            eventLabels = new ArrayList<>();
        }else{
            eventLabels = Arrays.asList(evento.labels.split(","));
        }
        addRecyclerView(eventLabels);


        eventName.setText(evento.nome);
        img.setImageDrawable(getDrawable(R.drawable.persone)); // TODO aggiungi immagine

        desc.setText(evento.descrizione.equals("")?"Non specificata":evento.descrizione);



        Utilities.httpRequest(this,Request.Method.GET,"/groups/"+Utilities.getGroupId(this)+"/nekomaActivities/"+evento.event_id+"/information",response -> {
            try {
                JSONObject obj = new JSONObject(response.toString());
                String start = obj.getString("start");
                String end = obj.getString("end");
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                cal.setTime(sdf.parse(start));

                startDate.setText(getDate(cal));

                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                cal.setTime(sdf.parse(end));

                endDate.setText(getDate(cal));

                if(obj.getString("parents").equals("[]")){
                    nPart.setText(nPart.getText()+"0");
                }else{
                    nPart.setText(nPart.getText()+""+obj.getJSONArray("parents").length());
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }, error -> {

        }, new HashMap<>());



        List<String> labelsName = new ArrayList<>();
        labelsId = new ArrayList<>();
        Utilities.httpRequest(this,Request.Method.GET,"/label/"+Utilities.getPrefs(this).getString("group",""),response -> {
            try {
                JSONArray user_response = new JSONArray((String) response);
                for (int j = 0; j < user_response.length(); j++) {
                    JSONObject obj = user_response.getJSONObject(j);
                    if(!eventLabels.contains(obj.getString("name"))){
                        labelsName.add(obj.getString("name"));
                        labelsId.add(obj.getString("label_id"));
                    }
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                dataSpinner = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item);
                dataSpinner.addAll(labelsName);
                dataSpinner.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinner.setAdapter(dataSpinner);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(DettagliEvento.this, error.toString(), Toast.LENGTH_LONG).show();
        }, new HashMap<>());

    }


    public void modify_event(){
        Toast.makeText(DettagliEvento.this, "Modifica", Toast.LENGTH_SHORT).show(); // TODO modifica evento
//        recreate(); Questa è utile
    }

    public void join_event(){
        // GET del timeslot -> PATCH localhost:8080/api/groups/:id_gruppo/nekomaActivities/:id_attività/timeslots/:id_timeslot
        Toast.makeText(DettagliEvento.this, "Partecipa", Toast.LENGTH_SHORT).show(); // TODO fai in modo che partecipi
    }

    public void newLabel(View v){
        selectedLabel.add(labelsId.get(spinner.getSelectedItemPosition()));
        dataSpinner.remove(spinner.getSelectedItem());
        labelsId.remove(spinner.getSelectedItemPosition());
    }

    public static String getDate(Calendar cal){
        return cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
    }


    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

        private List<String> label_list;
        private LayoutInflater mInflater;

        public MyRecyclerViewAdapter(Context context, List<String> label_list) {
            this.label_list = label_list;
            this.mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.recycler_view_item_event_labels, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            final String item = label_list.get(position);
            holder.label.setText(item);

            System.out.println("--------" + label_list.size());

            if(isCreator){
                holder.btn.setVisibility(View.VISIBLE);
            }
            holder.btn.setText("Elimina"); 
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DettagliEvento.this, "Così la elimini...", Toast.LENGTH_SHORT).show(); //TODO eliminazione etichetta
                }
            });
        }

        @Override
        public int getItemCount() {
            return label_list.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView label;
            Button btn;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.label = itemView.findViewById(R.id.label);
                this.btn = itemView.findViewById(R.id.edit_label);
            }
        }
    }


    private void addRecyclerView(List<String> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.event_label);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DettagliEvento.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(DettagliEvento.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

}