package com.example.gps_test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import android.util.Log;
import android.view.View;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.location.LocationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private boolean fineLocationGranted = false;
    private boolean backgroundLocationGranted = false;
    private boolean locationEnabled = false;
    private boolean logging = false;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkLocation(this);
        checkLocationPermissions();
    }

    private void checkLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_on =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_on =locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        locationEnabled = gps_on || network_on;

        if (!locationEnabled) {
            checkLocationServiceStatus();
        }
    }
    private void checkLocationServiceStatus() {
        LocationRequest locationRequest = new LocationRequest.Builder(10000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true); // Shows dialog even if settings are satisfied partially

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // ✅ Location settings are ON — proceed with app

        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, 1001);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }
    public void buttonClick(View view) {
        logging = !logging;

        if (logging) {
            setButtonText("Stop");
            checkLocation(this);
            startGpsLogging();
        }
        else {
            stopGpsLogging();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void startGpsLogging() {
        count = 0;

        this.startForegroundService(new Intent(this, LocationService.class));
        IntentFilter filter = new IntentFilter("com.example.gps_test.LOCATION_UPDATE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            registerReceiver(locationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(locationReceiver, filter);
        }
    }

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            TextView txtView = findViewById(R.id.status);
            count++;
            String txt = "Count: " + count  + "\n" + lat + "\n" + lng;
            txtView.setText(txt);
        }
    };
    private void stopGpsLogging() {
        setButtonText("Start");
        this.stopService(new Intent(this, LocationService.class));
        unregisterReceiver(locationReceiver);
    }

    private void setButtonText(String txt) {
        Button btn = findViewById(R.id.button);
        btn.setText(txt);
    }
    private void checkLocationPermissions() {

        fineLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        backgroundLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineLocationGranted) {
            requestLocationPermissions();
        }
        else if (!backgroundLocationGranted){
            requestBackgroundLocation();
        }
    }
    private void requestLocationPermissions() {
        // Android 10+ requires separate request for background location
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void requestBackgroundLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle("Background Location Required")
                    .setMessage("This app needs background location to work properly.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    fineLocationGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    backgroundLocationGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (fineLocationGranted  && !backgroundLocationGranted) {
                // Foreground fine location granted
                // Request background location if needed
                requestBackgroundLocation();
            }
        }
    }
}