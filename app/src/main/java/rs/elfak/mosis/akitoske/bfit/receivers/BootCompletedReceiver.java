package rs.elfak.mosis.akitoske.bfit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rs.elfak.mosis.akitoske.bfit.App;
import rs.elfak.mosis.akitoske.bfit.services.BackgroundLocationService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
            context.startService(backgroundLocationIntent);
        }
    }

}
