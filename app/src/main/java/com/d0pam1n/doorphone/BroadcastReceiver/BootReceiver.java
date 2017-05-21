package com.d0pam1n.doorphone.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.SipCommands;

/**
 * Receiver after device boot up.
 *
 * @author Andreas Pfister
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (SettingsUtil.getIsServiceAutostart(context)) {
            SipCommands.startService(context);
        }

    }
}
