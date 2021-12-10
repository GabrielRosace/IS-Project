package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
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
import java.util.Map;
import java.util.Objects;


public class DettagliEvento extends AppCompatActivity {

    private String activity_id = null;
    private String creator_id = null;
    private boolean isCreator = false;

    private ArrayAdapter dataSpinner;
    private List<String> labelsId;
    private Spinner spinner;
    private List<String> selectedLabel;
    private Map<String, String> event_labels_id;

    private List<String> eventLabels;

    private EditText endDate;
    private EditText startDate;
    private EditText desc;

    private String extraData;

    private String startTime;
    private String endTime;


    public void deleteActivity(View v) {
        Toast.makeText(this, "Questa activity deve sparire: " + activity_id, Toast.LENGTH_SHORT).show();
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

        extraData = intent.getStringExtra("evento");

        // System.out.println("-----------" + extraData);


        ImageView img = (ImageView) findViewById(R.id.eventImage);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        endDate = (EditText) findViewById(R.id.endDate);
        startDate = (EditText) findViewById(R.id.startdate);
        desc = (EditText) findViewById(R.id.description);
        TextView nPart = (TextView) findViewById(R.id.nPart);
        Button misc = (Button) findViewById(R.id.button);
        Button aggiungiLabel = (Button) findViewById(R.id.label_btn);
        spinner = (Spinner) findViewById(R.id.spinner);


        selectedLabel = new ArrayList<>();


        VisualizzazioneEventi.Evento evento = VisualizzazioneEventi.Evento.getEventoFromString(extraData); // Parsing intent params


        activity_id = evento.event_id;
        creator_id = evento.owner_id;

        isCreator = Utilities.getUserID(this).equals(creator_id);

        if (isCreator) {
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
        } else {
            misc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    join_event();
                }
            });
        }

        if (evento.labels.equals("")) {
            eventLabels = new ArrayList<>();
        } else {
            eventLabels = Arrays.asList(evento.labels.split(","));
        }
        addRecyclerView(eventLabels);


        eventName.setText(evento.nome);
        img.setImageDrawable(getDrawable(R.drawable.persone)); // TODO aggiungi immagine



        Utilities.httpRequest(this, Request.Method.GET, "/groups/"+Utilities.getGroupId(this)+"/activities/"+activity_id+"/timeslots", response -> {
            try {
                JSONArray arr = new JSONArray(response.toString());
                if(!arr.isNull(0)){
                    JSONObject obj = arr.getJSONObject(0);
                    String description =obj.getString("description");
                    desc.setText(description.equals("")?"Non specificato":description);
                }else{
                    desc.setText("Non specificato");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {

        }, new HashMap<>());


        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/nekomaActivities/" + evento.event_id + "/information", response -> {
            try {
                JSONObject obj = new JSONObject(response.toString());
                String start = obj.getString("start");
                String end = obj.getString("end");

                if(!start.equals("") || !end.equals("")){
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    cal.setTime(sdf.parse(start));

                    startTime = (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR)) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND));

                    startDate.setText(getDate(cal));

                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    cal.setTime(sdf.parse(end));

                    endTime = (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR)) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND));


                    endDate.setText(getDate(cal));

                    if (obj.getString("parents").equals("[]")) {
                        nPart.setText(nPart.getText() + "0");
                    } else {
                        nPart.setText(nPart.getText() + "" + obj.getJSONArray("parents").length());
                    }
                }else{
                    startDate.setText("nan");
                    endDate.setText("nan");
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }, error -> {

        }, new HashMap<>());


        List<String> labelsName = new ArrayList<>();
        labelsId = new ArrayList<>();
        Utilities.httpRequest(this, Request.Method.GET, "/label/" + Utilities.getPrefs(this).getString("group", ""), response -> {
            try {
                JSONArray user_response = new JSONArray((String) response);
                event_labels_id = new HashMap<>();
                for (int j = 0; j < user_response.length(); j++) {
                    JSONObject obj = user_response.getJSONObject(j);
                    if (!eventLabels.contains(obj.getString("name"))) {
                        labelsName.add(obj.getString("name"));
                        labelsId.add(obj.getString("label_id"));
                    }
                    event_labels_id.put(obj.getString("name"), obj.getString("label_id"));
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


    public void modify_event() {
        // Quando lo clicco devo:
        // - aggiungere le etichette
        String start = startDate.getText().toString();
        String end = endDate.getText().toString();
        String descrizione = desc.getText().toString();

        String startFormat = "";
        String endFormat = "";

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdfStart = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            cal.setTime(Objects.requireNonNull(sdfStart.parse(start)));
            startFormat = fromCalToString(cal, startTime);
            cal.setTime(Objects.requireNonNull(sdfStart.parse(end)));
            endFormat = fromCalToString(cal, endTime);


        } catch (ParseException e) {
            e.printStackTrace();
        }

        String url = "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/activities/" + activity_id + "/timeslots";

        String finalStartFormat = startFormat;
        String finalEndFormat = endFormat;

        // Modifica di descrizione e date
        Utilities.httpRequest(DettagliEvento.this, Request.Method.GET, "/groups/"+Utilities.getGroupId(DettagliEvento.this)+"/activities/"+activity_id+"/timeslots", response -> {
            Map<String,String> data = new HashMap<>();
            String timeslot_id;
            try {
                JSONArray arr = new JSONArray((String)response);
                if(!arr.isNull(0)){
                    JSONObject obj = arr.getJSONObject(0);
                    timeslot_id = obj.getString("id");
                    data.put("status",obj.getString("status"));
                    data.put("summary",obj.getString("summary"));
                    data.put("notifyUsers","false");
                    data.put("description",descrizione);
                    data.put("location",obj.getString("location"));
                    data.put("start", finalStartFormat);
                    data.put("end", finalEndFormat);

                    JSONObject prop;
                    if(obj.has("extendedProperties")){
                        prop = obj.getJSONObject("extendedProperties");

                        data.put("cost",prop.has("cost")?prop.getString("cost"):"");
                        data.put("requiredChildren",prop.has("requiredChildren")?prop.getString("requiredChildren"):"[]");
                        data.put("groupId",prop.has("groupId")?prop.getString("groupId"):"");
                        data.put("startHour",prop.has("start")?prop.getString("start"):"");
                        data.put("link",prop.has("link")?prop.getString("link"):"");
                        data.put("requiredParents",prop.has("requiredParents")?prop.getString("requiredParents"):"");
                        data.put("activityId",prop.has("activityId")?prop.getString("activityId"):"");
                        data.put("repetition",prop.has("repetition")?prop.getString("repetition"):"");
                        data.put("activityColor",prop.has("activityColor")?prop.getString("activityColor"):"");
                        data.put("children",prop.has("children")?prop.getString("children"):"[]");
                        data.put("externals",prop.has("externals")?prop.getString("externals"):"[]");
                        data.put("endHour",prop.has("endHour")?prop.getString("end"):"");
                        data.put("category",prop.has("category")?prop.getString("category"):"");
                        data.put("status",prop.has("status")?prop.getString("status"):"");
                        data.put("parents",prop.has("parents")?prop.getString("parents"):"[]");
                    }

                    Utilities.httpRequest(DettagliEvento.this, Request.Method.PATCH, "/groups/"+Utilities.getGroupId(DettagliEvento.this)+"/nekomaActivities/"+activity_id+"/timeslots/"+timeslot_id, response1 -> {
                        //System.out.println(response1);
                        reloadActivity("",false); //! Prima di ricaricare passano secoli ;)
                    }, error -> {
                        Toast.makeText(DettagliEvento.this, "È successo un casino", Toast.LENGTH_SHORT).show();
                    }, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, System.err::println, new HashMap<>());
    }

    public String fromCalToString(Calendar cal, String hour) {
        int month = (cal.get(Calendar.MONTH) + 1);
        int day = (cal.get(Calendar.DAY_OF_MONTH));


        return cal.get(Calendar.YEAR) + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + "T" + hour + "Z";
    }

    public void join_event() {
        // GET del timeslot -> PATCH localhost:8080/api/groups/:id_gruppo/nekomaActivities/:id_attività/timeslots/:id_timeslot
        Toast.makeText(DettagliEvento.this, "Partecipa", Toast.LENGTH_SHORT).show(); // TODO fai in modo che partecipi
    }

    public void newLabel(View v) {
        String id = labelsId.get(spinner.getSelectedItemPosition());
        selectedLabel.add(labelsId.get(spinner.getSelectedItemPosition()));
        dataSpinner.remove(spinner.getSelectedItem());
        labelsId.remove(spinner.getSelectedItemPosition());

        Map<String, String> data = new HashMap<>();
        data.put("label_id", id);
        Utilities.httpRequest(DettagliEvento.this, Request.Method.POST, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/activities/" + activity_id + "/label", System.out::println, System.out::println, data);
        reloadActivity("",true);

    }

    public static String getDate(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
    }


    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

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
            if (isCreator) {
                holder.btn.setVisibility(View.VISIBLE);
            }
            holder.btn.setText("Elimina");
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String label_id = event_labels_id.get(item);
                    Utilities.httpRequest(DettagliEvento.this, Request.Method.DELETE, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/activities/" + activity_id + "/label/" + label_id, response -> {
                        reloadActivity(item, false);
                    }, error -> {
                        Toast.makeText(DettagliEvento.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }, new HashMap<>());
                }
            });
        }

        @Override
        public int getItemCount() {
            return label_list.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView label;
            Button btn;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.label = itemView.findViewById(R.id.label);
                this.btn = itemView.findViewById(R.id.edit_label);
            }
        }
    }

    private void reloadActivity(String labelToErase, boolean labelToAdd) { // Mi fa un pò schifo....
        VisualizzazioneEventi.Evento e = VisualizzazioneEventi.Evento.getEventoFromString(extraData);
        String newLabel = "";
        String[] split = e.labels.split(",");
        for (String s : split) {
            if (!s.equals(labelToErase)) {
                newLabel += s + ",";
            }
        }
        if (labelToAdd) {
            String[] added = selectedLabel.toString().substring(1, selectedLabel.toString().length() - 1).replaceAll("\\s+", "").split(",");
            for (String s : added) {
                for (String key : event_labels_id.keySet()) {
                    if (event_labels_id.get(key).equals(s)) {
                        newLabel += key + ",";
                    }
                }
            }
        }
        Intent intent = getIntent();
        e.labels = newLabel;
        intent.putExtra("evento", e.toString());
        recreate();
    }

    private void addRecyclerView(List<String> list) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.event_label);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DettagliEvento.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(DettagliEvento.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

}