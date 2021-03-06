package rs.elfak.mosis.akitoske.bfit.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import rs.elfak.mosis.akitoske.bfit.models.CoordsModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class ForegroundLocationService extends Service {

    private static final String TAG = "ForegroundLocationService";

    public static final String USER_LOCATION_UPDATED_INTENT_ACTION = "rs.elfak.mosis.akitoske.bfit.user-location-update";

    private final IBinder mLocalBinder = new ForegroundLocationService.LocalBinder();

    private String mUserId;

    private FirebaseProvider mFirebaseProvider;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        // Called when the service is bound for the first time,
        // we can start timed operations here
        mFirebaseProvider = FirebaseProvider.getInstance();
        mUserId = mFirebaseProvider.getCurrentFirebaseUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onNewLocation(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );
            }
        };

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // We're not handling the permission exception here because this service shouldn't
        // ever be started and bound without location permission
        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Called when the last bound Activity is unbound from
        // this service, so we stop timed operations here
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        return super.onUnbind(intent);
    }

    private void onNewLocation(double latitude, double longitude) {
        mFirebaseProvider.updateUserLocation(mUserId, new CoordsModel(latitude, longitude));
        Intent intent = new Intent(USER_LOCATION_UPDATED_INTENT_ACTION);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public ForegroundLocationService getService() {
            return ForegroundLocationService.this;
        }
    }

}
