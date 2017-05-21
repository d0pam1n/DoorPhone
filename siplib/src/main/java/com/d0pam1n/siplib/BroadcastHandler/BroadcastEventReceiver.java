package com.d0pam1n.siplib.BroadcastHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.d0pam1n.siplib.CodecPriority;

import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;

import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.CALL_STATE;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.CODEC_PRIORITIES;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.CODEC_PRIORITIES_SET_STATUS;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.INCOMING_CALL;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.OUTGOING_CALL;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.REGISTRATION;
import static com.d0pam1n.siplib.BroadcastHandler.BroadcastAction.STACK_STATUS;

/**
 * Receiver for sip events emitted by the library.
 *
 * @author Andreas Pfister
 */

public class BroadcastEventReceiver extends BroadcastReceiver {

    private static final String TAG = BroadcastEventReceiver.class.getSimpleName();

    private Context mContext;
    private Intent mIntent;

    @Override
    public void onReceive(Context context, Intent intent){
        if (intent == null) {
            return;
        }

        mContext = context.getApplicationContext();
        mIntent = intent;

        String action = intent.getAction();

        if (action.equals(BroadcastEventSender.getAction(REGISTRATION))) {
            int stateCode = intent.getIntExtra(BroadcastParameters.CODE, -1);
            onRegistration(intent.getStringExtra(BroadcastParameters.ACCOUNT_ID),
                    pjsip_status_code.swigToEnum(stateCode));

        } else if (action.equals(BroadcastEventSender.getAction(INCOMING_CALL))) {
            onIncomingCall(intent.getStringExtra(BroadcastParameters.ACCOUNT_ID),
                    intent.getIntExtra(BroadcastParameters.CALL_ID, -1),
                    intent.getStringExtra(BroadcastParameters.REMOTE_URI));

        } else if (action.equals(BroadcastEventSender.getAction(OUTGOING_CALL))) {
            onOutgoingCall(intent.getStringExtra(BroadcastParameters.ACCOUNT_ID),
                    intent.getIntExtra(BroadcastParameters.CALL_ID, -1),
                    intent.getStringExtra(BroadcastParameters.NUMBER));

        } else if (action.equals(BroadcastEventSender.getAction(CALL_STATE))) {
            int callState = intent.getIntExtra(BroadcastParameters.CALL_STATE, -1);
            onCallState(intent.getStringExtra(BroadcastParameters.ACCOUNT_ID),
                    intent.getIntExtra(BroadcastParameters.CALL_ID, -1),
                    pjsip_inv_state.swigToEnum(callState),
                    intent.getLongExtra(BroadcastParameters.CONNECT_TIMESTAMP, -1),
                    intent.getBooleanExtra(BroadcastParameters.LOCAL_HOLD, false),
                    intent.getBooleanExtra(BroadcastParameters.LOCAL_MUTE, false));

        } else if (action.equals(BroadcastEventSender.getAction(STACK_STATUS))) {
            onStackStatus(intent.getBooleanExtra(BroadcastParameters.STACK_STARTED, false));

        } else if (action.equals(BroadcastEventSender.getAction(CODEC_PRIORITIES))) {
            ArrayList<CodecPriority> codecList = intent.getParcelableArrayListExtra(BroadcastParameters.CODEC_PRIORITIES_LIST);
            onReceivedCodecPriorities(codecList);

        } else if (action.equals(BroadcastEventSender.getAction(CODEC_PRIORITIES_SET_STATUS))) {
            onCodecPrioritiesSetStatus(intent.getBooleanExtra(BroadcastParameters.SUCCESS, false));

        }
    }

    /**
     * Register this broadcast receiver.
     * It's recommended to register the receiver in Activity's onResume method.
     *
     * @param context context in which to register this receiver
     */
    public void register(final Context context) {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastEventSender.getAction(REGISTRATION));
        intentFilter.addAction(BroadcastEventSender.getAction(INCOMING_CALL));
        intentFilter.addAction(BroadcastEventSender.getAction(CALL_STATE));
        intentFilter.addAction(BroadcastEventSender.getAction(OUTGOING_CALL));
        intentFilter.addAction(BroadcastEventSender.getAction(STACK_STATUS));
        intentFilter.addAction(BroadcastEventSender.getAction(CODEC_PRIORITIES));
        intentFilter.addAction(BroadcastEventSender.getAction(CODEC_PRIORITIES_SET_STATUS));
        context.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister this broadcast receiver.
     * It's recommended to unregister the receiver in Activity's onPause method.
     *
     * @param context context in which to unregister this receiver
     */
    public void unregister(final Context context) {
        context.unregisterReceiver(this);
    }


    public void onRegistration(String accountID, pjsip_status_code registrationStateCode) {
        Log.d(TAG, "onRegistration - accountID: " + accountID +
                ", registrationStateCode: " + registrationStateCode);
    }

    public void onIncomingCall(String accountID, int callID, String remoteUri) {
        Log.d(TAG, "onIncomingCall - accountID: " + accountID +
                ", callID: " + callID +
                ", remoteUri: " + remoteUri);
    }

    public void onOutgoingCall(String accountID, int callID, String number) {
        Log.d(TAG, "onOutgoingCall - accoundID: " + accountID +
                ", callID: " + callID +
                ", number: " + number);
    }

    public void onCallState(String accountID, int callID, pjsip_inv_state callStateCode,
                            long connectTimestamp, boolean isLocalHold, boolean isLocalMute) {
        Log.d(TAG, "onCallState - accountID: " + accountID +
                ", callID: " + callID +
                ", callStateCode: " + callStateCode +
                ", connectTimestamp: " + connectTimestamp +
                ", isLocalHold: " + isLocalHold +
                ", isLocalMute: " + isLocalMute);
    }

    public void onStackStatus(boolean started) {
        Log.d(TAG, "SIP service stack " + (started ? "started" : "stopped"));
    }

    public void onReceivedCodecPriorities(ArrayList<CodecPriority> codecPriorities) {
        Log.d(TAG, "Received codec priorities");
        for (CodecPriority codec : codecPriorities) {
            Log.d(TAG, codec.toString());
        }
    }

    public void onCodecPrioritiesSetStatus(boolean success) {
        Log.d(TAG, "Codec priorities " + (success ? "successfully set" : "set error"));
    }

    /**
     * Gets the receiver context.
     */
    protected Context getReceiverContext() {
        return mContext;
    }

    protected Intent getReceiverIntent() {
        return mIntent;
    }
}
