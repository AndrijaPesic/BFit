package rs.elfak.mosis.akitoske.bfit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.activities.MainActivity;
import rs.elfak.mosis.akitoske.bfit.models.CoordsModel;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;
import rs.elfak.mosis.akitoske.bfit.services.ForegroundLocationService;

public class MapFragment extends BaseFragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener{

    public static final String FRAGMENT_TAG = "MapFragment";

    private Context mContext;
    private MapView mMapView;
    private Circle mCircle;
    private float mRadius = 0;
    private Map<Marker, GoogleMap.OnMarkerClickListener> mMarkerListeners = new HashMap<>();
    private CoordsModel mMyLocation;
    private CoordsModel mMyLastKnownLocation;
    private GoogleMap mGoogleMap;

    private GoogleMap.OnMarkerClickListener mUserMarkerListener;

    private GeoQuery mUsersGeoQuery;

    private BroadcastReceiver mUserLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            onNewLocation(new CoordsModel(latitude, longitude));
        }
    };

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.VISIBLE);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserMarkerListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onUserMarkerClick(marker);
                return true;
            }
        };
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return mMarkerListeners.get(marker).onMarkerClick(marker);
    }

    private void onUserMarkerClick(Marker marker) {
        UserModel user = (UserModel) marker.getTag();
        if (mListener != null) {
            mListener.onOpenUserProfile(user.getId());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) inflatedView.findViewById(R.id.map_fragment_map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return inflatedView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));

        try {
            mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // We're not handling the exception here because we receive locations from the service
            // that will only be enabled if we have location permission
            e.printStackTrace();
        }

        if (mMyLastKnownLocation != null && mMyLocation == null) {
            onNewLocation(mMyLastKnownLocation);
        }
    }

    private void onNewLocation(CoordsModel loc) {
        LatLng center = new LatLng(loc.getLatitude(), loc.getLongitude());
        GeoLocation geoLoc = new GeoLocation(loc.getLatitude(), loc.getLongitude());

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        GeoFire usersGeoFire = firebaseProvider.getUsersGeoFire();

        // We need to setup some things only when we receive location for the first time,
        // such as to move camera there, create the circle, starting querying the area...
        if (mMyLocation == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16.0f - 0.3f *
                    mRadius));

            mCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .radius(mRadius)
                    .strokeWidth(10)
                    .strokeColor(Color.argb(80, 69, 90, 100))
                    .fillColor(Color.argb(40, 255, 171, 0))
            );

            mUsersGeoQuery = usersGeoFire.queryAtLocation(geoLoc, mRadius / 1000);

        } else {
            mCircle.setCenter(center);
            // Update the center of the area we're querying for users
            mUsersGeoQuery.setCenter(geoLoc);
        }

        mMyLocation = loc;

        if (mMyLastKnownLocation != null) {
            mMyLastKnownLocation = null;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

        mMyLastKnownLocation = mMyLocation;
        mMyLocation = null;

        if (mCircle != null) {
            mCircle.remove();
            mCircle = null;
        }

        if (mUsersGeoQuery != null) {
            mUsersGeoQuery.removeAllListeners();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        // Register a receiver for any changes in the user data (eg. from ForegroundLocationService)
        localBroadcastManager.registerReceiver(mUserLocationReceiver,
                new IntentFilter(ForegroundLocationService.USER_LOCATION_UPDATED_INTENT_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        // Unregister the receivers in onPause because we can guarantee its execution
        localBroadcastManager.unregisterReceiver(mUserLocationReceiver);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
