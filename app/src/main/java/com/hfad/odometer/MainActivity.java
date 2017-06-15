package com.hfad.odometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class MainActivity extends Activity {

    private OdometerService mOdometer;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // First, cast the incoming IBinder to the OdometerService.OdometerBinder we know it is.
            OdometerService.OdometerBinder odometerBinder =
                    (OdometerService.OdometerBinder) binder;
            // Then, we use this IBinder to fetch and hold a reference to our service and set the
            // "is bound?" flag to true, as the activity is now bound to the service.
            mOdometer = odometerBinder.getOdometer();
            mIsBound = true;
        } // close method onServiceConnected()

        @Override
        public void onServiceDisconnected(ComponentName name) {

        } // close method onServiceDisconnected()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
