package rs.elfak.mosis.akitoske.bfit.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import rs.elfak.mosis.akitoske.bfit.R;

public class SettingsActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

    }
}
