package rs.elfak.mosis.akitoske.bfit.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ImageView mAvatarImage;
    TextView mDisplayName;
    TextView mFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference users = FirebaseProvider.getInstance().getUserById(mAuth.getCurrentUser().getUid());
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                mAvatarImage = (ImageView) findViewById(R.id.profile_avatar_image);
                mDisplayName = (TextView) findViewById(R.id.profile_display_name);
                mFullName = (TextView) findViewById(R.id.profile_full_name);
                if (userModel != null){
                    if(userModel.getAvatarUrl() !=null)
                    {
                        Glide.with(ProfileActivity.this)
                                .load(userModel.getAvatarUrl())
                                .into(mAvatarImage);
                    }
                    if(userModel.getDisplayName() != null)
                    {
                        mDisplayName.setText(userModel.getDisplayName());
                        setTitle(userModel.getDisplayName());
                    }
                    if(userModel.getFullName() != null)
                    {
                        mFullName.setText(userModel.getFullName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
