package rs.elfak.mosis.akitoske.bfit.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;


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
import rs.elfak.mosis.akitoske.bfit.fragments.AddChallengeFragment;
import rs.elfak.mosis.akitoske.bfit.fragments.FriendsFragment;
import rs.elfak.mosis.akitoske.bfit.fragments.LeaderboardFragment;
import rs.elfak.mosis.akitoske.bfit.fragments.MapFragment;
import rs.elfak.mosis.akitoske.bfit.fragments.NoLocationFragment;
import rs.elfak.mosis.akitoske.bfit.models.CardioModel;
import rs.elfak.mosis.akitoske.bfit.models.ChallengeModel;
import rs.elfak.mosis.akitoske.bfit.models.ChallengeType;
import rs.elfak.mosis.akitoske.bfit.models.CoordsModel;
import rs.elfak.mosis.akitoske.bfit.models.FriendModel;
import rs.elfak.mosis.akitoske.bfit.models.StrengthModel;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;
import rs.elfak.mosis.akitoske.bfit.receivers.LocationProvidersChangedReceiver;
import rs.elfak.mosis.akitoske.bfit.services.ForegroundLocationService;
import rs.elfak.mosis.akitoske.bfit.services.UserUpdatesService;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FriendsFragment.OnListFragmentInteractionListener,
        LeaderboardFragment.OnListFragmentInteractionListener,
        AddChallengeFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        NoLocationFragment.OnFragmentInteractionListener{

    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 2;

    public static final int FILTER_ALL = 0;
    public static final int FILTER_FRIENDS = 1;
    public static final int FILTER_OTHERS = 2;

    private FragmentManager mFragmentManager;

    private boolean mUserUpdatesBound = false;
    private UserUpdatesService mUserUpdatesService;

    private boolean mUserLocationsBound = false;
    private ForegroundLocationService mUserLocationService;

    private String mLoggedUserId;
    private UserModel mLoggedUser;

    TextView mFriendRequestsCountTv;
    Spinner mFilterSpinner;

    private BroadcastReceiver mUserUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUserDataUpdated();
        }
    };

    private BroadcastReceiver mLocationProvidersChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isEnabled = intent.getBooleanExtra(LocationProvidersChangedReceiver.PROVIDERS_STATUS_KEY, false);
            onLocationProvidersChanged(isEnabled);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFilterSpinner = findViewById(R.id.toolbar_filter_spinner);

        // Setup default shared preferences if they haven't been setup already
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        mLoggedUserId = firebaseProvider.getCurrentFirebaseUser().getUid();

        mFragmentManager = getSupportFragmentManager();

        // Initialize the action bar spinner for filtering map markers
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(
                this,
                R.layout.toolbar_spinner_selected_item,
                getResources().getStringArray(R.array.filter_array)
        );
        spinAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);
        mFilterSpinner.setAdapter(spinAdapter);
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

    private void onLocationSettingsSatisfied() {
        checkLocationPermission();
    }

    private void onLocationSettingsUnsatisfied() {
        onOpenNoLocationScreen();
    }

    @Override
    public void onOpenChallenge(ChallengeModel challenge) {
        boolean isLoggedUserOwner = challenge.getOwnerId().equals(mLoggedUserId);
        if (isLoggedUserOwner) {
            //DO SOMETHING
        } else {
            //DO SOMETHING ELSE
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

    private void onLocationProvidersChanged(boolean isEnabled) {
        if (isEnabled) {
            onLocationSettingsSatisfied();
        } else {
            onLocationSettingsUnsatisfied();
        }
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

        Intent userLocationIntent = new Intent(this, ForegroundLocationService.class);
        bindService(userLocationIntent, mUserLocationConnection, Context.BIND_AUTO_CREATE);
        Intent userUpdatesIntent = new Intent(this, UserUpdatesService.class);
        bindService(userUpdatesIntent, mUserUpdatesConnection, Context.BIND_AUTO_CREATE);

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

    // This is called when a specific challenge is chosen, not when the AddChallenge FAB is clicked
    @Override
    public void onAddChallengeClick(final ChallengeType challengeType) {
        if (mLoggedUser.getPower() < challengeType.getBaseCost()) {
            Toast.makeText(this, getString(R.string.challenge_no_power_message), Toast.LENGTH_SHORT).show();
            return;
        }

        Location userLocation = new Location("dummyprovider");
        userLocation.setLatitude(mLoggedUser.getCoords().getLatitude());
        userLocation.setLongitude(mLoggedUser.getCoords().getLongitude());

        MapFragment mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(MapFragment.FRAGMENT_TAG);
        if (mapFragment == null || !mapFragment.canAddOnLocation(userLocation)) {
            Toast.makeText(this, "Can't build right here.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        // We can use the constantly-updated mLoggedUser to get the location for building
        addChallenge(challengeType, mLoggedUser.getCoords());
    }

    private void addChallenge(ChallengeType challengeType, CoordsModel coords) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        int newUserPowerValue = mLoggedUser.getPower() - challengeType.getBaseCost();
        int newUserPoints = mLoggedUser.getPower() + (mLoggedUser.getPower() - newUserPowerValue);
        switch (challengeType) {
            case CARDIO:
                CardioModel newCardioChallenge = new CardioModel(challengeType, mLoggedUserId, coords);
                firebaseProvider.addCardioChallenge(newCardioChallenge, coords, newUserPowerValue, newUserPoints)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFragmentManager.popBackStack();
                                Toast.makeText(MainActivity.this, "New Cardio Challenge Added.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            case STRENGTH:
                StrengthModel newStrengthChallenge= new StrengthModel(challengeType, mLoggedUserId, coords);
                firebaseProvider.addStrengthChallenge(newStrengthChallenge, coords, newUserPowerValue, newUserPoints)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFragmentManager.popBackStack();
                                Toast.makeText(MainActivity.this, "New Strength Challenge Added.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_main_menu, menu);

        View friendsItemView = menu.findItem(R.id.action_friends_item).getActionView();
        // We need to manually set the click listener for our custom options item
        // because we have used the "actionLayout" parameter in the xml
        friendsItemView.setOnClickListener(this);
        mFriendRequestsCountTv = friendsItemView.findViewById(R.id.friend_requests_count_tv);
        updateFriendRequestsCount();

        return super.onCreateOptionsMenu(menu);
    }

    private void updateFriendRequestsCount() {
        // Exit if for some reason the UI element is not present
        if (mFriendRequestsCountTv == null) return;

        if (mLoggedUser == null || mLoggedUser.getFriendRequests().size() == 0) {
            mFriendRequestsCountTv.setVisibility(View.INVISIBLE);
        } else {
            mFriendRequestsCountTv.setText(String.valueOf(mLoggedUser.getFriendRequests().size()));
            mFriendRequestsCountTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_friends_item:
                mFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_fragment_container, FriendsFragment.newInstance(), FriendsFragment.FRAGMENT_TAG)
                        .addToBackStack(null)
                        .commit();
        }
    }

    private void onOpenLeaderboard() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container,
                        LeaderboardFragment.newInstance(mLoggedUserId, mLoggedUser.getFriends()),
                        LeaderboardFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_profile_item:
                Intent i = new Intent(MainActivity.this,ProfileActivity.class);
                i.putExtra("userId", mLoggedUserId);
                startActivity(i);
                return true;
            case R.id.action_bar_leaderboard_item:
                onOpenLeaderboard();
                return true;
            case R.id.action_bar_settings_item:
                i = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_bar_about_item:
                i = new Intent(MainActivity.this,AboutActivity.class);
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

    private void onUserDataUpdated() {
        mLoggedUser = mUserUpdatesService.getUser();
        updateFriendRequestsCount();
    }

    private ServiceConnection mUserUpdatesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UserUpdatesService.LocalBinder binder = (UserUpdatesService.LocalBinder) service;
            mUserUpdatesService = binder.getService();
            mUserUpdatesBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUserUpdatesBound = false;
        }
    };

    private ServiceConnection mUserLocationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundLocationService.LocalBinder binder = (ForegroundLocationService.LocalBinder) service;
            mUserLocationService = binder.getService();
            mUserLocationsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUserLocationsBound = false;
        }
    };


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
    public void onFriendItemClick(FriendModel friendItem) {
        Intent i = new Intent(MainActivity.this,ProfileActivity.class);
        i.putExtra("userId", friendItem.userId);
        startActivity(i);
    }

    @Override
    public void onLeaderboardItemClick(String userId) {
        Intent i = new Intent(MainActivity.this,ProfileActivity.class);
        i.putExtra("userId", userId);
        startActivity(i);
    }

    @Override
    public void onFriendRequestAccept(final FriendModel friend) {
        FirebaseProvider.getInstance().addFriendship(mLoggedUserId, friend.userId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(friend);
                            friendsFragment.addFriend(friend);
                        }
                    }
                });
    }

    @Override
    public void onFriendRequestDecline(final FriendModel fromUser) {
        FirebaseProvider.getInstance().removeFriendRequest(fromUser.userId, mLoggedUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(fromUser);
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkLocationSettings();

        mFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MapFragment mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(MapFragment.FRAGMENT_TAG);
                if (mapFragment != null) {
                    mapFragment.onFilterChanged(position, mLoggedUser);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        // Register a receiver for any changes in the user data (eg. from UserUpdatesService)
        localBroadcastManager.registerReceiver(mUserUpdatesReceiver,
                new IntentFilter(UserUpdatesService.USER_UPDATED_INTENT_ACTION));
        // Register a receiver for any changes in location providers (eg. from LocationProvidersChangedReceiver)
        localBroadcastManager.registerReceiver(mLocationProvidersChangedReceiver,
                new IntentFilter(LocationProvidersChangedReceiver.PROVIDERS_CHANGED_INTENT_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        // Unregister the receivers in onPause because we can guarantee its execution
        localBroadcastManager.unregisterReceiver(mUserUpdatesReceiver);
        localBroadcastManager.unregisterReceiver(mLocationProvidersChangedReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from any services this activity is bound to
        if (mUserUpdatesBound) {
            unbindService(mUserUpdatesConnection);
            mUserUpdatesBound = false;
        }
        if (mUserLocationsBound) {
            unbindService(mUserLocationConnection);
            mUserLocationsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager = null;
        mLoggedUserId = null;
    }


}
