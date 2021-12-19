package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NuovoServizio extends AppCompatActivity {

    private EditText serviceImageUrl;
    private EditText serviceName;
    private EditText serviceDescription;
    private EditText serviceLocation;
    private Spinner servicePattern;
    private TextView serviceCarLabel;
    private TextView serviceLendObjLabel;
    private TextView serviceLendTimeLabel;
    private TextView servicePickupLabel;
    private EditText serviceCarSpace;
    private EditText serviceLendObj;
    private EditText servicePickup;
    private Button serviceLendTime;
    private CheckBox serviceRecurrence;
    private Spinner serviceType;
    private RadioGroup serviceDays;
    private Button serviceStartDate;
    private Button serviceEndDate;
    private TextView serviceEndDateLabel;
    private TextView serviceDaysLabel;

    private CheckBox checkMon;
    private CheckBox checkTue;
    private CheckBox checkWen;
    private CheckBox checkThu;
    private CheckBox checkFri;
    private CheckBox checkSat;
    private CheckBox checkSun;

    private DatePickerDialog dataStartPicker;
    private DatePickerDialog dataEndPicker;
    private DatePickerDialog dataLendPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuovo_servizio);

        Toolbar t = (Toolbar) findViewById(R.id.nsToolbar);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //Edit Text da inizializzare
        serviceImageUrl = findViewById(R.id.serviceImageUrl);
        serviceName = findViewById(R.id.serviceName);
        serviceDescription = findViewById(R.id.serviceDescription);
        serviceLocation = findViewById(R.id.serviceLocation);
        //CheckBox da inizializzare
        checkMon = findViewById(R.id.checkMon);
        checkTue = findViewById(R.id.checkTue);
        checkWen = findViewById(R.id.checkWen);
        checkThu = findViewById(R.id.checkThu);
        checkFri = findViewById(R.id.checkFri);
        checkSat = findViewById(R.id.checkSat2);
        checkSun = findViewById(R.id.checkSun);
        //Nascondo le labels che riguardano il pattern del servizio
        serviceCarLabel = findViewById(R.id.carspaceServiceLabel);
        serviceCarLabel.setVisibility(View.INVISIBLE);
        serviceLendObjLabel = findViewById(R.id.lendobjServiceLabel);
        serviceLendObjLabel.setVisibility(View.INVISIBLE);
        serviceLendTimeLabel = findViewById(R.id.lendtimeServiceLabel);
        serviceLendTimeLabel.setVisibility(View.INVISIBLE);
        servicePickupLabel = findViewById(R.id.pickupServiceLabel);
        servicePickupLabel.setVisibility(View.INVISIBLE);
        //Nascondo i vari EditText corrispondenti
        serviceCarSpace = findViewById(R.id.serviceCarSpace);
        serviceCarSpace.setVisibility(View.INVISIBLE);
        servicePickup = findViewById(R.id.servicePickup);
        servicePickup.setVisibility(View.INVISIBLE);
        serviceLendObj = findViewById(R.id.serviceLendObj);
        serviceLendObj.setVisibility(View.INVISIBLE);
        serviceLendTime = findViewById(R.id.serviceLendStartDate);
        serviceLendTime.setVisibility(View.INVISIBLE);
        //Disabilito i componenti che si attivano solo se si vuole una ricorrenza
        serviceRecurrence = findViewById(R.id.serviceCheck);
        serviceType = findViewById(R.id.serviceType);
        serviceType.setEnabled(false);
        serviceDaysLabel = findViewById(R.id.daysRecLabel);
        serviceDaysLabel.setVisibility((View.GONE));
        serviceStartDate = findViewById(R.id.serviceStartDate);
        serviceEndDate = findViewById(R.id.serviceEndDate);
        serviceEndDate.setVisibility(View.GONE);
        serviceEndDateLabel = findViewById(R.id.endDateLabel);
        serviceEndDateLabel.setVisibility(View.GONE);
        //Inizializzo i date picker
        initStartDatePicker();
        initEndDatePicker();
        initLendDatePicker();
        serviceStartDate.setText("Seleziona una data");
        serviceEndDate.setText("Seleziona una data");
        serviceLendTime.setText("Seleziona una data");
        //Popolo Service Type spinner
        serviceType = (Spinner) findViewById(R.id.serviceType);
        ArrayAdapter adapterType = ArrayAdapter.createFromResource(this, R.array.cadenza, R.layout.support_simple_spinner_dropdown_item);
        adapterType.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        serviceType.setAdapter(adapterType);
        //Popolo Service Pattern spinner
        servicePattern = (Spinner) findViewById(R.id.servicePattern);
        ArrayAdapter adapterPattern = ArrayAdapter.createFromResource(this, R.array.serviceType, R.layout.support_simple_spinner_dropdown_item);
        adapterPattern.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        servicePattern.setAdapter(adapterPattern);
        //Listener sull'elemento selezionato nello spinner
        servicePattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(adapterPattern.getItem(position).toString()){
                    case "Carsharing":
                        serviceCarSpace.setVisibility(View.VISIBLE);
                        serviceCarLabel.setVisibility(View.VISIBLE);
                        serviceLendTime.setVisibility(View.INVISIBLE);
                        serviceLendTimeLabel.setVisibility(View.INVISIBLE);
                        serviceLendObj.setVisibility(View.INVISIBLE);
                        serviceLendObjLabel.setVisibility(View.INVISIBLE);
                        servicePickup.setVisibility(View.INVISIBLE);
                        servicePickupLabel.setVisibility(View.INVISIBLE);
                        break;
                    case "Prestito oggetti":
                        serviceLendTime.setVisibility(View.VISIBLE);
                        serviceLendObj.setVisibility(View.VISIBLE);
                        serviceLendTimeLabel.setVisibility(View.VISIBLE);
                        serviceLendObjLabel.setVisibility(View.VISIBLE);
                        serviceCarSpace.setVisibility(View.INVISIBLE);
                        serviceCarLabel.setVisibility(View.INVISIBLE);
                        servicePickup.setVisibility(View.INVISIBLE);
                        servicePickupLabel.setVisibility(View.INVISIBLE);
                        break;
                    case "Servizio alle persone":
                        serviceCarSpace.setVisibility(View.INVISIBLE);
                        serviceCarLabel.setVisibility(View.INVISIBLE);
                        serviceLendTime.setVisibility(View.INVISIBLE);
                        serviceLendTimeLabel.setVisibility(View.INVISIBLE);
                        serviceLendObj.setVisibility(View.INVISIBLE);
                        serviceLendObjLabel.setVisibility(View.INVISIBLE);
                        servicePickup.setVisibility(View.VISIBLE);
                        servicePickupLabel.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        serviceRecurrence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    serviceType.setEnabled(true);
                    serviceEndDate.setVisibility(View.VISIBLE);
                    serviceEndDateLabel.setVisibility(View.VISIBLE);
                }else{
                    serviceType.setSelection(0);
                    serviceType.setEnabled(false);
                    serviceEndDate.setVisibility(View.GONE);
                    serviceEndDateLabel.setVisibility(View.GONE);
                }
            }
        });
        serviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(adapterType.getItem(position).toString()){
                    case "Giornaliera":
                        serviceEndDate.setVisibility(View.GONE);
                        serviceEndDateLabel.setVisibility(View.GONE);
                        break;
                    default:
                        serviceEndDate.setVisibility(View.VISIBLE);
                        serviceEndDateLabel.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initStartDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"/"+month+"/"+dayOfMonth;
                serviceStartDate.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataStartPicker = new DatePickerDialog(this,dateListener,year,month,day);
    }

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initEndDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"/"+month+"/"+dayOfMonth;
                serviceEndDate.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataEndPicker = new DatePickerDialog(this,dateListener,year,month,day);
    }

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initLendDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"/"+month+"/"+dayOfMonth;
                serviceLendTime.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        dataLendPicker = new DatePickerDialog(this,dateListener,year,month,day);
    }

    public void openDateStartPicker(View v){
        dataStartPicker.show();
    }
    public void openDateEndPicker(View v){
        dataEndPicker.show();
    }
    public void openDateLendPicker(View v){
        dataLendPicker.show();
    }

    public void newService(View v) throws ParseException {
        Map<String,String> params = new HashMap<>();
        params.put("group_id",Utilities.getGroupId(this));
        params.put("owner_id",Utilities.getUserID(this));
        params.put("name",serviceName.getText().toString());
        params.put("description",serviceDescription.getText().toString());
        params.put("location",serviceLocation.getText().toString());
        params.put("pattern",servicePattern.getSelectedItem().toString());
        //controlli in base al pattern
        if(servicePattern.getSelectedItem().toString()=="Carsharing"){
            params.put("car_space",serviceCarSpace.getText().toString());
        }else if(servicePattern.getSelectedItem().toString()=="Prestito oggetti"){
            params.put("lend_obj",serviceLendObj.getText().toString());
            params.put("lend_time",serviceLendTime.getText().toString());
        }else{
            params.put("pickupLocation",servicePickup.getText().toString());
        }
        params.put("img",serviceImageUrl.getText().toString());
        params.put("recurrence","true");
        params.put("nPart","0");

        //Generazione delle date
        //Servizio giornaliero (senza enddate)
        if(!serviceRecurrence.isChecked()){
            params.put("type","daily");
            params.put("start_date","["+serviceStartDate.getText().toString()+"]");
            params.put("end_date","["+serviceStartDate.getText().toString()+"]");
        }else if(serviceRecurrence.isChecked() && serviceType.getSelectedItem().toString().equals("Giornaliera")){
            params.put("type","daily");
            params.put("start_date","["+serviceStartDate.getText().toString()+"]");
            params.put("end_date","["+serviceEndDate.getText().toString()+"]");
        }
        /*else if(serviceRecurrence.isChecked() && serviceType.getSelectedItem().toString().equals("Settimanale")){
            params.put("type","weekly");
        }else if(serviceRecurrence.isChecked() && serviceType.getSelectedItem().toString().equals("Mensilmente")){
            params.put("type","monthly");
        }*/

        Utilities.httpRequest(this, Request.Method.POST,"/groups/"+Utilities.getGroupId(this)+"/service",response -> {
            Toolbar t = (Toolbar) findViewById(R.id.nsToolbar);
            t.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        },error -> {

        },params);
    }
}