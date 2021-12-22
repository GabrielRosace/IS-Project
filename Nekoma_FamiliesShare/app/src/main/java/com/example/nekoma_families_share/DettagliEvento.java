package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
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
    private List<String> selectedLabel;
    private Spinner spinner;
    private Map<String, String> event_labels_id;

    private List<String> eventLabels;

    private EditText endDate;
    private EditText startDate;
    private EditText desc;

    private String extraData;

    private String startTime;
    private String endTime;
    private String timeSlot;
    private String timeSlot_id;

    public DatePickerDialog datePickerStart;
    public DatePickerDialog datePickerEnd;

    public ConstraintLayout progress_layout;
    public ProgressBar progress_bar;


    public void deleteActivity(View v) {
        Utilities.httpRequest(this, Request.Method.DELETE, "/groups/" + Utilities.getGroupId(this) + "/activities/" + activity_id, response -> {
            finish();
        }, System.err::println, new HashMap<>());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento);

        progress_layout = (ConstraintLayout) findViewById(R.id.progress_layout_det);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar_det);

        // Aggiunta dell'evento torna indietro nella toolbar
        Toolbar t = (Toolbar) findViewById(R.id.toolbar2);
        t.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        // Ottengo informazioni dall'activity precedente
        extraData = intent.getStringExtra("evento");


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


        VisualizzazioneEventi.Evento evento = VisualizzazioneEventi.Evento.getEventoFromString(extraData); // Parsing dei parametri dell'intent


        activity_id = evento.event_id;
        creator_id = evento.owner_id;

        isCreator = Utilities.getUserID(this).equals(creator_id);

        getTimeslot(); // Ottengo le informazioni riguardo al timeslot

        if (isCreator) { // Se chi sta visualizzando i dettagli dell'evento è il creatore allora gli fornisco la possibilità di modificarlo
            findViewById(R.id.delete_action).setVisibility(View.VISIBLE);
            misc.setText("Modifica");
            desc.setEnabled(true);

            startDate.setEnabled(true);
            startDate.setFocusable(false);
            startDate.setClickable(true);
            startDate.setOnClickListener(v -> openDatePickerStart(v));

            endDate.setEnabled(true);
            endDate.setFocusable(false);
            endDate.setClickable(true);
            endDate.setOnClickListener(v -> openDatePickerEnd(v));


            findViewById(R.id.add_label).setVisibility(View.VISIBLE);
            misc.setOnClickListener(v -> modify_event());
        } else { // Altrimenti può solo visualizzare i dati e dichiarare la partecipazione
            Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/nekomaActivities/" + activity_id + "/information", response -> {
                try {
                    JSONObject obj = new JSONObject((String) response);
                    if (obj.has("parents")) {
                        boolean found = false;
                        if (!obj.getString("parents").equals("[]") && !obj.getString("parents").equals(""))  {
                            JSONArray arr = obj.getJSONArray("parents");
                            for (int i = 0; i < arr.length() && !found; i++) {
                                if (arr.getString(i).equals(Utilities.getUserID(this))) {
                                    found = true;
                                }
                            }

                        }
                        if (!found) {
                            misc.setText("Partecipa");
                            misc.setOnClickListener(v -> join_event());
                        } else {
                            misc.setText("Cancella part.");
                            misc.setOnClickListener(v -> dejoin_event());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
            }, new HashMap<>());
        }

        if (evento.labels.equals("")) { // Non è presente nessuna etichetta
            eventLabels = new ArrayList<>();
        } else {
            eventLabels = Arrays.asList(evento.labels.split(","));
        }
        addRecyclerView(eventLabels); // Aggiungo le informazioni dell'etichette alla view


        eventName.setText(evento.nome);
        if (evento.img.equals("nan")) { // Non è stata specificata nessun immagine, ne setto una di default
            img.setImageDrawable(getDrawable(R.drawable.persone));
        } else { // Scarico l'immagine dal server e la aggiungo alla view
            Utilities.httpRequest(DettagliEvento.this, Request.Method.GET, "/image/"+evento.img, response -> {
                String url="";
                try {
                    JSONObject obj = new JSONObject((String)response);
                    if(obj.has("url") && !obj.getString("url").equals("image.url")){
                        if(obj.getString("url").contains(".svg")){
                            url = getString(R.string.urlnoapi)+obj.getString("path");
                        }else{
                            url = obj.getString("url");

                        }
                    }else{
                        url = getString(R.string.urlnoapi)+obj.getString("path");
                    }
                    new ImageDownloader(img).execute(url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, System.err::println, new HashMap<>());

        }



        // Ottengo le informazioni riguardo al timeslot e aggiorna la view
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/activities/" + activity_id + "/timeslots", response -> {
            try {
                JSONArray arr = new JSONArray(response.toString());
                if (!arr.isNull(0)) {
                    JSONObject obj = arr.getJSONObject(0);
                    String description;
                    if (obj.has("description")) {
                        description = obj.getString("description");
                    } else {
                        description = "";
                    }
                    desc.setText(description.equals("") ? "Non specificato" : description);
                } else {
                    desc.setText("Non specificato");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
        }, new HashMap<>());

        // Ottengo i partecipanti e la data relativi all'evento
        Utilities.httpRequest(this, Request.Method.GET, "/groups/" + Utilities.getGroupId(this) + "/nekomaActivities/" + evento.event_id + "/information", response -> {
            try {
                JSONObject obj = new JSONObject(response.toString());
                String start = obj.getString("start");
                String end = obj.getString("end");

                if (!start.equals("") || !end.equals("")) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    cal.setTime(sdf.parse(start));

                    startTime = (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR)) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND));

                    startDate.setText(getDate(cal));

                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    cal.setTime(sdf.parse(end));

                    endTime = (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR)) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND));


                    endDate.setText(getDate(cal));

                    try {
                        initDatePickerStart();
                        initDatePickerEnd();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (obj.getString("parents").equals("[]") || obj.getString("parents").equals("") || obj.getString("parents").equals("[\"\"]")) {
                        nPart.setText(nPart.getText() + "0");
                    } else {
                        String[] str = obj.getString("parents").substring(1, obj.getString("parents").length() - 1).split(",");
                        nPart.setText(nPart.getText() + "" + str.length);
                    }
                } else {
                    startDate.setText("nan");
                    endDate.setText("nan");
                }
                // Rimuovo la view di caricamento
                progress_layout.setVisibility(View.GONE);
                progress_bar.setVisibility(View.GONE);

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }, error -> {
        }, new HashMap<>());

        // Colleziono l'etichette che sono disponibili in un gruppo, così da permettere l'aggiunta mediante uno spinner al creatore
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
        String start = startDate.getText().toString();
        String end = endDate.getText().toString();
        String descrizione = desc.getText().toString();

        String startFormat = "";
        String endFormat = "";

        // Parsing delle date secondo il formato richiesto
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

        // Aggiornamento della data e delle descrizione
        Map<String, String> data = new HashMap<>();
        try {
            JSONArray arr = new JSONArray((String) timeSlot);
            if (!arr.isNull(0)) {
                fillMapTimeslot(data, timeSlot, descrizione, finalStartFormat, finalEndFormat, "");
                Utilities.httpRequest(DettagliEvento.this, Request.Method.PATCH, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/nekomaActivities/" + activity_id + "/timeslots/" + timeSlot_id, response1 -> {
                    reloadActivity("", false);
                }, error -> {
                    Toast.makeText(DettagliEvento.this, "Hai impostato delle date errate", Toast.LENGTH_SHORT).show();
                }, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Ottengo le informazioni del timeslot riguardanti l'evento
    public void getTimeslot() {
        Utilities.httpRequest(DettagliEvento.this, Request.Method.GET, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/activities/" + activity_id + "/timeslots", response -> {
            Map<String, String> data = new HashMap<>();
            try {
                JSONArray arr = new JSONArray((String) response);
                if (!arr.isNull(0)) {
                    JSONObject obj = arr.getJSONObject(0);
                    timeSlot_id = obj.getString("id");
                    timeSlot = (String) response;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.err::println, new HashMap<>());
    }

    // Creo l'oggetto che contiene il body della richiesta per l'aggiornamento del timeslot
    public Map<String, String> fillMapTimeslot(Map<String, String> data, String response, String descrizione, String finalStartFormat, String finalEndFormat, String partecipants) throws JSONException {
        JSONArray arr = new JSONArray((String) response);
        if (!arr.isNull(0)) {
            JSONObject obj = arr.getJSONObject(0);
            data.put("status", obj.getString("status"));
            data.put("summary", obj.getString("summary"));
            data.put("notifyUsers", "false");
            data.put("description", descrizione);
            data.put("location", obj.getString("location"));
            data.put("start", finalStartFormat);
            data.put("end", finalEndFormat);

            JSONObject prop;
            if (obj.has("extendedProperties")) {
                prop = obj.getJSONObject("extendedProperties").getJSONObject("shared");

                data.put("cost", prop.has("cost") ? prop.getString("cost") : "");
                data.put("requiredChildren", prop.has("requiredChildren") ? prop.getString("requiredChildren") : "");
                data.put("groupId", prop.has("groupId") ? prop.getString("groupId") : "");
                data.put("startHour", prop.has("start") ? prop.getString("start") : "");
                data.put("link", prop.has("link") ? prop.getString("link") : "");
                data.put("requiredParents", prop.has("requiredParents") ? prop.getString("requiredParents") : "");
                data.put("activityId", prop.has("activityId") ? prop.getString("activityId") : "");
                data.put("repetition", prop.has("repetition") ? prop.getString("repetition") : "");
                data.put("activityColor", prop.has("activityColor") ? prop.getString("activityColor") : "");
                data.put("children", "[]");
                data.put("externals", prop.has("externals") ? prop.getString("externals") : "[]");
                data.put("endHour", prop.has("endHour") ? prop.getString("end") : "");
                data.put("category", prop.has("category") ? prop.getString("category") : "");
                data.put("status", prop.has("status") ? prop.getString("status") : "");
                data.put("parents", partecipants);
            }
        }
        return data;
    }

    // Parsing della data
    public String fromCalToString(Calendar cal, String hour) {
        int month = (cal.get(Calendar.MONTH) + 1);
        int day = (cal.get(Calendar.DAY_OF_MONTH));


        return cal.get(Calendar.YEAR) + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + "T" + hour + "Z";
    }

    // Richiesta di partecipare ad un evento
    public void join_event() {
        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);

        Map<String, String> data = new HashMap<>();
        try {
            JSONArray arr = new JSONArray((String) timeSlot);
            if (!arr.isNull(0)) {
                JSONObject obj = arr.getJSONObject(0);
                String descrizione = obj.has("description") ? obj.getString("description") : "";
                String finalStartFormat = obj.getJSONObject("start").getString("dateTime");
                String finalEndFormat = obj.getJSONObject("end").getString("dateTime");
                String partecipants = "[" + Utilities.getUserID(this) + "]";


                fillMapTimeslot(data, timeSlot, descrizione, finalStartFormat, finalEndFormat, partecipants);
                Utilities.httpRequest(DettagliEvento.this, Request.Method.PATCH, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/nekomaActivities/" + activity_id + "/timeslots/" + timeSlot_id, response1 -> {
                    reloadActivity("", false);
                }, error -> {
                    Toast.makeText(DettagliEvento.this, error.toString(), Toast.LENGTH_SHORT).show();
                }, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Richiesta di annullamento della partecipazione ad un evento
    public void dejoin_event() {
        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);

        Map<String, String> data = new HashMap<>();
        try {
            JSONArray arr = new JSONArray((String) timeSlot);
            if (!arr.isNull(0)) {
                JSONObject obj = arr.getJSONObject(0);
                String descrizione = obj.has("description") ? obj.getString("description") : "";
                String finalStartFormat = obj.getJSONObject("start").getString("dateTime");
                String finalEndFormat = obj.getJSONObject("end").getString("dateTime");
                String payload = "[]";


                fillMapTimeslot(data, timeSlot, descrizione, finalStartFormat, finalEndFormat, payload);

                Utilities.httpRequest(DettagliEvento.this, Request.Method.PATCH, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/nekomaActivities/" + activity_id + "/timeslots/" + timeSlot_id, response1 -> {
                    reloadActivity("", false);
                }, error -> {
                    Toast.makeText(DettagliEvento.this, error.toString(), Toast.LENGTH_SHORT).show();
                }, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Aggiunta delle etichette all'evento
    public void newLabel(View v) {
        String id = labelsId.get(spinner.getSelectedItemPosition());
        selectedLabel.add(labelsId.get(spinner.getSelectedItemPosition()));
        dataSpinner.remove(spinner.getSelectedItem());
        labelsId.remove(spinner.getSelectedItemPosition());

        Map<String, String> data = new HashMap<>();
        data.put("label_id", id);
        Utilities.httpRequest(DettagliEvento.this, Request.Method.POST, "/groups/" + Utilities.getGroupId(DettagliEvento.this) + "/activities/" + activity_id + "/label", System.out::println, System.out::println, data);
        reloadActivity("", true);

    }

    public static String getDate(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
    }

    // Classe per la gestione del recycle view, permette la visualizzazione delle etichette
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


    // Ricarica l'activity aggiornando i dati dopo che è stata effettuata una modifica
    private void reloadActivity(String labelToErase, boolean labelToAdd) {
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
        finish();
        e.labels = newLabel;
        intent.putExtra("evento", e.toString());
        startActivity(intent);
    }


    // Aggiunta dei dati nella recycle view
    private void addRecyclerView(List<String> list) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.event_label);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DettagliEvento.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(DettagliEvento.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // Classe che permette di scaricare le immagini dell'evento
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView holder;

        public ImageDownloader(ImageView holder) {
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
            holder.setImageBitmap(bitmap);
        }
    }


    // Gestione del datepicker per l'aggiornamento della data di inizio dell'evento
    public void openDatePickerStart(View v) {
        datePickerStart.show();
    }

    // Gestione del datepicker per l'aggiornamento della data di inizio dell'evento
    public void openDatePickerEnd(View v){
        datePickerEnd.show();
    }

    // Inizializzazione dei date picker
    private void initDatePickerStart() throws ParseException {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth+"/"+month+"/"+year;
                startDate.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(startDate.getText().toString()));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerStart = new DatePickerDialog(this,dateListener,year,month,day);
    }

    private void initDatePickerEnd() throws ParseException {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth+"/"+month+"/"+year;
                endDate.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(endDate.getText().toString()));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerEnd = new DatePickerDialog(this,dateListener,year,month,day);
    }
}