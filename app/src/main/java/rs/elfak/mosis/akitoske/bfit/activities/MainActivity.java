package rs.elfak.mosis.akitoske.bfit.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import rs.elfak.mosis.akitoske.bfit.Constants;
import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.fragments.MapFragment;
import rs.elfak.mosis.akitoske.bfit.fragments.NoLocationFragment;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class MainActivity extends AppCompatActivity implements
        MapFragment.OnFragmentInteractionListener,
        NoLocationFragment.OnFragmentInteractionListener{

    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 2;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFragmentManager = getSupportFragmentManager();

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                onLocationSettingsSatisfied();
            } else {
                onLocationSettingsUnsatisfied();
            }
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(Constants.LOCATION_REQUEST_PRIORITY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        onLocationSettingsSatisfied();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case CommonStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog to change them.
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sendEx) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                onLocationSettingsUnsatisfied();
                                break;
                        }
                    }
                });
    }
    private void checkLocationPermission() {
        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int userPermission = ContextCompat.checkSelfPermission(this, locationPermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{locationPermission}, REQUEST_LOCATION_PERMISSION);
        } else {
            onLocationPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is granted, the results array won't be empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted();
                } else {
                    final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
                    // Explain the user why the app requires location permission and then ask for it
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, locationPermission)) {
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.location_permission_title))
                                .setMessage(getString(R.string.location_permission_message))
                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{locationPermission},
                                                REQUEST_LOCATION_PERMISSION);
                                    }
                                }).create().show();
                    } else {
                        // User checked "never ask again", show explanation and switch to app settings
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.location_permission_title))
                                .setMessage(getString(R.string.location_permission_message))
                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }).create().show();
                    }
                    onLocationPermissionDenied();
                }
            }
        }
    }

    private void onLocationSettingsSatisfied() {
        checkLocationPermission();
    }

    private void onLocationSettingsUnsatisfied() {
        onOpenNoLocationScreen();
    }

    private void onLocationPermissionDenied() {
        // Treat denied location permission as if the device location is disabled
        onOpenNoLocationScreen();
    }

    private void onLocationPermissionGranted() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(Constants.LOCATION_REQUEST_PRIORITY);

        // If there's nothing on the stack (no fragment loaded), load map
        if (mFragmentManager.getBackStackEntryCount() < 1) {
            onOpenMap();
        }
    }

    public void onOpenMap() {
        MapFragment mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(MapFragment.FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
        }
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, mapFragment, MapFragment.FRAGMENT_TAG)
                .commit();
    }

    private void onOpenNoLocationScreen() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, NoLocationFragment.newInstance(),
                        NoLocationFragment.FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onNoLocationContinueClick() {
        checkLocationSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_profile_item:
                Intent i = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(i);
                return true;
            case R.id.action_bar_logout_item:
                FirebaseProvider.getInstance().getAuthInstance().signOut();
                Intent logoutIntent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(logoutIntent);
                finish();
                return true;
        }

        // If none of the 'case' statements return true, we return false to let a specific fragment handle the option
        return false;
    }

}
