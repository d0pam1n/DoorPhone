package com.d0pam1n.doorphone.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

import com.d0pam1n.doorphone.DoorPhoneApplication;
import com.d0pam1n.doorphone.R;
import com.d0pam1n.doorphone.ui.MainActivity;
import com.d0pam1n.doorphone.utils.NotificationBuilderUtil;
import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.BroadcastHandler.BroadcastEventReceiver;

import org.pjsip.pjsua2.pjsip_inv_state;

/**
 * Receiver for sip events from the sip library.
 *
 * @author Andreas Pfister
 */

public class SipEventReceiver extends BroadcastEventReceiver {

    @Override
    public void onIncomingCall(String accountID, int callID, String remoteUri) {

        if (SettingsUtil.getShowNotification(getReceiverContext())) {
            NotificationBuilderUtil.showIncomingCallNotification(getReceiverContext(), accountID, callID);
        }

        if (!DoorPhoneApplication.IsMainActivityVisible() && SettingsUtil.getStartAppOnIncomingCall(getReceiverContext())) {
            Context context = getReceiverContext();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(context.getString(R.string.param_call_id), callID);
            intent.putExtra(context.getString(R.string.param_player_playing), true);
            intent.putExtra(context.getString(R.string.param_close_after_call), true);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCallState(String accountID, int callID, pjsip_inv_state callStateCode,
                            long connectTimestamp, boolean isLocalHold, boolean isLocalMute) {
        if (callStateCode == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            if (SettingsUtil.getShowNotification(getReceiverContext())) {
                NotificationBuilderUtil.showDeclineCallNotification(getReceiverContext(), accountID, callID);
            }
        }

        if (callStateCode == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            NotificationBuilderUtil.hideIncomingCallNotification(getReceiverContext());
        }
    }
}
