package com.ym.model.location;

import android.location.Location;

public interface LocationObserver {

    public void onLocationChanged(Location location);

    // add methods that you need below

    // public void onLossConnection();
    // etc. etc.
}