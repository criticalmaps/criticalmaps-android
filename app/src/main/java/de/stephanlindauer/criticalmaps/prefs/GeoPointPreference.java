package de.stephanlindauer.criticalmaps.prefs;

import android.content.SharedPreferences;

import org.osmdroid.util.GeoPoint;

import info.metadude.android.typedpreferences.DoublePreference;

public class GeoPointPreference {

    protected final SharedPreferences mSharedPreferences;
    protected final DoublePreference mLatitudePreference;
    protected final DoublePreference mLongitudePreference;

    public GeoPointPreference(final SharedPreferences sharedPreferences, final String key) {
        mSharedPreferences = sharedPreferences;
        String keyLatitude = key + ".LATITUDE";
        String keyLongitude = key + ".LONGITUDE";
        mLatitudePreference = new DoublePreference(mSharedPreferences, keyLatitude);
        mLongitudePreference = new DoublePreference(mSharedPreferences, keyLongitude);
    }

    public GeoPoint get() {
        double latitude = mLatitudePreference.get();
        double longitude = mLongitudePreference.get();
        return new GeoPoint(latitude, longitude);
    }

    public boolean isSet() {
        return mLatitudePreference.isSet() && mLongitudePreference.isSet();
    }

    public void set(final GeoPoint point) {
        mLatitudePreference.set(point.getLatitude());
        mLongitudePreference.set(point.getLongitude());
    }

    public void delete() {
        mLatitudePreference.delete();
        mLongitudePreference.delete();
    }

}
