package rs.elfak.mosis.akitoske.bfit.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int STATUS_UNKNOWN = 0;
    private static final int STATUS_MYSELF = 1;
    private static final int STATUS_NOT_FRIEND = 2;
    private static final int STATUS_REQUEST_SENT = 3;
    private static final int STATUS_REQUEST_RECEIVED = 4;
    private static final int STATUS_FRIEND = 5;

    private String mUserId;
    private UserModel mUser;
    private int mStatus = STATUS_UNKNOWN;

    // UI elements
    FirebaseAuth mAuth;
    ImageView mAvatarImage;
    TextView mDisplayName;
    TextView mFullName;
    LinearLayout mFriendRequestGroup;
    Button mFriendRequestBtn;

    private ValueEventListener mLoggedUserListener;
    private DatabaseReference mLoggedUserDbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent myIntent = getIntent();
        mUserId = myIntent.getStringExtra("userId");

        mAvatarImage = findViewById(R.id.profile_avatar_image);
        mDisplayName = findViewById(R.id.profile_display_name);
        mFullName = findViewById(R.id.profile_full_name);
        mFriendRequestGroup = findViewById(R.id.profile_friend_request_group);
        mFriendRequestBtn = findViewById(R.id.profile_friend_request_btn);
        mFriendRequestBtn.setCompoundDrawablePadding(16);

        getUserDataAndSetupUI(mUserId);
    }

    private void getUserDataAndSetupUI(String userId) {
        final String loggedUserId = FirebaseProvider.getInstance().getCurrentFirebaseUser().getUid();

        // Just once, get the user's profile data
        FirebaseProvider.getInstance().getUserById(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(UserModel.class);
                        mUser.setId(dataSnapshot.getKey());
                        setupUI();

                        if (mUserId.equals(loggedUserId)) {
                            mStatus = STATUS_MYSELF;
                        } else if (mUser.getFriendRequests().containsKey(loggedUserId)) {
                            mStatus = STATUS_REQUEST_SENT;
                        } else if (mUser.getFriends().containsKey(loggedUserId)) {
                            mStatus = STATUS_FRIEND;
                        }
                        setupFriendRequestButton();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        // Track logged user's data so we can update if we receive a request
        mLoggedUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel loggedUser = dataSnapshot.getValue(UserModel.class);
                loggedUser.setId(dataSnapshot.getKey());

                if (loggedUser.getFriends().containsKey(mUserId)) {
                    mStatus = STATUS_FRIEND;
                } else if (loggedUser.getFriendRequests().containsKey(mUserId)) {
                    mStatus = STATUS_REQUEST_RECEIVED;
                } else if ((mStatus == STATUS_REQUEST_RECEIVED && !loggedUser.getFriendRequests().containsKey(mUserId))
                        || (mStatus == STATUS_FRIEND && !loggedUser.getFriends().containsKey(mUserId))) {
                    mStatus = STATUS_NOT_FRIEND;
                }
                setupFriendRequestButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mLoggedUserDbRef = FirebaseProvider.getInstance().getUserById(loggedUserId);
        mLoggedUserDbRef.addValueEventListener(mLoggedUserListener);

    }

    private void setupUI() {
        Glide.with(ProfileActivity.this)
                .load(mUser.getAvatarUrl())
                .into(mAvatarImage);
        mDisplayName.setText(mUser.getDisplayName());
        mFullName.setText(mUser.getFullName());
    }

    private void setupFriendRequestButton() {
        if (mStatus == STATUS_MYSELF) {
            return;
        }

        switch (mStatus) {
            case STATUS_FRIEND:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_unfriend));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(this, R.color.colorPrimaryLight),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                Drawable removeIcon = ContextCompat.getDrawable(this, R.drawable.ic_remove_circle_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);
                break;
            case STATUS_REQUEST_SENT:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_cancel));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(this, R.color.colorPrimaryLight),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                Drawable cancelIcon = ContextCompat.getDrawable(this, R.drawable.ic_cancel_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(cancelIcon, null, null, null);
                break;
            case STATUS_REQUEST_RECEIVED:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_accept));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(this, R.color.colorFriends),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                Drawable acceptIcon = ContextCompat.getDrawable(this, R.drawable.ic_check_circle_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(acceptIcon, null, null, null);
                break;
            default:
                mStatus = STATUS_NOT_FRIEND;
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_add));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(this, R.color.colorFriends),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                Drawable addIcon = ContextCompat.getDrawable(this, R.drawable.ic_person_add_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(addIcon, null, null, null);
                break;
        }
        mFriendRequestBtn.setOnClickListener(this);
        mFriendRequestGroup.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_friend_request_btn:
                mFriendRequestBtn.setOnClickListener(null);
                String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                switch (mStatus) {
                    case STATUS_FRIEND:
                        removeFriendship(myUserId, mUserId);
                        break;
                    case STATUS_REQUEST_SENT:
                        removeFriendRequest(myUserId, mUserId);
                        break;
                    case STATUS_REQUEST_RECEIVED:
                        acceptFriendRequest(mUserId, myUserId);
                        break;
                    case STATUS_NOT_FRIEND:
                        sendFriendRequest(myUserId, mUserId);
                        break;
                }
                break;
        }
    }

    private void sendFriendRequest(String fromUserId, String toUserId) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.sendFriendRequest(fromUserId, mUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_REQUEST_SENT;
                        setupFriendRequestButton();
                    }
                });
    }

    private void removeFriendRequest(String fromUserId, String toUserId) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.removeFriendRequest(fromUserId, mUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_NOT_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

    private void acceptFriendRequest(String fromUserId, String toUserId) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.addFriendship(fromUserId, toUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

    private void removeFriendship(String firstUserId, String secondUserId) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.removeFriendship(firstUserId, secondUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_NOT_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

}
