package com.d0pam1n.doorphone.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.SipCommands;

/**
 * Receiver for network changes.
 *
 * @author Andreas Pfister
 */

public class NetworkChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SipCommands.reRegisterAccount(context, SettingsUtil.getConfiguredSipAccount(context).getIdUri());
    }
}
