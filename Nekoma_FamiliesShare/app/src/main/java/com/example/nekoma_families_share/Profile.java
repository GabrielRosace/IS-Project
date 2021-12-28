package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
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

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private String user_id = "";
    private EditText emailLabel;
    private EditText nameLabel;
    private EditText surnameLabel;
    private EditText phoneLabel;
    private EditText addressLabel;
    private String address_id;
    private EditText numberLabel;
    private EditText cityLabel;
    private Switch editSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar_profilo);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Token dell'utente, mi permette di fare le chiamate al server
        String userToken = Utilities.getToken(this);
        // Parse del token, che permette di conoscere l'id dell'utente
        String[] split_token = userToken.split("\\.");
        String base64Body = split_token[1];
        String body = new String(Base64.getDecoder().decode(base64Body));
        try {
            JSONObject res = new JSONObject(body);
            user_id = res.getString("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Chiamata al server, che restituisce tutti i dati personali di un utente, che verranno usati per riempire i vari campi del profilo
        Utilities.httpRequest(this,Request.Method.GET,"/users/"+user_id+"/profile",response -> {
            try {
                JSONObject user_response = new JSONObject((String) response);
                JSONObject user_address = user_response.getJSONObject("address");
                JSONObject user_image = user_response.getJSONObject("image");
                // Informazioni personali dell'utente
                emailLabel = findViewById(R.id.profileEmail);
                emailLabel.setEnabled(false);
                emailLabel.setText(user_response.getString("email"));
                nameLabel = findViewById(R.id.profileName);
                nameLabel.setEnabled(false);
                nameLabel.setText(user_response.getString("given_name"));
                surnameLabel = findViewById(R.id.profileSurname);
                surnameLabel.setEnabled(false);
                surnameLabel.setText(user_response.getString("family_name"));
                phoneLabel = findViewById(R.id.profilePhone);
                phoneLabel.setEnabled(false);
                if (((user_response.getString("phone")).length()!= 0)) {
                    phoneLabel.setText(user_response.getString("phone"));
                } else {
                    phoneLabel.setHint("Non specificato");
                }
                // Informazioni su indirizzo dell'utente
                address_id = user_address.getString("address_id");
                addressLabel = findViewById(R.id.profileStreet);
                addressLabel.setEnabled(false);
                if (((user_address.getString("street")).length()!= 0)) {
                    addressLabel.setText(user_address.getString("street"));
                } else {
                    addressLabel.setHint("Non specificato");
                }
                numberLabel = findViewById(R.id.profileNumber);
                numberLabel.setEnabled(false);
                if (((user_address.getString("number")).length()!= 0)) {
                    numberLabel.setText(user_address.getString("number"));
                } else {
                    numberLabel.setHint(" ");
                }
                cityLabel = findViewById(R.id.profileCity);
                cityLabel.setEnabled(false);
                if (((user_address.getString("city")).length()!= 0)) {
                    cityLabel.setText(user_address.getString("city"));
                } else {
                    cityLabel.setHint("Non specificato");
                }
                // Immagine profilo dell'utente
                ImageDownloader image = new ImageDownloader(findViewById(R.id.profileImage));
                image.execute(getString(R.string.urlnoapi)+user_image.getString("path"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(Profile.this, "Impossibile caricare informazioni del profilo, riprova più tardi.", Toast.LENGTH_LONG).show();
        }, new HashMap<>());

        // Metodo che permette di cambiare stato ai vari campi della pagina in base al valore dello switch
        editSwitch = (Switch)findViewById(R.id.EditSwitch);
        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button saveChange = findViewById(R.id.profileSaveChanges);
                nameLabel.setEnabled(isChecked);
                surnameLabel.setEnabled(isChecked);
                phoneLabel.setEnabled(isChecked);
                addressLabel.setEnabled(isChecked);
                saveChange.setEnabled(isChecked);
                numberLabel.setEnabled(isChecked);
                cityLabel.setEnabled(isChecked);
                if ((isChecked)) {
                    editSwitch.setText(R.string.disableEdit);
                } else {
                    editSwitch.setText(R.string.enableEdit);
                }
            }
        });

    }

    // Metodo che permette di comunicare con il server, usato per eliminare un utente
    public void DeleteUser(View v){
        Utilities.httpRequest(this,Request.Method.DELETE,"/users/"+user_id,response -> {
            Intent login = new Intent(Profile.this,MainActivity.class);
            startActivity(login);
        },error -> {
            Toast.makeText(Profile.this, "Non è possibile eliminare il profilo, riprova più tardi.", Toast.LENGTH_LONG).show();
        },new HashMap<>());
    }

    // Metodo che permette di comunicare con il server, usato per modificare i dati personali di un utente
    public void EditUser(View v){
        Map<String,String> params = new HashMap<>();
        params.put("given_name",nameLabel.getText().toString());
        params.put("family_name",surnameLabel.getText().toString());
        params.put("email",emailLabel.getText().toString());
        params.put("phone",phoneLabel.getText().toString());
        params.put("phone_type","unspecified");
        params.put("visible","true");
        params.put("address_id",address_id);
        params.put("street",addressLabel.getText().toString());
        params.put("number",numberLabel.getText().toString());
        params.put("city",cityLabel.getText().toString());
        params.put("description","");
        params.put("contact_option","email");

        Utilities.httpRequest(this,Request.Method.PATCH,"/users/"+user_id+"/profile",response -> {
            Toast.makeText(Profile.this, "Profilo modificato correttamente!", Toast.LENGTH_LONG).show();
            Intent homepage = new Intent(Profile.this,Homepage.class);
            startActivity(homepage);
        },error -> {
            Toast.makeText(Profile.this, error.toString(), Toast.LENGTH_LONG).show();
        },params);
    }

    // Metodo che mi permette di scaricare l'immagine dell'utente
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
}