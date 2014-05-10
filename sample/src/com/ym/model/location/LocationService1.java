package com.ym.model.location;

import com.ym.utils.L;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import java.util.List;


public class LocationService1 extends Service {

    public static final int MINIMUM_LOCATIONS_DISTANCE = 0;
    private long updateInterval = 15000;

    private LocationManager mLocationManager;
    private LocationBinder mLocationBinder = new LocationBinder();

    private Location currentLocation = new Location(LocationManager.GPS_PROVIDER);

    @Override
    public void onCreate() {
        super.onCreate();
        initGpsStateObserver();
        checkGpsState();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocationBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        final String intentAction = intent.getAction();
        if ("ACTION_START_RECORD".equals(intentAction)) {
            // start recording data, return REDELIVER_INTENT because
            // we want to continue recording if service was killed and recreated by system
            return START_REDELIVER_INTENT;
        } else if ("ACTION_STOP_RECORD".equals(intentAction)) {
            // stop recording data, return START_STICKY because
            // we don't need to go here if service was killed and recreated by system
            return START_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        removeGpsStateObserver();
        destroyLocationManager();
        super.onDestroy();
    }

    private void initGpsStateObserver() {
        final ContentResolver resolver = getContentResolver();

        Uri uri = Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        resolver.registerContentObserver(uri, false, gpsObserver);
    }

    private void removeGpsStateObserver() {
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(gpsObserver);
    }

    final private ContentObserver gpsObserver = new ContentObserver(new Handler()) {

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // do some changes when gps state changes
            checkGpsState();
        }
    };

    private void checkGpsState() {
        if (isGpsOn()) {
            initLocationManager();
        } else {
            destroyLocationManager();
        }
    }

    private void initLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    updateInterval,
                    MINIMUM_LOCATIONS_DISTANCE,
                    mLocationListener);

            currentLocation = getLastKnownLocation();
        }
    }

    private void destroyLocationManager() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager = null;
        }
    }

    private android.location.LocationListener mLocationListener
            = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            locationChanged(location);
        }

        @Override
        public void onProviderDisabled(final String provider) {
            // gps turned off
            // if provider == LocationManager.GPS_PROVIDER
        }

        @Override
        public void onProviderEnabled(final String provider) {
            // gps turned on
            // if provider == LocationManager.GPS_PROVIDER
        }

        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            // called when gps changed its status. There are 3 of them :
            // OUT_OF_SERVICE, TEMPORARILY_UNAVAILABLE and AVAILABLE
        }
    };

    private void locationChanged(Location location) {
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
            // skip zero locations
            return;
        }

        //save your current location
        currentLocation = location;

        if (!isWifiLocation()) {
            // skip wifi locations if needed
            // and calculate changes
        }

        mLocationBinder.onLocationChanged(location);

        L.i("location provider = [" + currentLocation.getProvider() + "]");
    }

    private boolean isWifiLocation() {
        return LocationManager.GPS_PROVIDER.equals(currentLocation.getProvider());
    }

    private boolean isGpsOn() {
        String provider = Settings.Secure
                .getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return provider != null && provider.contains(LocationManager.GPS_PROVIDER);
    }

    private Location getLastKnownLocation() {
        Location result = null;

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        final String bestProvider = mLocationManager.getBestProvider(criteria, true);

        if (bestProvider != null) {
            result = mLocationManager.getLastKnownLocation(bestProvider);
        }

        if (result == null) {
            final List<String> providers = mLocationManager.getAllProviders();

            for (final String providerStr : providers) {
                result = mLocationManager.getLastKnownLocation(providerStr);
                if (result != null) {
                    break;
                }
            }
        }

        if (result == null) {
            result = new Location(LocationManager.GPS_PROVIDER);
            result.setLatitude(0);
            result.setLongitude(0);
        }

        return result;
    }
}