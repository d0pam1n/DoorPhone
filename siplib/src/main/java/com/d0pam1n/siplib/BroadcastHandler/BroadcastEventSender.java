package com.d0pam1n.siplib.BroadcastHandler;

import android.content.Context;
import android.content.Intent;

import com.d0pam1n.siplib.CodecPriority;

import java.util.ArrayList;

/**
 * Sender for events of the library.
 *
 * @author Andreas Pfister
 */

public class BroadcastEventSender {

    public static final String NAMESPACE = "com.d0pamin.siplib";

    private Context mContext;

    public BroadcastEventSender(Context context) {
        mContext = context;
    }

    /**
     * Gets the full qualified action string.
     * @param action The broadcast action.
     * @return Full qualified action string.
     */
    public static String getAction(BroadcastAction action) {
        return NAMESPACE + "." + action;
    }

    /**
     * Sends an incoming call broadcast intent
     * @param accountID The account ID uri.
     * @param callID The call ID.
     * @param remoteUri The ID uri of the caller.
     */
    public void incomingCall(String accountID, int callID, String remoteUri) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.INCOMING_CALL));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.REMOTE_URI, remoteUri);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends an outgoing call broadcast intent.
     * @param accountID The account ID uri.
     * @param callID The call ID.
     * @param number The number get called to.
     */
    public void outgoingCall(String accountID, int callID, String number) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.OUTGOING_CALL));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.NUMBER, number);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends a registration state broadcast intent.
     * @param accountID The account ID uri.
     * @param registrationStateCode The SIP registration code.
     */
    public void registrationState(String accountID, int registrationStateCode) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.REGISTRATION));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CODE, registrationStateCode);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends a call state broadcast intent.
     * @param accountID The account ID uri.
     * @param callID THe call ID.
     * @param callStateCode The SIP call state code.
     * @param connectTimestamp The call start timestamp.
     * @param isLocalHold true if the call is held locally.
     * @param isLocalMute true if the call is muted locally.
     */
    public void callState(String accountID, int callID, int callStateCode, long connectTimestamp,
                          boolean isLocalHold, boolean isLocalMute) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CALL_STATE));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.CALL_STATE, callStateCode);
        intent.putExtra(BroadcastParameters.CONNECT_TIMESTAMP, connectTimestamp);
        intent.putExtra(BroadcastParameters.LOCAL_HOLD, isLocalHold);
        intent.putExtra(BroadcastParameters.LOCAL_MUTE, isLocalMute);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends a stack status broadcast intent.
     * @param started true if stack is started.
     */
    public void stackStatus(boolean started) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.STACK_STATUS));
        intent.putExtra(BroadcastParameters.STACK_STARTED, started);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends a list of codec priorities broadcast intent.
     * @param codecPriorities List of codec priorities.
     */
    public void codecPriorities(ArrayList<CodecPriority> codecPriorities) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES));
        intent.putParcelableArrayListExtra(BroadcastParameters.CODEC_PRIORITIES_LIST, codecPriorities);

        mContext.sendBroadcast(intent);
    }

    /**
     * Sends a codec priorities status changed broadcast intent.
     * @param success true if codec priorities set successfully.
     */
    public void codecPrioritiesSetStatus(boolean success) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES_SET_STATUS));
        intent.putExtra(BroadcastParameters.SUCCESS, success);

        mContext.sendBroadcast(intent);
    }
}
