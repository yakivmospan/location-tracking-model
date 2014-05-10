package com.ym.controller;

import com.defaultproject.R;
import com.ym.model.location.LocationBinder;
import com.ym.model.location.LocationObserver;
import com.ym.model.location.LocationService1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;


public class MainActivity1
        extends FragmentActivity {

    private TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);

        txtLocation = (TextView) findViewById(R.id.txtLocation);

        Intent intent = new Intent(getBaseContext(), LocationService1.class);

//        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationBinder locationBinder = (LocationBinder) iBinder;
            locationBinder.addLocationObserver("observer_key", mLocationObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private LocationObserver mLocationObserver = new LocationObserver() {
        @Override
        public void onLocationChanged(Location location) {
            txtLocation.setText(
                    "lat, lon = [" + location.getLatitude() + ", " + location.getLongitude() + "]");
        }
    };
}
