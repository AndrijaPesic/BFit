package rs.elfak.mosis.akitoske.bfit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        routeToAppropriatePage();
        finish();
    }

    private void routeToAppropriatePage(){
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }
}
