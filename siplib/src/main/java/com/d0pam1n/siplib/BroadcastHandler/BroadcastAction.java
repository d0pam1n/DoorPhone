package com.d0pam1n.siplib.BroadcastHandler;

/**
 * All available broadcast actions.
 *
 * @author Andreas Pfister
 */

public enum BroadcastAction {
    REGISTRATION,
    INCOMING_CALL,
    CALL_STATE,
    OUTGOING_CALL,
    STACK_STATUS,
    CODEC_PRIORITIES,
    CODEC_PRIORITIES_SET_STATUS
}
