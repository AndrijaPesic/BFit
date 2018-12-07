package rs.elfak.mosis.akitoske.bfit;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

import rs.elfak.mosis.akitoske.bfit.services.BackgroundLocationService;

public class App extends Application {
    private static WeakReference<Context> mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<Context>(this);

        Intent backgroundLocationIntent = new Intent(this, BackgroundLocationService.class);
        startService(backgroundLocationIntent);
    }

    public static Context getContext() {
        return mContext.get();
    }
}
