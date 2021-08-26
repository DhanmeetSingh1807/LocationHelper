package com.mespl.locationhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mespl.helper.LocationBaseActivity;
import com.mespl.locationhelper.databinding.ActivityMainBinding;

public class MainActivity extends LocationBaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
    }

    @Override
    protected void currentLocation(Location location) {
        binding.tvCurrentLocation.setText(location.toString());
    }

    public void onLocationPress(View v) {
        float distance = getDistance(28.612073, 77.093532);
        Snackbar snackbar = Snackbar.make(v, String.valueOf(distance), Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}