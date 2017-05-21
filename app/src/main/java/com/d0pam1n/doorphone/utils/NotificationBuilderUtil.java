package com.d0pam1n.doorphone.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.d0pam1n.doorphone.R;
import com.d0pam1n.doorphone.ui.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Util for building notifications, e.g. on incoming call.
 *
 * @author Andreas Pfister
 */

public class NotificationBuilderUtil {

    private static int mNotifID = 4711;
    private static NotificationManager mNotificationManager;

    public static void showIncomingCallNotification(Context context, String accountID, int callID) {

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra(context.getString(R.string.param_call_id), callID);

        Intent actionIntent = new Intent(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action));
        actionIntent.putExtra(context.getString(R.string.param_account_id), accountID);
        actionIntent.putExtra(context.getString(R.string.param_call_id), callID);

        PendingIntent pActivityIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), activityIntent, 0);
        PendingIntent pActionIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), actionIntent, 0);

        NotificationCompat.Action acceptCallAction = new NotificationCompat.Action.Builder(R.drawable.ic_call, context.getString(R.string.notif_incoming_call_action_accept), pActionIntent).build();

        Notification n  = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notif_incoming_call_title))
                .setContentText(context.getString(R.string.notif_incoming_call_text))
                .setSmallIcon(R.drawable.ic_call)
                .setContentIntent(pActivityIntent)
                .setAutoCancel(true)
                .addAction(acceptCallAction)
                .build();

        mNotificationManager.notify(mNotifID, n);
    }

    public static void showDeclineCallNotification(Context context, String accountID, int callID) {
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra(context.getString(R.string.param_call_id), callID);

        Intent hangupIntent = new Intent(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action_hangup));
        hangupIntent.putExtra(context.getString(R.string.param_account_id), accountID);
        hangupIntent.putExtra(context.getString(R.string.param_call_id), callID);

        Intent dtmfIntent = new Intent(context.getString(R.string.package_name) + "." + context.getString(R.string.notif_incoming_call_intent_action_open_door));
        dtmfIntent.putExtra(context.getString(R.string.param_account_id), accountID);
        dtmfIntent.putExtra(context.getString(R.string.param_call_id), callID);
        dtmfIntent.putExtra(context.getString(R.string.param_dtmf_open_door), SettingsUtil.getDTMFUnlockCode(context));


        PendingIntent pActivityIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), activityIntent, 0);
        PendingIntent pHangupIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), hangupIntent, 0);
        PendingIntent pDtmfIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), dtmfIntent, 0);

        NotificationCompat.Action hangupCallAction = new NotificationCompat.Action.Builder(R.drawable.ic_call_end, context.getString(R.string.notif_incoming_call_action_hangup), pHangupIntent).build();
        NotificationCompat.Action openDoorAction = new NotificationCompat.Action.Builder(R.drawable.ic_lock_open, context.getString(R.string.notif_incoming_call_action_open_door), pDtmfIntent).build();

        Notification n  = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notif_incoming_call_activ_title))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_call)
                .setContentIntent(pActivityIntent)
                .setAutoCancel(true)
                .addAction(hangupCallAction)
                .addAction(openDoorAction)
                .build();

        mNotificationManager.notify(mNotifID, n);
    }

    public static void hideIncomingCallNotification(Context context) {

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNotifID);
    }
}
