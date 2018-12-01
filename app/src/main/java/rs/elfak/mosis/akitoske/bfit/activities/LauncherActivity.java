package rs.elfak.mosis.akitoske.bfit.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        if (!loggedIn) {
            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
        } else {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        }
        finish();
    }
}
