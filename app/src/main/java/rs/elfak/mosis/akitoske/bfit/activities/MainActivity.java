package rs.elfak.mosis.akitoske.bfit.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

}
