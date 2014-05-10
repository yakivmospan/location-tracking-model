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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class LocationService3 extends Service {

    private final static int RECONNECT_CLIENT = 101;

    private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private long updateInterval = 15000;
    private long fastestInterval = 5000;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;


    @Override
    public void onCreate() {
        super.onCreate();
        initLocationClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        final String intentAction = intent.getAction();

        if ("ACTION_GEOFENCE".equals(intentAction)) {
            handleGeofence(intent);
            return START_REDELIVER_INTENT;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        destroyLocationClient();
        super.onDestroy();
    }

    private void initLocationClient() {
        //...
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

        Intent intent = new Intent(getBaseContext(), LocationService3.class);
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
        Intent intent = new Intent(getBaseContext(), LocationService3.class);
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

    private LocationListener mLocationListener;
}
