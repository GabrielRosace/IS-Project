package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

public class Guide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        /*Toolbar a = (Toolbar) findViewById(R.id.toolbar);
        a.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(Guide.this,MainActivity.class);
                startActivity(login);
            }
        });*/
    }
    public void getLogin(View v){
        Intent login = new Intent(Guide.this,MainActivity.class);
        startActivity(login);
    }

}