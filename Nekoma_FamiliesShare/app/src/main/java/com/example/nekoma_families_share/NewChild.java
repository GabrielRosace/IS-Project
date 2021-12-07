package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewChild extends AppCompatActivity {

    private String user_id = "";
    private DatePickerDialog datePicker;
    private EditText childName;
    private EditText childSurname;
    private Spinner childGender;
    private Button dateButton;
    private EditText childAllergies;
    private EditText childOtherInfos;
    private EditText childSpecialNeeds;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);

        childName = findViewById(R.id.childName);
        childSurname = findViewById(R.id.childSurname);
        childGender = findViewById(R.id.childGender);
        childAllergies = findViewById(R.id.childAllergies);
        childOtherInfos = findViewById(R.id.childOtherInfo);
        childSpecialNeeds = findViewById(R.id.childSpecialNeeds);

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

        initDatePicker();
        dateButton = findViewById(R.id.dateButton);
        dateButton.setText("Select a date");

        childGender = (Spinner) findViewById(R.id.childGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gendervalues, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        childGender.setAdapter(adapter);
    }

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
        RequestQueue newChild = Volley.newRequestQueue(this);
        String url= getString(R.string.url) + "/users/"+user_id+"/children";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(NewChild.this, "New child added succesfully!", Toast.LENGTH_LONG).show();
                Intent homepage = new Intent(NewChild.this,Homepage.class);
                startActivity(homepage);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(NewChild.this, "You have to specify: name, surname, birthdate, gender", Toast.LENGTH_LONG).show();
                System.err.println(error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","Bearer "+Utilities.getToken(NewChild.this));
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("birthdate",dateButton.getText().toString());
                params.put("given_name",childName.getText().toString());
                params.put("family_name",childSurname.getText().toString());
                params.put("gender",childGender.getSelectedItem().toString());
                params.put("allergies",childAllergies.getText().toString());
                params.put("other_info",childOtherInfos.getText().toString());
                params.put("special_needs",childSpecialNeeds.getText().toString());
                params.put("background","#00838F");
                params.put("image","\"\\/images\\/profiles\\/child_default_photo.png\"");
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        newChild.add(stringRequest);
    }

    public void openDatePicker(View v){
        datePicker.show();
    }


    public void getChildrenList(View v){
        Intent childrenList= new Intent(NewChild.this,ListaBambiniAmici.class);
        startActivity(childrenList);
    }
}