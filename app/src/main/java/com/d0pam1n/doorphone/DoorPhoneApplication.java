package com.d0pam1n.doorphone;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.d0pam1n.doorphone.ui.MainActivity;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Manager for basic application stuff.
 *
 * @author Andreas Pfister
 */

@ReportsCrashes(mailTo = "andi.dopamin@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)

public class DoorPhoneApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static boolean sIsMainActivityVisible;
    private static boolean sQuitApp;

    public static boolean IsMainActivityVisible() {
        return sIsMainActivityVisible;
    }

    public static boolean getQuitApp() { return sQuitApp;}

    public static void setQuitApp(boolean quit) { sQuitApp = quit; }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity) {
            sIsMainActivityVisible = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof MainActivity) {
            sIsMainActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
