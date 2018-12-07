package rs.elfak.mosis.akitoske.bfit.providers;

import android.net.Uri;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.models.CoordsModel;

public class FirebaseProvider {

    private static FirebaseProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Firebase Realtime Database references
    private DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersDbRef = mDbRef.child("users");

    // Firebase Storage references
    private StorageReference mAvatarsStorageRef = FirebaseStorage.getInstance().getReference().child("avatars");

    // Geofire
    private GeoFire mUsersGeoFire = new GeoFire(mDbRef.child("usersGeoFire"));

    public static synchronized FirebaseProvider getInstance() {
        if (mInstance == null) {
            mInstance = new FirebaseProvider();
        }
        return mInstance;
    }

    private FirebaseProvider() {
        // private constructor required for singleton
    }

    public FirebaseAuth getAuthInstance() {
        return mAuth;
    }

    // *********************************************** USERS *********************************************** //

    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    public DatabaseReference getAllUsers() {
        return mUsersDbRef;
    }

    public DatabaseReference getUserById(String userId) {
        return mUsersDbRef.child(userId);
    }

    public DatabaseReference getCurrentUser() {
        return mUsersDbRef.child(mAuth.getCurrentUser().getUid());
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public StorageReference uploadAvatarImage(String fileName) {
        return mAvatarsStorageRef.child(fileName);
    }

    public Task<Void> updateUserInfo(String userId, Map<String, Object> newUserValues) {
        return mUsersDbRef.child(userId).updateChildren(newUserValues);
    }

    public Task<Void> updateUserLocation(String userId, CoordsModel coords) {

        mUsersGeoFire.setLocation(userId, new GeoLocation(coords.getLatitude(), coords.getLongitude()), new
                GeoFire.CompletionListener(){
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                        }
                    }
                });

       return mUsersDbRef.child(userId).child("coords").setValue(coords);
    }

    public GeoFire getUsersGeoFire() {
        return mUsersGeoFire;
    }

}
