package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NuovoServizio extends AppCompatActivity {

    private EditText serviceImageUrl;
    private EditText serviceName;
    private EditText serviceDescription;
    private EditText serviceLocation;
    private EditText serviceCarSpace;
    private EditText serviceLendObj;
    private EditText servicePickup;
    private EditText serviceNOfMonths;

    private TextView serviceCarLabel;
    private TextView serviceLendObjLabel;
    private TextView serviceLendTimeLabel;
    private TextView servicePickupLabel;
    private TextView serviceStartDateLabel;
    private TextView serviceEndDateLabel;
    private TextView serviceDaysLabel;
    private TextView serviceMonthLabel;
    private TextView serviceNOfMonthsLabel;
    private TextView serviceListofDates;

    private Button serviceStartDate;
    private Button serviceEndDate;
    private ImageView serviceSave;
    private Button serviceMonthDate;
    private Button serviceLendTime;

    private Spinner serviceType;
    private Spinner servicePattern;

    private CheckBox serviceRecurrence;
    private CheckBox checkMon;
    private CheckBox checkTue;
    private CheckBox checkWen;
    private CheckBox checkThu;
    private CheckBox checkFri;
    private CheckBox checkSat;
    private CheckBox checkSun;

    private List<String> startWeek = new ArrayList<>();
    private List<String> endWeek = new ArrayList<>();
    private List<String> monthRec;
    private List<String> monthEnd;

    private DatePickerDialog dataStartPicker;
    private DatePickerDialog dataEndPicker;
    private DatePickerDialog dataLendPicker;
    private DatePickerDialog dataMonthPicker;

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
        //Save da inizializzare
        serviceSave = findViewById(R.id.serviceSave);
        //Edit Text da inizializzare
        serviceImageUrl = findViewById(R.id.serviceImageUrl);
        serviceName = findViewById(R.id.serviceName);
        serviceDescription = findViewById(R.id.serviceDescription);
        serviceLocation = findViewById(R.id.serviceLocation);
        serviceNOfMonthsLabel = findViewById(R.id.nOfMonths);
        serviceNOfMonths = findViewById(R.id.nofm);
        serviceNOfMonths.setVisibility(View.GONE);
        serviceNOfMonthsLabel.setVisibility(View.GONE);
        serviceListofDates = findViewById(R.id.listOfDates);
        serviceListofDates.setVisibility(View.GONE);
        //CheckBox da inizializzare
        checkMon = findViewById(R.id.checkMon);
        checkTue = findViewById(R.id.checkTue);
        checkWen = findViewById(R.id.checkWen);
        checkThu = findViewById(R.id.checkThu);
        checkFri = findViewById(R.id.checkFri);
        checkSat = findViewById(R.id.checkSat2);
        checkSun = findViewById(R.id.checkSun);
        checkMon.setVisibility(View.GONE);
        checkTue.setVisibility(View.GONE);
        checkWen.setVisibility(View.GONE);
        checkThu.setVisibility(View.GONE);
        checkFri.setVisibility(View.GONE);
        checkSat.setVisibility(View.GONE);
        checkSun.setVisibility(View.GONE);
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
        serviceMonthDate = findViewById(R.id.serviceMonthPicker);
        serviceMonthDate.setEnabled(false);
        serviceMonthLabel = findViewById(R.id.monthPickerLabel);
        serviceMonthLabel.setVisibility(View.GONE);
        serviceMonthDate.setVisibility(View.GONE);
        serviceEndDateLabel = findViewById(R.id.endDateLabel);
        serviceEndDateLabel.setVisibility(View.GONE);
        serviceStartDateLabel = findViewById(R.id.startdateLabel);
        //Inizializzo i date picker
        initStartDatePicker();
        initEndDatePicker();
        initLendDatePicker();
        initMonthDialogPicker();
        serviceStartDate.setText("Seleziona una data");
        serviceEndDate.setText("Seleziona una data");
        serviceLendTime.setText("Seleziona una data");
        serviceMonthDate.setText("Seleziona una data");
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
        //Listener che capisce se il checkbox è stato selezionato oppure no
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
                    checkMon.setVisibility(View.GONE);
                    checkTue.setVisibility(View.GONE);
                    checkWen.setVisibility(View.GONE);
                    checkThu.setVisibility(View.GONE);
                    checkFri.setVisibility(View.GONE);
                    checkSat.setVisibility(View.GONE);
                    checkSun.setVisibility(View.GONE);
                }
            }
        });
        //Listener sull'elemento selezionato nello spinner
        serviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(adapterType.getItem(position).toString()){
                    case "Giornaliera":
                        if(!serviceRecurrence.isChecked()){
                            serviceEndDate.setVisibility(View.GONE);
                            serviceEndDateLabel.setVisibility(View.GONE);
                        }else{
                            serviceEndDate.setVisibility(View.VISIBLE);
                            serviceEndDateLabel.setVisibility(View.VISIBLE);
                        }
                        checkMon.setVisibility(View.GONE);
                        checkTue.setVisibility(View.GONE);
                        checkWen.setVisibility(View.GONE);
                        checkThu.setVisibility(View.GONE);
                        checkFri.setVisibility(View.GONE);
                        checkSat.setVisibility(View.GONE);
                        checkSun.setVisibility(View.GONE);
                        serviceStartDate.setVisibility(View.VISIBLE);
                        serviceStartDateLabel.setVisibility(View.VISIBLE);
                        serviceMonthLabel.setVisibility(View.GONE);
                        serviceMonthDate.setVisibility(View.GONE);
                        serviceNOfMonths.setVisibility(View.GONE);
                        serviceNOfMonthsLabel.setVisibility(View.GONE);
                        serviceListofDates.setVisibility(View.GONE);
                        break;
                    case "Settimanale":
                        serviceEndDate.setVisibility(View.VISIBLE);
                        serviceEndDateLabel.setVisibility(View.VISIBLE);
                        checkMon.setVisibility(View.VISIBLE);
                        checkTue.setVisibility(View.VISIBLE);
                        checkWen.setVisibility(View.VISIBLE);
                        checkThu.setVisibility(View.VISIBLE);
                        checkFri.setVisibility(View.VISIBLE);
                        checkSat.setVisibility(View.VISIBLE);
                        checkSun.setVisibility(View.VISIBLE);
                        serviceStartDate.setVisibility(View.VISIBLE);
                        serviceStartDateLabel.setVisibility(View.VISIBLE);
                        serviceMonthLabel.setVisibility(View.GONE);
                        serviceMonthDate.setVisibility(View.GONE);
                        serviceNOfMonths.setVisibility(View.GONE);
                        serviceNOfMonthsLabel.setVisibility(View.GONE);
                        serviceListofDates.setVisibility(View.GONE);
                        break;
                    case "Mensile":
                        serviceEndDate.setVisibility(View.GONE);
                        serviceEndDateLabel.setVisibility(View.GONE);
                        checkMon.setVisibility(View.GONE);
                        checkTue.setVisibility(View.GONE);
                        checkWen.setVisibility(View.GONE);
                        checkThu.setVisibility(View.GONE);
                        checkFri.setVisibility(View.GONE);
                        checkSat.setVisibility(View.GONE);
                        checkSun.setVisibility(View.GONE);
                        serviceEndDate.setVisibility(View.GONE);
                        serviceEndDateLabel.setVisibility(View.GONE);
                        serviceStartDate.setVisibility(View.GONE);
                        serviceStartDateLabel.setVisibility(View.GONE);
                        serviceMonthLabel.setVisibility(View.VISIBLE);
                        serviceMonthDate.setVisibility(View.VISIBLE);
                        serviceNOfMonths.setVisibility(View.VISIBLE);
                        serviceNOfMonthsLabel.setVisibility(View.VISIBLE);
                        serviceListofDates.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Listener che mi permette di attivare il bottone del picker solo dopo aver inserito il numero di mesi
        serviceNOfMonths.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(serviceNOfMonths.length()==0){
                    serviceMonthDate.setEnabled(false);
                }else{
                    serviceMonthDate.setEnabled(true);
                    //Ricalcolo delle date di fine
                    monthEnd=getDateMonthEnd(monthRec, Integer.parseInt(serviceNOfMonths.getText().toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //Listener su checkbox dei vari giorni
        checkMon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkTue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkWen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkThu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkFri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        checkSun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if(serviceStartDate.getText().length()<=10){
                    LocalDate firstDate = getStartWeek(serviceStartDate.getText().toString());
                    startWeek = getWeekDates(firstDate);

                }
                if(serviceEndDate.getText().length()<=10){
                    LocalDate lastDate = getStartWeek(serviceEndDate.getText().toString());
                    endWeek = getWeekDates(lastDate);
                }
            }
        });
        serviceSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    newService(v);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initStartDatePicker() {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                }
                serviceStartDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);
                //metodo che calcola tutte le date utili della settimana in base alle scelte
                startWeek = getWeekDates(startDate);
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
                String date = year+"-"+month+"-"+dayOfMonth;;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                }
                serviceEndDate.setText(date);
                //calcolo data inizio e fine della settimana
                LocalDate startDate = getStartWeek(date);
                //metodo che calcola tutte le date utili della settimana in base alle scelte
                endWeek = getWeekDates(startDate);
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

    // Metodo che permette di inzializzare il date picker, impostandolo alla data odierna
    private void initMonthDialogPicker() {
        monthRec = new ArrayList<String>();
        monthEnd = new ArrayList<String>();
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = year+"-"+month+"-"+dayOfMonth;;
                if(month<10 && dayOfMonth<10){
                    date = year+"-0"+month+"-0"+dayOfMonth;
                }else if(dayOfMonth<10){
                    date = year+"-"+month+"-0"+dayOfMonth;
                }else if(month<10){
                    date = year+"-0"+month+"-"+dayOfMonth;
                }
                if(monthRec.contains(date)){
                    monthRec.remove(date);
                }else{
                    monthRec.add(date);
                }
                if(serviceNOfMonths.getText().toString().length()==0){
                    monthEnd=getDateMonthEnd(monthRec,1);
                }else {
                    monthEnd=getDateMonthEnd(monthRec, Integer.parseInt(serviceNOfMonths.getText().toString()));
                }
                serviceListofDates.setText("Ecco le date selezionate: "+ monthRec.toString().substring(1,monthRec.toString().length()-1)+"\nN.B per eliminare le date basta riselezionarle sul date picker.");
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //Blocco il calendario in modo tale che l'utente possa selezionare solamente i giorni del mese corrente
        dataMonthPicker = new DatePickerDialog(this,dateListener,year,month,day);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        dataMonthPicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        dataMonthPicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
    }

    //Metodo che calcola le date della fine del mese
    private List<String> getDateMonthEnd(List<String> startDates,int nOfMonths){
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < startDates.size(); i++) {
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate actDate = LocalDate.parse(startDates.get(i),formatter);
            dates.add(actDate.plusMonths(nOfMonths).toString());
        }
        return dates;
    }

    //Metodo che restituisce la data di inizio della settimana
    private LocalDate getStartWeek(String sDate){
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDate = LocalDate.parse(sDate,formatter);
        switch (firstDate.getDayOfWeek().toString()){
            case "TUESDAY":
                firstDate = firstDate.minusDays(1);
                break;
            case "WEDNESDAY":
                firstDate = firstDate.minusDays(2);
                break;
            case "THURSDAY":
                firstDate = firstDate.minusDays(3);
                break;
            case "FRIDAY":
                firstDate = firstDate.minusDays(4);
                break;
            case "SATURDAY":
                firstDate = firstDate.minusDays(5);
                break;
            case "SUNDAY":
                firstDate = firstDate.minusDays(6);
                break;
        }
        return firstDate;
    }

    //Metodo che restituisce le date della settimana partendo dalla data di inizio
    private List<String> getWeekDates(LocalDate start){
        List<String> dates = new ArrayList<>();
        if(checkMon.isChecked()){
            dates.add(start.toString());
        }
        if(checkTue.isChecked()){
            dates.add(start.plusDays(1).toString());
        }
        if(checkWen.isChecked()){
            dates.add(start.plusDays(2).toString());
        }
        if(checkThu.isChecked()){
            dates.add(start.plusDays(3).toString());
        }
        if(checkFri.isChecked()){
            dates.add(start.plusDays(4).toString());
        }
        if(checkSat.isChecked()){
            dates.add(start.plusDays(5).toString());
        }
        if(checkSun.isChecked()){
            dates.add(start.plusDays(6).toString());
        }
        System.out.println("date:"+dates.toString());
        return dates;
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
    public void openDateMonthPicker(View v){ dataMonthPicker.show();}

    //Metodo che mi permette di comunicare con il server per creare un nuovo servizio
    public void newService(View v) throws ParseException {
        Map<String,String> params = new HashMap<>();
        params.put("group_id",Utilities.getGroupId(this));
        params.put("owner_id",Utilities.getUserID(this));
        params.put("name",serviceName.getText().toString());
        params.put("description",serviceDescription.getText().toString());
        params.put("location",serviceLocation.getText().toString());
        //controlli in base al pattern
        if(servicePattern.getSelectedItem().toString().equals("Carsharing")){
            params.put("pattern","car");
            params.put("car_space",serviceCarSpace.getText().toString());
        }else if(servicePattern.getSelectedItem().toString().equals("Prestito oggetti")){
            params.put("pattern","lend");
            params.put("lend_obj",serviceLendObj.getText().toString());
            params.put("lend_time",serviceLendTime.getText().toString());
        }else{
            params.put("pattern","pickup");
            params.put("pickuplocation",servicePickup.getText().toString());
        }
        params.put("img",serviceImageUrl.getText().toString());
        if(serviceRecurrence.isChecked()){
            params.put("recurrence","true");
        }else{
            params.put("recurrence","false");
        }
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
        }else if(serviceRecurrence.isChecked() && serviceType.getSelectedItem().toString().equals("Settimanale")){
            params.put("type","weekly");
            if(startWeek.size()!=0){
                params.put("start_date",startWeek.toString());
            }
            if(endWeek.size()!=0){
                params.put("end_date",endWeek.toString());
            }
        }else if(serviceRecurrence.isChecked() && serviceType.getSelectedItem().toString().equals("Mensile")){
            params.put("type","monthly");
            params.put("start_date",monthRec.toString());
            params.put("end_date",monthEnd.toString());
        }
        System.out.println(params.toString());
        //Controlli per vedere se input è corretto
        if(TextUtils.isEmpty(serviceName.getText())){
            Toast.makeText(this, "Nome servizio mancante", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(serviceDescription.getText())){
            Toast.makeText(this, "Descrizione servizio mancante", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(serviceLocation.getText())){
            Toast.makeText(this, "Posizione servizio mancante", Toast.LENGTH_SHORT).show();
        }
        if(servicePattern.getSelectedItem().toString().equals("Carsharing") && TextUtils.isEmpty(serviceCarSpace.getText())){
            Toast.makeText(this, "Numero di posti mancanti", Toast.LENGTH_SHORT).show();
        }
        if(servicePattern.getSelectedItem().toString().equals("Servizio alle persone") && TextUtils.isEmpty(servicePickup.getText())){
            Toast.makeText(this, "Posto mancante mancanti", Toast.LENGTH_SHORT).show();
        }
        if(servicePattern.getSelectedItem().toString().equals("Prestito oggetti")) {
            if(TextUtils.isEmpty(serviceLendObj.getText()) && serviceLendTime.getText().toString().length()>10){
                Toast.makeText(this, "Oggetto da prestare e durata del prestito mancanti", Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(serviceLendObj.getText())){
                Toast.makeText(this, "Oggetto da prestare mancante", Toast.LENGTH_SHORT).show();
            }else if(serviceLendTime.getText().toString().length()>10){
                Toast.makeText(this, "Durata prestito mancante", Toast.LENGTH_SHORT).show();
            }
        }
        if(serviceRecurrence.isChecked()){
            switch (serviceType.getSelectedItem().toString()) {
                case "Mensile":
                    if(TextUtils.isEmpty(serviceNOfMonths.getText())){
                        Toast.makeText(this, "Numero di mesi mancanti", Toast.LENGTH_SHORT).show();
                    }else if(monthRec.size()==0){
                        Toast.makeText(this, "Data di inizio mancante", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "Giornaliero":
                    if(serviceStartDate.getText().length()>10 && serviceEndDate.getText().length()>10){
                        Toast.makeText(this, "Data di inizio e fine mancanti", Toast.LENGTH_SHORT).show();
                    }else if(serviceStartDate.getText().length()>10){
                        Toast.makeText(this, "Data di inizio mancante", Toast.LENGTH_SHORT).show();
                    }else if(serviceEndDate.getText().length()>10){
                        Toast.makeText(this, "Data di fine mancante", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "Settimanale":
                    if(startWeek.size()==0){
                        Toast.makeText(this, "Data di inizio mancanti", Toast.LENGTH_SHORT).show();
                    }else if(serviceStartDate.getText().length()>10 && serviceEndDate.getText().length()>10){
                        Toast.makeText(this, "Data di inizio e fine mancanti", Toast.LENGTH_SHORT).show();
                    }else if(serviceStartDate.getText().length()>10){
                        Toast.makeText(this, "Data di inizio mancante", Toast.LENGTH_SHORT).show();
                    }else if(serviceEndDate.getText().length()>10){
                        Toast.makeText(this, "Data di fine mancante", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        if(!serviceRecurrence.isChecked()){
            if(serviceStartDate.getText().length()>10){
                Toast.makeText(this, "Data di inizio mancante", Toast.LENGTH_SHORT).show();
            }
        }
        Utilities.httpRequest(this, Request.Method.POST,"/groups/"+Utilities.getGroupId(this)+"/service",response -> {
                finish();
            },error -> {
                Toast.makeText(this, "Servizio non creato correttamente", Toast.LENGTH_SHORT).show();
            },params);
    }
}