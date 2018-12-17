package rs.elfak.mosis.akitoske.bfit.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import rs.elfak.mosis.akitoske.bfit.App;
import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.services.BackgroundLocationService;

public class SettingsActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new MySettingsFragment())
                .commit();


        /*
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();

        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(SettingsActivity.this, "Device doesn't support Bluetooth.", Toast.LENGTH_LONG).show();
        }

        if(mBluetoothAdapter.isEnabled())
        {
            Switch s = (Switch) findViewById(R.id.switch_bluetooth);
            s.setChecked(true);
        }

        findViewById(R.id.switch_bluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Switch s = (Switch) findViewById(R.id.switch_bluetooth);
                if (s.isChecked() == true) {
                    // Toast.makeText(SettingsActivity.this, "Bluetooth on...", Toast.LENGTH_LONG).show();
                    //visible
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBtIntent);

                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);


                    }
                    //server

                } else
                {
                    //Toast.makeText(SettingsActivity.this, "Bluetooth off", Toast.LENGTH_LONG).show();
                    mBluetoothAdapter.disable();
                }

            }
        });
*/

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    public static class MySettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref_background_service_key))) {
                boolean serviceEnabled = sharedPreferences.getBoolean(
                        getString(R.string.pref_background_service_key),
                        false
                );

                if (serviceEnabled) {
                    Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
                    App.getContext().startService(backgroundLocationIntent);
                } else {
                    App.getContext().stopService(new Intent(App.getContext(), BackgroundLocationService.class));
                }
            }
        }

    }
}
