package com.example.nekoma_families_share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DettagliEventoRicorrente extends AppCompatActivity {

    private Utilities.myRecEvent evento;

    private EditText descrizione;
    private ImageView dejoin;

    public DatePickerDialog datePicker;
    private boolean isCreator;
    private boolean isPartecipant;

    private Map<String, String> labels_in_a_group;

    private ArrayList<String> eventLabels;
    private Spinner spinner;
    private ArrayAdapter dataSpinner;
    private TextView yourJoin;
    private List<String> selectedLabel;
    private ArrayList<String> labelName;

    private List<String> spinner_labelID = new ArrayList<>();
    private List<String> spinner_labelName = new ArrayList<>();

    private List<String> selectedDate = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_evento_ricorrente);


        labels_in_a_group = new HashMap<>();
        eventLabels = new ArrayList<>();

        TextView name = findViewById(R.id.eventName);
        descrizione = findViewById(R.id.description);
        TextView nPart = findViewById(R.id.nPart);
        Button btn = findViewById(R.id.button);
        FloatingActionButton addDate = findViewById(R.id.addDate);
        ImageView img = findViewById(R.id.eventImage);
        TextView recurr = findViewById(R.id.textView31);
        spinner = findViewById(R.id.spinner);

        yourJoin = findViewById(R.id.yourJoin);
        dejoin = findViewById(R.id.delete_action);


        // Aggiunta dell'evento torna indietro nella toolbar
        Toolbar t = findViewById(R.id.toolbar2);
        t.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        // Ottengo informazioni dall'activity precedente
        String extraData = intent.getStringExtra("evento");

        selectedLabel = new ArrayList<>();

        evento = new Utilities.myRecEvent(extraData);

//        Aggiunto le informazioni base relative all'evento
        name.setText(evento.nome);
        descrizione.setText(evento.descrizione);

        // Parsing delle etichette
        if(evento.labels.contains("{")){ // devo controllare il formato che mi arriva dal server, se è un json object allora devo parsarlo
            String s = "[";
            try{
                JSONArray arr = new JSONArray(evento.labels);
                for (int i = 0; i < arr.length(); i++) {
                    s+= "\"" + arr.getJSONObject(i).getString("label_id")+"\",";
                }
                if(s.length()>1){
                    s = s.substring(0,s.length()-1);
                }
                s+="]";
                evento.labels = s;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        String[] strings = evento.labels.substring(1, evento.labels.length() - 1).split(",");
        if (!evento.labels.equals("[]")) {
            for (String s : strings) {
                eventLabels.add(s.substring(1, s.length() - 1));
            }
        }

//        Ottengo il numero di partecipanti
        Utilities.httpRequest(this, Request.Method.GET, "/partecipant/nPart/" + evento.event_id, response -> nPart.append((String) response), System.err::println, new HashMap<>());


        // Aggiunta della descrizione riguardante la ricorrenza
        if (evento.recType.equals("daily")) {
            String start = evento.start_date.substring(2, 12);
            String end = evento.end_date.substring(2, 12);
            recurr.setText("Questo evento si svolgerà dal " + start + " a " + end);
        } else if (evento.recType.equals("monthly")) {
            String[] start = evento.start_date.substring(1, evento.start_date.length() - 1).split(",");
            String[] end = evento.end_date.substring(1, evento.end_date.length() - 1).split(",");

            String giorni = "";
            for (String s : start) {
                s = s.substring(1, 11);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                try {
                    c.setTime(Objects.requireNonNull(sdf.parse(s)));
                    giorni += c.get(Calendar.DAY_OF_MONTH) + ",";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            giorni = giorni.substring(0, giorni.length() - 1);

//            Date startDate, endDate;
            String firstMonth = "", lastMonth = "";
            try {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                c.setTime(Objects.requireNonNull(s.parse(start[0].substring(1, 11))));

                firstMonth = getMonth(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);


                c.setTime(Objects.requireNonNull(s.parse(end[0].substring(1, 11))));
                lastMonth = getMonth(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);


            } catch (ParseException e) {
                e.printStackTrace();
            }

            recurr.setText("Questo evento si svolgerà con cadenza mensile nei giorni " + giorni + " a partire da " + firstMonth + " fino a " + lastMonth);

        } else {
            String[] start = evento.start_date.substring(1, evento.start_date.length() - 1).split(",");
            String[] end = evento.end_date.substring(1, evento.end_date.length() - 1).split(",");
            String giorni = "";
            for (String s : start) {
                s = s.substring(1, 11);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                try {
                    c.setTime(Objects.requireNonNull(sdf.parse(s)));
                    switch (c.get(Calendar.DAY_OF_WEEK)) {
                        case 1:
                            giorni += "Domenica,";
                            break;
                        case 2:
                            giorni += "Lunedì,";
                            break;
                        case 3:
                            giorni += "Martedì,";
                            break;
                        case 4:
                            giorni += "Mercoledì,";
                            break;
                        case 5:
                            giorni += "Giovedì,";
                            break;
                        case 6:
                            giorni += "Venerdì,";
                            break;
                        case 7:
                            giorni += "Sabato,";
                            break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            giorni = giorni.substring(0, giorni.length() - 1);

            recurr.setText("Questo evento si svolgerà con cadenza settimanale nei giorni " + giorni + " dal " + start[0].substring(1, 11) + " al " + end[end.length - 1].substring(1, 11));
        }

//    Ottengo le informazioni relative al creatore, nello specifico se l'utente che visiona l'evento è il creatore
        isCreator = evento.owner_id.equals(Utilities.getUserID(this));


        initDatePicker();

//        Se è il creatore allora abilito la possibilà di modifica e gestione delle etichette
        if (isCreator) {
            descrizione.setEnabled(true);
            addDate.setVisibility(View.GONE);
            btn.setText("Modifica");
            yourJoin.setText("");
            dejoin.setVisibility(View.VISIBLE);
            dejoin.setOnClickListener(v -> Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.DELETE, "/recurringActivity/"+evento.event_id, response -> {
                Toast.makeText(DettagliEventoRicorrente.this, "Evento eliminato con successo", Toast.LENGTH_SHORT).show();
                finish();
            }, System.err::println, new HashMap<>()));
            findViewById(R.id.add_label).setVisibility(View.VISIBLE);
            btn.setOnClickListener(v -> {
                Map<String, String> m = new HashMap<>();
                m.put("description", descrizione.getText().toString());
                Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.PUT, "/recurringActivity/" + evento.event_id, response -> {
                    Toast.makeText(DettagliEventoRicorrente.this, "Modifica effettuata con successo", Toast.LENGTH_SHORT).show();
                }, System.out::println, m);
            });
        } else { // Altrimenti abilito la possibilità di gestire la partecipazione
            Utilities.httpRequest(this, Request.Method.GET, "/recurringActivity/isPartecipant/" + evento.event_id, response -> {
                isPartecipant = response.equals("true");

                if (isPartecipant) {
                    dejoin.setVisibility(View.VISIBLE);
                    dejoin.setOnClickListener(v -> {
                        Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.DELETE, "/partecipant/" + evento.event_id, response1 -> {
                            Toast.makeText(DettagliEventoRicorrente.this, "Hai cancellato la tua partecipazione", Toast.LENGTH_SHORT).show();
                            recreate();
                        }, System.err::println, new HashMap<>());
                    });
                    Utilities.httpRequest(DettagliEventoRicorrente.this,Request.Method.GET, "/partecipant/days/"+evento.event_id, success -> {
                        String[] strings1 = ((String) success).substring(1,((String)success).length()-1).split(",");

                        String date = "";
                        for (String s:strings1) {
                            s = s.substring(1,s.length()-1).split("T")[0];
                            date += getDateFromEncoding(s) + ", ";
                            selectedDate.add(s);
                        }
                        if(date.length()>0){
                            yourJoin.append(" " + date.substring(0,date.length()-2));
                        }
                    }, System.err::println, new HashMap<>());
                }else{
                    yourJoin.setText("Aggiungi delle date per partecipare anche tu!");
                }

                addDate.setOnClickListener(v -> openDatePicker(v));

                btn.setOnClickListener(v -> {
                    String toSend = "[";
                    for (String s : selectedDate) {
                        toSend += s + ",";
                    }
                    toSend = toSend.substring(0, toSend.length() - 1) + "]";

                    Map<String, String> m = new HashMap<>();
                    m.put("activity_id", evento.event_id);
                    m.put("days", toSend);

                    if (isPartecipant) {
                        Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.PATCH, "/partecipant/"+evento.event_id, response1 -> Toast.makeText(DettagliEventoRicorrente.this, "Dati aggiornati con successo", Toast.LENGTH_SHORT).show(), error -> Toast.makeText(DettagliEventoRicorrente.this, "Errore, aggiornamento non riuscito", Toast.LENGTH_SHORT).show(), m);
                    } else {
                        Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.POST, "/partecipant", response1 -> {
                            Toast.makeText(DettagliEventoRicorrente.this, "Partipazione effettuata", Toast.LENGTH_SHORT).show();
                            recreate();
                        }, error -> Toast.makeText(DettagliEventoRicorrente.this, "Errore, partecipazione non aggiunta", Toast.LENGTH_SHORT).show(), m);
                    }
                });
            }, System.err::println, new HashMap<>());
        }

//  Scarico l'immagine dell'evento
        new ImageDownloader(img).execute(evento.img);

//        Ottengo le eitchette disponibili in un gruppo
        getLabelsInGroups();


    }


    // Aggiunta delle etichette all'evento
    public void newLabel(View v) {
        String id = spinner_labelID.get(spinner.getSelectedItemPosition());
        selectedLabel.add(spinner_labelID.get(spinner.getSelectedItemPosition()));
        String sel = (String) spinner.getSelectedItem();
        dataSpinner.remove(spinner.getSelectedItem());
        spinner_labelID.remove(spinner.getSelectedItemPosition());

        Map<String, String> data = new HashMap<>();
        data.put("label_id", id);
        Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.PATCH, "/recurringActivity/label/" + evento.event_id, r -> {
        }, e -> {
        }, data);
        labelName.add(sel);
        addRecyclerView(labelName);
    }

    // Questo metodo prende tutte le etichette che sono disponibili in un gruppo e le salva in una mappa, inoltre aggiunge alla recycle view le etichette già assegnate
    private void getLabelsInGroups() {
        Utilities.httpRequest(this, Request.Method.GET, "/label/" + Utilities.getGroupId(this), response -> {
            try {
                JSONArray arr = new JSONArray((String) response);
                labelName = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    labels_in_a_group.put(obj.getString("label_id"), obj.getString("name"));
                    if (eventLabels.contains(obj.getString("label_id"))) {
                        labelName.add(obj.getString("name"));
                    } else {
                        spinner_labelID.add(obj.getString("label_id"));
                        spinner_labelName.add(obj.getString("name"));
                    }
                }
                addRecyclerView(labelName);


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                dataSpinner = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item);
                dataSpinner.addAll(spinner_labelName);
                dataSpinner.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinner.setAdapter(dataSpinner);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, System.err::println, new HashMap<>());
    }

// Calendario per aggiungere le date relative alla partecipazione
    public void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = year + "-" + month + "-" + dayOfMonth;
            if (selectedDate.contains(date)) {
                selectedDate.remove(date);
            } else {
                selectedDate.add(date);
            }

            yourJoin.setText(getString(R.string.yourJoin));
            for (String s : selectedDate) {
                yourJoin.append(" " + getDateFromEncoding(s));
            }
        };
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePicker = new DatePickerDialog(this, dateListener, year, month, day);
    }

//    Parsing della date per ottenere un formato più leggibile
    private String getDateFromEncoding(String date) {
        String[] s = date.split("-");
        return s[2] + "/" + s[1] + "/" + s[0];
    }

    public void openDatePicker(View v) {
        datePicker.show();
    }

    // Classe che permette di scaricare le immagini dell'evento
    private static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        private ImageView holder;

        public ImageDownloader(ImageView holder) {
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlOfImage = strings[0];
            Bitmap logo = null;
            try {
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holder.setImageBitmap(bitmap);
        }
    }

// Matching del numero del mese con il suo nome
    private String getMonth(int month) {
        String firstMonth = "";
        switch (month) {
            case Calendar.JANUARY:
                firstMonth = "Gennaio";
                break;
            case Calendar.FEBRUARY:
                firstMonth = "Febbraio";
                break;
            case Calendar.MARCH:
                firstMonth = "Marzo";
                break;
            case Calendar.APRIL:
                firstMonth = "Aprile";
                break;
            case Calendar.MAY:
                firstMonth = "Maggio";
                break;
            case Calendar.JUNE:
                firstMonth = "Giugno";
                break;
            case Calendar.JULY:
                firstMonth = "Luglio";
                break;
            case Calendar.AUGUST:
                firstMonth = "Agosto";
                break;
            case Calendar.SEPTEMBER:
                firstMonth = "Settembre";
                break;
            case Calendar.OCTOBER:
                firstMonth = "Ottobre";
                break;
            case Calendar.NOVEMBER:
                firstMonth = "Novembre";
                break;
            case Calendar.DECEMBER:
                firstMonth = "Dicembre";
                break;

        }
        return firstMonth;
    }


    // Aggiunta dei dati nella recycle view
    private void addRecyclerView(List<String> list) {
        RecyclerView recyclerView = findViewById(R.id.event_label);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DettagliEventoRicorrente.this);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(DettagliEventoRicorrente.this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
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
            holder.btn.setOnClickListener(v -> {
                List<String> ids = new ArrayList<>(labels_in_a_group.values());
                String label_id = ids.get(ids.indexOf(item));
                Utilities.httpRequest(DettagliEventoRicorrente.this, Request.Method.DELETE, "/recurringActivity/label/" + evento.event_id + "/" + label_id, response -> {
                    labelName.remove(item);
                    addRecyclerView(labelName);
                    spinner_labelID.add(label_id);
                    spinner_labelName.add(item);
                    dataSpinner.add(item);
                    spinner.setAdapter(dataSpinner);
                }, error -> Toast.makeText(DettagliEventoRicorrente.this, error.toString(), Toast.LENGTH_SHORT).show(), new HashMap<>());
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

}