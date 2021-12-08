package com.example.nekoma_families_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class YourEvent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_event);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        TabItem tabYourEvent = (TabItem) findViewById(R.id.your_events);
        TabItem tabActiveEvent = (TabItem) findViewById(R.id.active_events);
        TabItem tabClosedEvent = (TabItem) findViewById(R.id.closed_events);

    }
    public void getHomePage(View v){
        Intent homepage = new Intent(YourEvent.this,Homepage.class);
        startActivity(homepage);
    }
}