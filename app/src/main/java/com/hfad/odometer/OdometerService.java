package com.hfad.odometer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

public class OdometerService extends Service {

    private final IBinder mBinder = new OdometerBinder();
    // We'll use a Random to generate random numbers
    private final Random mRandom = new Random();

    public OdometerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the IBinder
        return mBinder;
    } // close method onBind()

    public double getDistance() {
        return mRandom.nextDouble();
    } // close method getDistance


    /**
     * This is our IBinder implementation. (Class Binder implements the IBinder interface).
     * The IBinder works as kind of a proxy for the activity to access the service.
     */
    public class OdometerBinder extends Binder
    {
        OdometerService getOdometer() {
            return OdometerService.this;
        }
    } // close inner class OdometerBinder

} // close enclosing class OdometerService
