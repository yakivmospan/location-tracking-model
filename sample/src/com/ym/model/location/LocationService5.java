package com.ym.model.location;

import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

public class LocationService5 extends Service {

    private LocationBuffer mLocationBuffer;


    @Override
    public void onCreate() {
        super.onCreate();
        mLocationBuffer = new LocationBuffer(mFulledListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private LocationBuffer.FulledListener mFulledListener = new LocationBuffer.FulledListener() {
        @Override
        public void onFulled() {
            saveGlobalData();
        }
    };

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            saveGlobalData();
        }
    }

    @Override
    public void onLowMemory() {
        // call it here to support old operating systems
        saveGlobalData();
    }

    private void saveGlobalData() {
        // save your stats to database or preferences
    }


    private void locationChanged(Location location) {
        // ...
        mLocationBuffer.add(location);
    }

}