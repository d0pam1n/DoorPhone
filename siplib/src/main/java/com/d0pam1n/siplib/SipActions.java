package com.d0pam1n.siplib;

/**
 * All available library commands and parameters.
 *
 * @author Andreas Pfister
 */

public class SipActions {

    protected static final String ACTION_SET_ACCOUNT = "setAccount";
    protected static final String ACTION_REMOVE_ACCOUNT = "removeAccount";
    protected static final String ACTION_MAKE_CALL = "makeCall";
    protected static final String ACTION_HANG_UP_CALL = "hangUpCall";
    protected static final String ACTION_HANG_UP_CALLS = "hangUpCalls";
    protected static final String ACTION_HOLD_CALLS = "holdCalls";
    protected static final String ACTION_GET_CALL_STATUS = "getCallStatus";
    protected static final String ACTION_SEND_DTMF = "sendDtmf";
    protected static final String ACTION_ACCEPT_INCOMING_CALL = "acceptIncomingCall";
    protected static final String ACTION_DECLINE_INCOMING_CALL = "declineIncomingCall";
    protected static final String ACTION_SET_HOLD = "callSetHold";
    protected static final String ACTION_SET_MUTE = "callSetMute";
    protected static final String ACTION_TOGGLE_HOLD = "callToggleHold";
    protected static final String ACTION_TOGGLE_MUTE = "callToggleMute";
    protected static final String ACTION_TRANSFER_CALL = "callTransfer";
    protected static final String ACTION_GET_CODEC_PRIORITIES = "codecPriorities";
    protected static final String ACTION_SET_CODEC_PRIORITIES = "setCodecPriorities";
    protected static final String ACTION_GET_REGISTRATION_STATUS = "getRegistrationStatus";
    protected static final String ACTION_REREGISTER_ACCOUNT = "reRegisterAccount";
    protected static final String ACTION_SET_RINGTONE_ON = "setRingtoneOn";
    protected static final String ACTION_SET_VIBRATOR_ON = "setVibratorOn";
    protected static final String ACTION_SET_RINGTONE = "setRingTone";
    protected static final String ACTION_SET_ECHO_CANCELLATION_SETTINGS = "setEchoCancellationSettings";

    protected static final String PARAM_ACCOUNT_DATA = "accountData";
    protected static final String PARAM_ACCOUNT_ID = "accountID";
    protected static final String PARAM_NUMBER = "number";
    protected static final String PARAM_CALL_ID = "callId";
    protected static final String PARAM_DTMF = "dtmf";
    protected static final String PARAM_HOLD = "hold";
    protected static final String PARAM_MUTE = "mute";
    protected static final String PARAM_CODEC_PRIORITIES = "codecPriorities";
    protected static final String PARAM_RINGTONE_ON = "ringtoneOn";
    protected static final String PARAM_VIBRATOR_ON = "vibratorOn";
    protected static final String PARAM_RINGTONE = "ringtone";
    protected static final String PARAM_ECHO_CANCELLATION_MODE = "echoMode";
    protected static final String PARAM_ECHO_CANCELLATION_TAIL_LENGTH = "tailLength";
    protected static final String PARAM_ECHO_CANCELLATION_AGGRESSIVENESS = "aggressiveness";
    protected static final String PARAM_ECHO_CANCELLATION_NOISE_REDUCTION = "noiseReduction";
}
