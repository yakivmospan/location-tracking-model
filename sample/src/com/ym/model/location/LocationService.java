package com.ym.model.location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import com.ym.utils.L;

import android.app.PendingIntent;
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

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    private final static int RECONNECT_CLIENT = 101;
    public static final int MINIMUM_TRACING_ACCURACY = 15;

    private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private long updateInterval = 15000;
    private long fastestInterval = 5000;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

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
        destroyLocationClient();
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
            initLocationClient();
        } else {
            destroyLocationClient();
        }
    }

    private void initLocationClient() {
        if (mLocationClient == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(priority);
            mLocationRequest.setInterval(updateInterval);
            mLocationRequest.setFastestInterval(fastestInterval);

            mLocationClient = new LocationClient(
                    getBaseContext(),
                    mConnectionCallBacks,
                    mConnectionFailedListener);
            mLocationClient.connect();
        }
    }

    private void destroyLocationClient() {
        if (mLocationClient != null) {
            if (mLocationClient.isConnected()) {
                removeGeofences();
                mLocationClient.removeLocationUpdates(mLocationListener);
            }
            mLocationClient.disconnect();
            mLocationClient = null;
        }
    }

    private GooglePlayServicesClient.ConnectionCallbacks mConnectionCallBacks
            = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            final Location lastLocation = mLocationClient.getLastLocation();

            currentLocation = lastLocation == null ? getLastKnownLocation() : lastLocation;
            mLocationClient.requestLocationUpdates(mLocationRequest, mLocationListener);

            addGeofences();
        }

        @Override
        public void onDisconnected() {
            // try to reconnect client
            reconnectClientHandler.obtainMessage(RECONNECT_CLIENT);
        }
    };

    private Handler reconnectClientHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == RECONNECT_CLIENT) {
                destroyLocationClient();
                initLocationClient();
            }
        }
    };

    private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener
            = new GooglePlayServicesClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // connection failed
            // maybe you want to notify user about this ..
        }
    };


    private void addGeofences() {
        List<Geofence> geofences = getGeofences();

        Intent intent = new Intent(getBaseContext(), LocationService.class);
        intent.setAction("ACTION_GEOFENCE");
        PendingIntent pendingIntent = PendingIntent
                .getService(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mLocationClient.addGeofences(geofences, pendingIntent, mOnAddGeonfences);
    }

    private void removeGeofences() {
        // remove by ids
        List<String> ids = new ArrayList<String>();
        ids.add("geo_id");
        mLocationClient.removeGeofences(ids, mOnRemoveGeofences);

        // remove by intent
        Intent intent = new Intent(getBaseContext(), LocationService.class);
        intent.setAction("ACTION_GEOFENCE");
        PendingIntent pendingIntent = PendingIntent
                .getService(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationClient.removeGeofences(pendingIntent, mOnRemoveGeofences);
    }

    private LocationClient.OnAddGeofencesResultListener mOnAddGeonfences
            = new LocationClient.OnAddGeofencesResultListener() {
        @Override
        public void onAddGeofencesResult(int i, String[] strings) {

        }
    };

    private LocationClient.OnRemoveGeofencesResultListener mOnRemoveGeofences
            = new LocationClient.OnRemoveGeofencesResultListener() {
        @Override
        public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

        }

        @Override
        public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {

        }
    };

    private List<Geofence> getGeofences() {
        List<Geofence> result = new ArrayList<Geofence>();

        Geofence geo = new Geofence.Builder()
                .setRequestId("geo_id")
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(37.422006d, -122.084095d, 100f) // lat, lon and radius in meters
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // time in millis
                .build();

        result.add(geo);
        return result;
    }

    private void handleGeofence(Intent intent) {
        if (!LocationClient.hasError(intent)) {
            int transitionType = LocationClient.getGeofenceTransition(intent);

            //We don't need location updates when user is out of resort. So we can stop them.
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                // entered area
                L.i("GEOFENCE_TRANSITION_ENTER");
            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                // lived area
                L.i("GEOFENCE_TRANSITION_EXIT");
            }
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationChanged(location);
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
    }

    private boolean isWifiLocation() {
        return currentLocation.getSpeed() == 0
                && currentLocation.getAccuracy() >= MINIMUM_TRACING_ACCURACY;
    }

    private boolean isGpsOn() {
        String provider = Settings.Secure
                .getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return provider != null && provider.contains(LocationManager.GPS_PROVIDER);
    }

    private Location getLastKnownLocation() {
        Location result = null;

        LocationManager manager = (LocationManager) getBaseContext().getSystemService(
                Context.LOCATION_SERVICE);

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        final String bestProvider = manager.getBestProvider(criteria, true);

        if (bestProvider != null) {
            result = manager.getLastKnownLocation(bestProvider);
        }

        if (result == null) {
            final List<String> providers = manager.getAllProviders();

            for (final String providerStr : providers) {
                result = manager.getLastKnownLocation(providerStr);
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
