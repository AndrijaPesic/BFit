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
import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.models.CardioModel;
import rs.elfak.mosis.akitoske.bfit.models.CoordsModel;
import rs.elfak.mosis.akitoske.bfit.models.StrengthModel;

public class FirebaseProvider {

    private static FirebaseProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Firebase Realtime Database references
    private DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersDbRef = mDbRef.child("users");
    private DatabaseReference mChallengesDbRef = mDbRef.child("challenges");

    // Firebase Storage references
    private StorageReference mAvatarsStorageRef = FirebaseStorage.getInstance().getReference().child("avatars");

    // Geofire
    private GeoFire mUsersGeoFire = new GeoFire(mDbRef.child("usersGeoFire"));
    private GeoFire mChallengesGeoFire = new GeoFire(mDbRef.child("challengesGeoFire"));

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

    public Task<Void> sendFriendRequest(String fromUserId, String toUserId) {
        return mUsersDbRef.child(toUserId).child("friendRequests").child(fromUserId).setValue(true);
    }

    public Task<Void> removeFriendRequest(String fromUserId, String toUserId) {
        return mUsersDbRef.child(toUserId).child("friendRequests").child(fromUserId).removeValue();
    }

    public Task<Void> addFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendRequestPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendRequestPath(secondUserId, firstUserId), null);
        updates.put(getUserFriendPath(firstUserId, secondUserId), true);
        updates.put(getUserFriendPath(secondUserId, firstUserId), true);

        return mUsersDbRef.updateChildren(updates);
    }

    public Task<Void> removeFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendPath(secondUserId, firstUserId), null);

        return mUsersDbRef.updateChildren(updates);
    }

    private String getUserFriendPath(String userId, String friendUserId) {
        return "/" + userId + "/friends/" + friendUserId;
    }

    private String getUserFriendRequestPath(String fromUserId, String toUserId) {
        return "/" + toUserId + "/friendRequests/" + fromUserId;
    }

    // ********************************************* CHALLENGES ********************************************* //

    public DatabaseReference getChallengeById(String challengeId) {
        return mChallengesDbRef.child(challengeId);
    }

    public Task<Void> addCardioChallenge(CardioModel cardioChallenge, CoordsModel coords, int newUserPowerValue, int newUserPoints) {
        String newChallengesKey = mChallengesDbRef.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/challenges/" + newChallengesKey, cardioChallenge);
        updates.put("/users/" + cardioChallenge.getOwnerId() + "/challenges/" + newChallengesKey, true);
        updates.put("/users/" + cardioChallenge.getOwnerId() + "/power", newUserPowerValue);
        updates.put("/users/" + cardioChallenge.getOwnerId() + "/points", newUserPoints);

        GeoLocation geoLoc = new GeoLocation(coords.getLatitude(), coords.getLongitude());
        mChallengesGeoFire.setLocation(newChallengesKey, geoLoc, new
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

        return mDbRef.updateChildren(updates);
    }

    public Task<Void> addStrengthChallenge(StrengthModel strengthChallenge, CoordsModel coords, int newUserPowerValue, int newUserPoints) {
        String newChallengesKey = mChallengesDbRef.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/challenges/" + newChallengesKey, strengthChallenge);
        updates.put("/users/" + strengthChallenge.getOwnerId() + "/challenges/" + newChallengesKey, true);
        updates.put("/users/" + strengthChallenge.getOwnerId() + "/power", newUserPowerValue);
        updates.put("/users/" + strengthChallenge.getOwnerId() + "/points", newUserPoints);

        GeoLocation geoLoc = new GeoLocation(coords.getLatitude(), coords.getLongitude());
        mChallengesGeoFire.setLocation(newChallengesKey, geoLoc, new
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

        return mDbRef.updateChildren(updates);
    }

    public GeoFire getChallengesGeoFire() { return mChallengesGeoFire; }
}
