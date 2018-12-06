package rs.elfak.mosis.akitoske.bfit.services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.activities.MainActivity;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = BackgroundLocationService.class.getSimpleName();

    private int mStartId;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseUser mUser;
    private Location mMyLocation;
    private GeoFire mUsersGeoFire;
    private GeoQuery mUsersGeoQuery;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("usersGeoFire"));

        Log.i(TAG, "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        // We test if it's already connected in case "startService()" gets called multiple times
        // so we don't try to reconnect every single time if we're already connected
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        Log.i(TAG, "starting...");

        // We want this service to restart if the system disabled it because of low
        // memory at the very moment, so we return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        mMyLocation = null;

        Log.i(TAG, "destroying...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean serviceEnabled = sharedPref.getBoolean(getString(R.string.pref_background_service_key), true);

        // If the user disabled the service in settings, or we have no location permission, we stop the service
        if (mUser == null || !serviceEnabled ||
                (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            Log.i(TAG, "can't receive locations (user not logged in, service disabled in settings or no permission)");

            // Just stop the service if there's no location permission
            stopSelf(mStartId);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Log.i(TAG, "connected to GoogleApiClient, should receive locations now...");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location loc) {
        GeoLocation geoLoc = new GeoLocation(loc.getLatitude(), loc.getLongitude());

        if (mMyLocation == null) {
            mUsersGeoQuery = mUsersGeoFire.queryAtLocation(geoLoc, 0.100);
            addUserGeoQueryEventListener();
        } else {
            mUsersGeoFire.setLocation(mUser.getUid(), geoLoc);
            mUsersGeoQuery.setCenter(geoLoc);
        }

        mMyLocation = loc;

        Log.i(TAG, "New location received: " + loc.getLatitude() + "   " + loc.getLongitude());
    }

    // We use this to send notifications ONLY if our app is not running in the foreground
    private boolean isAppInForeground() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcesses = manager.getRunningAppProcesses();

        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(this.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    private void addUserGeoQueryEventListener() {
        mUsersGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i(TAG, "nearby key recognized -> " + key);
                // We are only concerned for notifications if the app is not in the foreground
                // and if the registered nearby key is not our own
                if (!isAppInForeground() && !key.equals(mUser.getUid())) {
                    Log.i(TAG, "key is not our own and the app is in the background");
                    FirebaseProvider.getInstance().getUserById(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserModel nearbyUser = dataSnapshot.getValue(UserModel.class);
                            NotificationCompat.Builder builder =
                                    new NotificationCompat.Builder(BackgroundLocationService.this)
                                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                                    R.mipmap.ic_launcher))
                                            .setContentTitle("User " + nearbyUser.getFullName() + " is nearby!")
                                            .setContentText("Touch to see what they're up to.")
                                            .setAutoCancel(true);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            builder.setContentIntent(resultPendingIntent);
                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            notificationManager.notify(1, builder.build());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}
