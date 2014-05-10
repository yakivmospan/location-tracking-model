package com.ym.model.location;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationListener;

import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

public class LocationService4 extends Service {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private long mLastSaveTime;

    private Location currentLocation = new Location(LocationManager.GPS_PROVIDER);
    private LocationBuffer mLocationBuffer;


    @Override
    public void onCreate() {
        super.onCreate();
        mLocationBuffer = new LocationBuffer(mFulledListener);
        initLocationClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        destroyLocationClient();
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        if(level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE){
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
        mLastSaveTime = System.currentTimeMillis();
    }

    private void initLocationClient() {
        //...
    }

    private void destroyLocationClient() {
        // ...
    }

    private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener;

    private LocationListener mLocationListener;



    private void locationChanged(Location location) {
        // ...

        if (isBetterLocation(location, currentLocation)) {
            currentLocation = location;
        }

        if(System.currentTimeMillis() - mLastSaveTime > TWO_MINUTES){
            saveGlobalData();
        }
    }

    private LocationBuffer.FulledListener mFulledListener = new LocationBuffer.FulledListener() {
        @Override
        public void onFulled() {

        }
    };

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new
     *                            one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
