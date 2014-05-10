package com.ym.model.location;

import junit.framework.Assert;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationBuffer {

    // max locations count that can be in buffer
    public static final int SIZE = 50;

    // listener that triggers when buffer gets full
    private FulledListener mFullListener;

    public LocationBuffer(FulledListener fullListener) {
        Assert.assertNotNull("FullListener cannot be null", fullListener);
        mFullListener = fullListener;
    }

    private List<Location> buffer = new ArrayList<Location>();

    public boolean isFull() {
        return buffer.size() >= SIZE;
    }

    // interface for adding locations to buffer
    public void add(Location location) {
        buffer.add(location);
        if (isFull()) {
            mFullListener.onFulled();
            buffer.clear();
        }
    }

    public interface FulledListener {

        public void onFulled();
    }
}
