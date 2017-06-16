package com.hfad.odometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class OdometerService extends Service {

    // The distance in meters this device has been traveling since this service has been created.
    private static double distanceInMeters;
    private static Location lastLocation = null;

    // Permission to access the device's fine location
    public static final String PERMISSION_STRING = android.Manifest.permission.ACCESS_FINE_LOCATION;

    // Create a new IBinder instance that we'll later use to get a reference to our service.
    private final IBinder binder = new OdometerBinder();

    // Some class refs for the location purposes
    private LocationListener listener;
    private LocationManager locManager;

    /**
     * This is our IBinder implementation. (Class Binder implements the IBinder interface).
     *
     * An object of this class gets returned from the service when the activity tries to bind
     * to it (when the service's method onBind() is called), and provides the activity with
     * a reference to the service.
     */
    public class OdometerBinder extends Binder
    {
        OdometerService getOdometer() {
            return OdometerService.this;
        }
    } // close inner class OdometerBinder

    /**
     * Constructor
     */
    public OdometerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup the LocationListener
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("LocationListener", "in method onLocationChanged()");
                if (lastLocation == null) {
                    // Set the user's starting location
                    lastLocation = location;
                }
                // Update the distance traveled and the user's last location.
                distanceInMeters += location.distanceTo(lastLocation);
                lastLocation = location;
            } // close method onLocationChanged()

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        // Get the location manager
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Check whether we have permission to access fine location
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED) {
            // Get the most accurate location provider
            String provider = locManager.getBestProvider(new Criteria(), true);
            if (provider != null) {
                // Request updates from the location provider, no more frequent than every
                // 1000 milliseconds, but only if the location has changed more than 1 meter.
                locManager.requestLocationUpdates(
                        provider, 1000, 1, listener);
            }
        } // close: if we do have permission to access fine location
    } // close method onCreate()

    @Override
    public IBinder onBind(Intent intent) {
        // Return the IBinder
        return binder;
    } // close method onBind()

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop getting location updates (if we have permission to remove them).
        if (locManager != null && listener != null) {
            // Check if we've permission to access fine location
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {
                // Stop the location listener getting updates.
                locManager.removeUpdates(listener);
            }
            // Set the location manager and location listener variables to null.
            locManager = null;
            listener = null;
        }
    } // close method onDestroy()

    public double getDistance() {
        return this.distanceInMeters / 1000;
    } // close method getDistance

} // close enclosing class OdometerService
