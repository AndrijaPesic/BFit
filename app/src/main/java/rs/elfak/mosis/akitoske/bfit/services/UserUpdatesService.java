package rs.elfak.mosis.akitoske.bfit.services;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class UserUpdatesService extends Service implements ValueEventListener {

    private static final String TAG = "UserUpdatesService";

    public static final String USER_UPDATED_INTENT_ACTION = "rs.elfak.mosis.akitoske.bfit.user-update";
    public static final String USER_LOCATION_UPDATED_INTENT_ACTION = "rs.elfak.mosis.akitoske.bfit.user-location-update";

    private final IBinder mLocalBinder = new LocalBinder();

    private DatabaseReference mUserDbRef;
    private UserModel mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        // Called when the service is bound for the first time,
        // we can start timed operations here
        mUserDbRef = FirebaseProvider.getInstance().getCurrentUser();
        mUserDbRef.addValueEventListener(this);
    }

    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Called when the last bound Activity is unbound from
        // this service, so we stop timed operations here
        mUserDbRef.removeEventListener(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // Update the user object and locally broadcast an intent
        mUser = dataSnapshot.getValue(UserModel.class);
        mUser.setId(dataSnapshot.getKey());
        Intent intent = new Intent(USER_UPDATED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public UserModel getUser() {
        return mUser;
    }

    public class LocalBinder extends Binder {
        public UserUpdatesService getService() {
            return UserUpdatesService.this;
        }
    }
}
