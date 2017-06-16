package com.hfad.odometer;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    /** Tag for logging purposes */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST_CODE = 698;
    private final int NOTIFICATION_ID = 423;

    /** Will hold a reference to the OdometerService */
    private OdometerService mOdometer;

    /** Flag whether or not this activity is currently bound to the service */
    private boolean mIsBound = false;

    /** We need to define a service connection in order to bind MainActivity to OdometerService */
    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * Gets a reference to the OdometerService when the service is connected
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
            Log.d("onServiceConnected()", "Connected! Odometer != null? "
                    + (mOdometer != null) + ", mIsBound = " + mIsBound );
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

        // If permission for ACCESS_FINE_LOCATION hasn't been already granted...
        if (ContextCompat.checkSelfPermission(this, OdometerService.PERMISSION_STRING)
                != PackageManager.PERMISSION_GRANTED) {
            //...request it at runtime
            ActivityCompat.requestPermissions(this,
                    new String[] {OdometerService.PERMISSION_STRING}, PERMISSION_REQUEST_CODE);
        } else {
            // If permission has already been granted, bind to the service.
            Intent intent = new Intent(this, OdometerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } // close if: location permission granted/not yet granted
    } // close method onStart()

    /**
     * Returns the results of our permissions requests, if we've asked the user for permissions at
     * runtime.
     *
     * @param requestCode the code that was used in our requestPermissions() method call
     * @param permissions a string array of permissions
     * @param grantResults an int array for the results of the requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // Check whether the code matches the one in our requestPermissions() method call.
            case PERMISSION_REQUEST_CODE:
                // If permission was granted, bind to the service.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, OdometerService.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                } else {
                    // If the request was cancelled, no results will be returned.
                    // Code to run if permission was denied: notify the user that this permission
                    // is necessary for the app to work properly.

                    // Create a notification builder
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.permission_denied))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[] {1000, 1000})
                            .setAutoCancel(true);

                    // Create an action
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    PendingIntent actionPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(actionPendingIntent);

                    // Issue the notification
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                } // close if/else: permission granted/denied
        } // close switch
    } // close method onRequestPermissionsResult

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind the service when the activity stops.
        if (mIsBound) {
            unbindService(mConnection);
        }
    } // close method onStop()

    /**
     * Displays the distance traveled, returned by the service's getDistance() method.
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
                        "%1$,.3f km", distance);
                distanceView.setText(distanceString);
                handler.postDelayed(this, 1000); // repeat this code after ~1 second
            } // close method run()
        });
    } // close method displayDistance()

} // close class MainActivity
