package com.d0pam1n.siplib;

import android.content.Context;
import android.content.Intent;

import com.d0pam1n.siplib.Model.SipAccount;

import java.util.ArrayList;

import static com.d0pam1n.siplib.SipActions.ACTION_ACCEPT_INCOMING_CALL;
import static com.d0pam1n.siplib.SipActions.ACTION_GET_CALL_STATUS;
import static com.d0pam1n.siplib.SipActions.ACTION_GET_CODEC_PRIORITIES;
import static com.d0pam1n.siplib.SipActions.ACTION_GET_REGISTRATION_STATUS;
import static com.d0pam1n.siplib.SipActions.ACTION_HANG_UP_CALL;
import static com.d0pam1n.siplib.SipActions.ACTION_HANG_UP_CALLS;
import static com.d0pam1n.siplib.SipActions.ACTION_MAKE_CALL;
import static com.d0pam1n.siplib.SipActions.ACTION_REMOVE_ACCOUNT;
import static com.d0pam1n.siplib.SipActions.ACTION_REREGISTER_ACCOUNT;
import static com.d0pam1n.siplib.SipActions.ACTION_SEND_DTMF;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_ACCOUNT;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_CODEC_PRIORITIES;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_ECHO_CANCELLATION_SETTINGS;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_RINGTONE;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_RINGTONE_ON;
import static com.d0pam1n.siplib.SipActions.ACTION_SET_VIBRATOR_ON;
import static com.d0pam1n.siplib.SipActions.PARAM_ACCOUNT_DATA;
import static com.d0pam1n.siplib.SipActions.PARAM_ACCOUNT_ID;
import static com.d0pam1n.siplib.SipActions.PARAM_CALL_ID;
import static com.d0pam1n.siplib.SipActions.PARAM_CODEC_PRIORITIES;
import static com.d0pam1n.siplib.SipActions.PARAM_DTMF;
import static com.d0pam1n.siplib.SipActions.PARAM_ECHO_CANCELLATION_AGGRESSIVENESS;
import static com.d0pam1n.siplib.SipActions.PARAM_ECHO_CANCELLATION_MODE;
import static com.d0pam1n.siplib.SipActions.PARAM_ECHO_CANCELLATION_NOISE_REDUCTION;
import static com.d0pam1n.siplib.SipActions.PARAM_ECHO_CANCELLATION_TAIL_LENGTH;
import static com.d0pam1n.siplib.SipActions.PARAM_NUMBER;
import static com.d0pam1n.siplib.SipActions.PARAM_RINGTONE;
import static com.d0pam1n.siplib.SipActions.PARAM_RINGTONE_ON;
import static com.d0pam1n.siplib.SipActions.PARAM_VIBRATOR_ON;

/**
 * Created by d0pam1n on 05.03.17.
 */

public class SipCommands {
    /**
     * Adds a new SIP account.
     *
     * @param context The application context.
     * @param account The SIP account data.
     * @return The SIP account ID uri.
     */
    public static String setAccount(Context context, SipAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account MUST not null");
        }

        String accountID = account.getIdUri();

        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_ACCOUNT);
        intent.putExtra(PARAM_ACCOUNT_DATA, account);
        context.startService(intent);

        return accountID;
    }

    /**
     * Removes a SIP account.
     *
     * @param context   The application context.
     * @param accountID The account ID uri.
     */
    public static void removeAccount(Context context, String accountID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_REMOVE_ACCOUNT);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        context.startService(intent);
    }

    /**
     * Starts the SIP service.
     *
     * @param context The application context.
     */
    public static void startService(Context context) {
        context.startService(new Intent(context, SipService.class));
    }

    /**
     * Stops the SIP service.
     *
     * @param context
     */
    public static void stopService(Context context) {
        context.stopService(new Intent(context, SipService.class));
    }

    public static void makeCall(Context context, String accountID, String number) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_MAKE_CALL);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        intent.putExtra(PARAM_NUMBER, number);
        context.startService(intent);
    }

    /**
     * Accept an incoming call.
     *
     * @param context   The application context.
     * @param accountID The account ID uri.
     * @param callID    The call ID.
     */
    public static void acceptIncomingCall(Context context, String accountID, int callID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_ACCEPT_INCOMING_CALL);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        intent.putExtra(PARAM_CALL_ID, callID);
        context.startService(intent);
    }

    /**
     * Sends DTMF.
     *
     * @param context    The application context.
     * @param accountID  The account ID uri.
     * @param callID     The call ID.
     * @param dtmfDigits The DTMF digits to send.
     */
    public static void sendDTMF(Context context, String accountID, int callID, String dtmfDigits) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SEND_DTMF);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        intent.putExtra(PARAM_CALL_ID, callID);
        intent.putExtra(PARAM_DTMF, dtmfDigits);
        context.startService(intent);
    }

    /**
     * Hangs up an active call.
     *
     * @param context   The application context.
     * @param accountID The account ID uri.
     * @param callID    The call ID to hang up.
     */
    public static void hangUpCall(Context context, String accountID, int callID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_HANG_UP_CALL);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        intent.putExtra(PARAM_CALL_ID, callID);
        context.startService(intent);
    }

    /**
     * Hangs up all active calls of account.
     *
     * @param context   The application context
     * @param accountID The account ID uri.
     */
    public static void hangUpActiveCalls(Context context, String accountID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_HANG_UP_CALLS);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        context.startService(intent);
    }

    /**
     * Gets the codec priorities.
     *
     * @param context The application context.
     */
    public static void getCodecPriorities(Context context) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_GET_CODEC_PRIORITIES);
        context.startService(intent);
    }

    public static void setCodecPriorities(Context context, ArrayList<CodecPriority> codecPriorities) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_CODEC_PRIORITIES);
        intent.putParcelableArrayListExtra(PARAM_CODEC_PRIORITIES, codecPriorities);
        context.startService(intent);
    }

    /**
     * Gets the registration status for an account.
     *
     * @param context   The application context.
     * @param accountID The sip account ID uri.
     */
    public static void getRegistrationStatus(Context context, String accountID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_GET_REGISTRATION_STATUS);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        context.startService(intent);
    }

    /**
     * Reregisters the account.
     *
     * @param context   The application context.
     * @param accountID The sip account ID uri.
     */
    public static void reRegisterAccount(Context context, String accountID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_REREGISTER_ACCOUNT);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        context.startService(intent);
    }

    public static void setRingtoneOn(Context context, boolean on) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_RINGTONE_ON);
        intent.putExtra(PARAM_RINGTONE_ON, on);
        context.startService(intent);
    }

    public static void setVibratorOn(Context context, boolean on) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_VIBRATOR_ON);
        intent.putExtra(PARAM_VIBRATOR_ON, on);
        context.startService(intent);
    }

    public static void setRingtone(Context context, String path) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_RINGTONE);
        intent.putExtra(PARAM_RINGTONE, path);
        context.startService(intent);
    }

    public static void setEchoCancellation(Context context, MediaEchoFlags mode, MediaEchoFlags aggressiveness, int tailLength, boolean useNoiseSuppressor) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_SET_ECHO_CANCELLATION_SETTINGS);
        intent.putExtra(PARAM_ECHO_CANCELLATION_MODE, mode);
        intent.putExtra(PARAM_ECHO_CANCELLATION_AGGRESSIVENESS, aggressiveness);
        intent.putExtra(PARAM_ECHO_CANCELLATION_TAIL_LENGTH, tailLength);
        intent.putExtra(PARAM_ECHO_CANCELLATION_NOISE_REDUCTION, useNoiseSuppressor);
        context.startService(intent);
    }

    public static void getCallStatus(Context context, String accountID, int callID) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(ACTION_GET_CALL_STATUS);
        intent.putExtra(PARAM_ACCOUNT_ID, accountID);
        intent.putExtra(PARAM_CALL_ID, callID);
        context.startService(intent);
    }
}
