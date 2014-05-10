package com.ym.controller;

import com.defaultproject.R;
import com.ym.model.location.LocationService2;
import com.ym.model.location.LocationService6;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class MainActivity3
        extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);

        Intent intent = new Intent(getBaseContext(), LocationService6.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(getBaseContext(), LocationService2.class);
        stopService(intent);
    }
}
