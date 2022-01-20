package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChild extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String user_id = "";
    private DatePickerDialog datePicker;
    private EditText childName;
    private EditText childSurname;
    private Spinner childGender;
    private Spinner labelsSpinner;
    private Button dateButton;
    private EditText childAllergies;
    private EditText childOtherInfos;
    private EditText childSpecialNeeds;
    private Button confirmButton;

    private List<String> labelsName;
    private List<String> labelsId;
    private List<String> childLabels = new ArrayList<>();
    private ArrayAdapter dataSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);

        Toolbar t = (Toolbar) findViewById(R.id.newchild_toolbar);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        childName = findViewById(R.id.childName);
        childSurname = findViewById(R.id.childSurname);
        childGender = findViewById(R.id.childGender);
        childAllergies = findViewById(R.id.childAllergies);
        childOtherInfos = findViewById(R.id.childOtherInfo);
        childSpecialNeeds = findViewById(R.id.childSpecialNeeds);
        confirmButton = findViewById(R.id.confirmlabel);
        labelsSpinner = (Spinner) findViewById(R.id.labelsSpinner);

        // Token dell'utente, mi permette di fare le chiamate al server
        String userToken = Utilities.getToken(this);
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Lista che contiene i nomi delle etichette che possono essere associate ad un bambino (usata solo per riferimento grafico)
        labelsName = new ArrayList<>();
        // Lista che contiene gli id delle etichette (usata per le operazioni con il db)
        labelsId = new ArrayList<>();
        // Chiamata al server che popola le liste con le etichette presenti nel gruppo
        Utilities.httpRequest(this,Request.Method.GET,"/label/"+Utilities.getPrefs(this).getString("group",""),response -> {
            try {
                JSONArray user_response = new JSONArray((String) response);
                for (int i = 0; i < user_response.length(); i++) {
                    JSONObject obj = user_response.getJSONObject(i);
                    labelsName.add(obj.getString("name"));
                    labelsId.add(obj.getString("label_id"));
                }
                labelsSpinner.setOnItemSelectedListener(this);
                dataSpinner = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item);
                dataSpinner.addAll(labelsName);
                dataSpinner.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                // Inserisco i risultati nello spinner
                labelsSpinner.setAdapter(dataSpinner);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(NewChild.this, error.toString(), Toast.LENGTH_LONG).show();
        }, new HashMap<>());

        // Metodo che inizializza il date picker per la data di nascita del bambino
        initDatePicker();
        dateButton = findViewById(R.id.dateButton);
        dateButton.setText("Seleziona una data");

        // Popolo lo spinner del genere del bambino utilizzando un array statico di elementi specificato nelle resources
        childGender = (Spinner) findViewById(R.id.childGender);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.gendervalues, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        childGender.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"/"+month+"/"+dayOfMonth;
                dateButton.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePicker = new DatePickerDialog(this,dateListener,year,month,day);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
    }

    // Metodo che permette di aggiungere i dati di un nuovo bambino nel database
    public void setNewChild(View v){
        Map<String,String> params = new HashMap<>();
        params.put("birthdate",dateButton.getText().toString());
        params.put("given_name",childName.getText().toString());
        params.put("family_name",childSurname.getText().toString());
        params.put("gender",childGender.getSelectedItem().toString());
        params.put("allergies",childAllergies.getText().toString());
        params.put("other_info",childOtherInfos.getText().toString());
        params.put("special_needs",childSpecialNeeds.getText().toString());
        params.put("background","#00838F");
        params.put("labels", childLabels.toString());
        params.put("image","/images/profiles/child_default_photo.png");

        Utilities.httpRequest(this,Request.Method.POST,"/users/"+user_id+"/children",response -> {
            Toast.makeText(NewChild.this, "Nuovo figlio aggiunto correttamente!", Toast.LENGTH_LONG).show();
            Intent homepage = new Intent(NewChild.this,Homepage.class);
            startActivity(homepage);
        },error -> {
            Toast.makeText(NewChild.this, "Devi specificare: nome,cognome,data di nascita e genere!", Toast.LENGTH_LONG).show();
        },params);
    }

    // Metodo che permette di aggiungere ad un bambino una specifica etichetta, e la rimuove dallo spinner
    public void newLabel(View v){
        childLabels.add(labelsId.get(labelsSpinner.getSelectedItemPosition()));
        dataSpinner.remove(labelsSpinner.getSelectedItem());
        labelsId.remove(labelsSpinner.getSelectedItemPosition());
        if(labelsId.size()==0){
            confirmButton.setEnabled(false);
        }
    }

    public void openDatePicker(View v){
        datePicker.show();
    }


    public void getChildrenList(View v){
        Intent childrenList= new Intent(NewChild.this,ListaBambiniAmici.class);
        startActivity(childrenList);
    }
}