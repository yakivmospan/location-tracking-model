package com.ym.model.location;

import android.location.Location;
import android.os.Binder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LocationBinder extends Binder {

    private Object data;

    public LocationBinder() {
    }

    private Map<String, LocationObserver> mLocationObservers
            = new LinkedHashMap<String, LocationObserver>();

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public void addLocationObserver(String theKey, LocationObserver theLocationObserver) {
        mLocationObservers.put(theKey, theLocationObserver);
    }

    public void removeLocationObserver(String theKey) {
        mLocationObservers.remove(theKey);
    }

    void onLocationChanged(Location location) {
        Set<Map.Entry<String, LocationObserver>> entries = mLocationObservers.entrySet();

        for (Map.Entry<String, LocationObserver> entry : entries) {
            LocationObserver locationObserver = entry.getValue();
            if (locationObserver != null) {
                locationObserver.onLocationChanged(location);
            }
        }
    }
}