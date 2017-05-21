package com.d0pam1n.siplib.pjsip;

/**
 * Available options for echo cancellation and noise suppression,
 * provided by pjsip.
 *
 * @author Andreas Pfister
 */

public final class PjSipMediaEchoFlags {
    public static long ECHO_WEBRTC = 3,
            ECHO_USE_NOISE_SUPPRESSOR = 128,
            ECHO_AGGRESSIVENESS_DEFAULT = 0,
            ECHO_AGGRESSIVENESS_CONVERVATIVE = 0x100,
            ECHO_AGGRESSIVENESS_MODERATE = 0x200,
            ECHO_AGGRESSIVENESS_AGGRESSIV = 0x300;
}
