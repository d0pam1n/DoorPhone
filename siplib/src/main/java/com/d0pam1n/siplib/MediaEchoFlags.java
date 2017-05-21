package com.d0pam1n.siplib;

/**
 * All available options for echo cancellation and noise suppression
 * provided by pjsip.
 *
 * @author Andreas Pfister
 */

public enum MediaEchoFlags {
    ECHO_MODE_WEBRTC,
    ECHO_USE_NOISE_SUPPRESSOR,
    ECHO_AGGRESSIVENESS_DEFAULT,
    ECHO_AGGRESSIVENESS_CONVERVATIVE,
    ECHO_AGGRESSIVENESS_MODERATE,
    ECHO_AGGRESSIVENESS_AGGRESSIV
}
