package com.mespl.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public abstract class LocationBaseActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isGPS = false;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    currentLocation(currentLocation);
                    Log.d("location", "location" + currentLocation);
                } else
                    Toast.makeText(getApplicationContext(), "Location Not Fetched", Toast.LENGTH_LONG).show();
            }
        };
    }

    protected abstract void currentLocation(Location location);

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionUtils.neverAskAgainSelected(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    displayNeverAskAgainDialog();
                } else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            Constants.LOCATION_REQUEST);
            }
        } else
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                else
                    PermissionUtils.setShouldShowStatus(this, Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
                getLocation();
            }
        }
    }

    private void displayNeverAskAgainDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("We need to get current location performing necessary task. Please permit the permission through "
                    + "Settings screen.\n\nSelect Permissions -> Enable permission");
            builder.setCancelable(false);
            builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
            dialog = builder.create();
        }
        dialog.show();
    }

    public final float getDistance(Double otherLocationLat, Double otherLocationLng) {
        float distanceInMeters = 0;
        if(currentLocation!=null){
            Location location = new Location("");
            location.setLatitude(otherLocationLat);
            location.setLongitude(otherLocationLng);
            distanceInMeters = location.distanceTo(currentLocation);
        }
        return distanceInMeters;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GpsUtils(this).turnGPSOn((boolean isGPSEnable) -> {
            isGPS = isGPSEnable;
            if (isGPS)
                getLocation();
            else
                Toast.makeText(getApplicationContext(), "Please Enable GPS", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}