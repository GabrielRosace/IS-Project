package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

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

        System.out.println(Utilities.getToken(this));

        childName = findViewById(R.id.childName);
        childSurname = findViewById(R.id.childSurname);
        childGender = findViewById(R.id.childGender);
        childAllergies = findViewById(R.id.childAllergies);
        childOtherInfos = findViewById(R.id.childOtherInfo);
        childSpecialNeeds = findViewById(R.id.childSpecialNeeds);
        confirmButton = findViewById(R.id.confirmlabel);
        labelsSpinner = (Spinner) findViewById(R.id.labelsSpinner);

        String userToken = Utilities.getToken(this);
        System.out.println(userToken);
        // Faccio il parse del token in modo tale da prendermi l'id dell'utente
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        labelsName = new ArrayList<>();
        labelsId = new ArrayList<>();
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
                labelsSpinner.setAdapter(dataSpinner);
                for (int i = 0; i < labelsId.size(); i++) {
                    System.out.println("ID :"+labelsId.get(i));
                }
                for (int i = 0; i < labelsName.size(); i++) {
                    System.out.println("NAME :"+labelsName.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(NewChild.this, error.toString(), Toast.LENGTH_LONG).show();
            System.err.println(error.getMessage());
        }, new HashMap<>());

        initDatePicker();
        dateButton = findViewById(R.id.dateButton);
        dateButton.setText("Select a date");

        childGender = (Spinner) findViewById(R.id.childGender);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.gendervalues, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        childGender.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

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
    }

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
            Toast.makeText(NewChild.this, "New child added succesfully!", Toast.LENGTH_LONG).show();
            Intent homepage = new Intent(NewChild.this,Homepage.class);
            startActivity(homepage);
        },error -> {
            Toast.makeText(NewChild.this, "You have to specify: name, surname, birthdate, gender", Toast.LENGTH_LONG).show();
            System.err.println(error.getMessage());
        },params);
    }

    public void newLabel(View v){
        childLabels.add(labelsId.get(labelsSpinner.getSelectedItemPosition()));
        dataSpinner.remove(labelsSpinner.getSelectedItem());
        labelsId.remove(labelsSpinner.getSelectedItemPosition());
        if(labelsId.size()==0){
            confirmButton.setEnabled(false);
        }
        for (int i = 0; i < childLabels.size(); i++) {
            System.out.println(childLabels.get(i));
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