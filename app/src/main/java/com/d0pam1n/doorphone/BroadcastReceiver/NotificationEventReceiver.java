package com.d0pam1n.doorphone.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.d0pam1n.doorphone.R;
import com.d0pam1n.siplib.SipCommands;

/**
 * Receiver for notification actions.
 *
 * @author Andreas Pfister
 */

public class NotificationEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action))) {
            String accountID = intent.getStringExtra(context.getString(R.string.param_account_id));
            int callID = intent.getIntExtra(context.getString(R.string.param_call_id), 0);

            SipCommands.acceptIncomingCall(context.getApplicationContext(), accountID, callID);
        } else if (intent.getAction().equals(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action_open_door))) {
            String accountID = intent.getStringExtra(context.getString(R.string.param_account_id));
            int callID = intent.getIntExtra(context.getString(R.string.param_call_id), 0);
            String dtmf = intent.getStringExtra(context.getString(R.string.param_dtmf_open_door));

            SipCommands.sendDTMF(context.getApplicationContext(), accountID, callID, dtmf);
        } else if (intent.getAction().equals(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action_hangup))) {
            String accountID = intent.getStringExtra(context.getString(R.string.param_account_id));
            int callID = intent.getIntExtra(context.getString(R.string.param_call_id), 0);

            SipCommands.hangUpCall(context.getApplicationContext(), accountID, callID);
        }
    }
}
