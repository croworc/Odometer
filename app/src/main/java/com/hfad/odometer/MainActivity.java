package com.hfad.odometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    /** Holds a reference to the OdometerService */
    private OdometerService mOdometer;
    /** Flag whether or not this activity is bound to the service */
    private boolean mIsBound = false;

    /** We need to define a service connection */
    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * Get a reference to the OdometerService when the service is connected
         * @param serviceName the fully qualified name of the service
         * @param binder the IBinder implementation object provided by our service
         */
        @Override
        public void onServiceConnected(ComponentName serviceName, IBinder binder) {
            // First, cast the incoming IBinder to the OdometerService.OdometerBinder we know it is.
            OdometerService.OdometerBinder odometerBinder =
                    (OdometerService.OdometerBinder) binder;
            // Then, we use this IBinder to fetch and hold a reference to our service and set the
            // "is bound?" flag to true, as the activity is now bound to the service.
            mOdometer = odometerBinder.getOdometer();
            mIsBound  = true;
        } // close method onServiceConnected()

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Set the "is bound?" flag to false, as the activity is no longer bound to the service.
            mIsBound = false;
        } // close method onServiceDisconnected()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Call the displayDistance() method when the activity is created.
        displayDistance();
    } // close method onCreate()

    @Override
    protected void onStart() {
        super.onStart();
        // Bind the service when the activity starts.
        Intent intent = new Intent(this, OdometerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    } // close method onStart()

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind the service when the activity stops.
        if (mIsBound) {
            unbindService(mConnection);
        }
    } // close method onStop()

    /**
     * Displays the distance returned by the service's getDistance() method.
     *
     * Uses a Handler to run the code in a separate thread in the background.
     * Should be started by onCreate(), then runs in "standby" until we've got a
     * reference to the OdometerService and this activity is bound to the service.
     * From that moment on, it requests the distance from the service, updates the text view
     * and does so about every second.
     */
    private void displayDistance() {
        final TextView distanceView = (TextView) findViewById(R.id.distance);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance = 0.0;
                if (mOdometer != null && mIsBound) { // are we bound to the service, yet?
                    distance = mOdometer.getDistance();
                }
                String distanceString = String.format(Locale.getDefault(),
                        "%1$,.2f miles", distance);
                distanceView.setText(distanceString);
                handler.postDelayed(this, 1000); // repeat this code after ~1 second
            } // close method run()
        });
    } // close method displayDistance()

} // close class MainActivity
