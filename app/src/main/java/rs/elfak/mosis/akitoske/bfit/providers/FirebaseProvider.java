package rs.elfak.mosis.akitoske.bfit.providers;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;

public class FirebaseProvider {

    private static FirebaseProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Firebase Realtime Database references
    private DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersDbRef = mDbRef.child("users");
    private DatabaseReference mStructuresDbRef = mDbRef.child("structures");

    // Firebase Storage references
    private StorageReference mAvatarsStorageRef = FirebaseStorage.getInstance().getReference().child("avatars");

    public static synchronized FirebaseProvider getInstance() {
        if (mInstance == null) {
            mInstance = new FirebaseProvider();
        }
        return mInstance;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public UploadTask uploadAvatarImage(String fileName, String localImgUri) {
        return mAvatarsStorageRef.child(fileName).putFile(Uri.fromFile(new File(localImgUri)));
    }

    public Task<Void> updateUserInfo(String userId, Map<String, Object> newUserValues) {
        return mUsersDbRef.child(userId).updateChildren(newUserValues);
    }
}
