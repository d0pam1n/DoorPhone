package com.d0pam1n.doorphone.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.d0pam1n.doorphone.Enums.VideoFormat;
import com.d0pam1n.siplib.MediaEchoFlags;
import com.d0pam1n.siplib.Model.SipAccount;

/**
 * Util to getting application preferences.
 *
 * @author Andreas Pfister
 */

public class SettingsUtil {

    public static final String TAG = SettingsUtil.class.getSimpleName();

    public static final String SETTING_SIP_HOST = "setting_sip_host";
    public static final String SETTING_SIP_USERNAME = "setting_sip_username";
    public static final String SETTING_SIP_PASSWORD = "setting_sip_password";
    public static final String SETTING_SIP_PORT = "setting_sip_port";
    public static final String SETTING_SIP_REALM = "setting_sip_realm";
    public static final String SETTING_SIP_TRANSPORT = "setting_sip_transport";
    public static final String SETTING_ECHO_CANCELLATION_TAIL_LENGTH = "setting_echo_cancellation_tail_length";
    public static final String SETTING_ECHO_CANCELLATION_NOISE_REDUCTION = "setting_echo_cancellation_noise_reduction";
    public static final String SETTING_ECHO_CANCELLATION_AGGRESSIVENESS = "setting_echo_cancellation_aggressiveness";

    public static final String SETTING_VIDEO_PATH = "setting_video_path";
    public static final String SETTING_VIDEO_FORMAT = "setting_video_format";

    public static final String SETTING_SHOW_NOTIFICATION = "setting_show_notification";

    public static final String SETTING_COMMAND_DOORPI_NUMBER = "setting_doorpi_number";

    public static final String SETTING_ADVANCED_UNLOCK_DTMF = "setting_unlock_dtmf";
    public static final String SETTING_ADVANCED_START_SERVICE_ON_BOOT = "setting_start_service_on_boot";
    public static final String SETTINGS_ADVANCED_CLOSE_AFTER_CALL = "setting_close_after_call";
    public static final String SETTING_ADVANCED_USE_RINGTONE = "setting_use_ringtone";
    public static final String SETTING_ADVANCED_USE_VIBRATOR = "setting_use_vibrator";
    public static final String SETTING_ADVANCED_RINGTONE = "setting_ringtone";
    public static final String SETTING_ADVANCED_START_APP_ON_INCOMING_CALL = "setting_start_app_on_incoming_call";

    public static SipAccount getConfiguredSipAccount(Context context) {
        SipAccount account = new SipAccount();

        account.setHost(PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_SIP_HOST, ""));
        account.setUsername(PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_SIP_USERNAME, ""));
        account.setPassword(PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_SIP_PASSWORD, ""));
        account.setRealm(PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_SIP_REALM, ""));

        String port = (PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_SIP_PORT, null));

        try {
            account.setPort(Long.parseLong(port));
        } catch (NumberFormatException exc) {
            Log.e(TAG, "Wrong SIPPort: " + port, exc);
            account.setPort(5060);
        }
        return account;
    }

    public static String getVideoPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_VIDEO_PATH, "");
    }

    public static VideoFormat getVideoFormat(Context context) {
        String videoFormat = PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_VIDEO_FORMAT, VideoFormat.SURFACE_BEST_FIT.name());

        VideoFormat format = VideoFormat.SURFACE_BEST_FIT;

        try {
            format = VideoFormat.valueOf(videoFormat);
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "Video format could not be converted. Using default value.");
        }

        return format;
    }

    public static String getDTMFUnlockCode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_ADVANCED_UNLOCK_DTMF, "");
    }

    public static boolean getIsServiceAutostart(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ADVANCED_START_SERVICE_ON_BOOT, false);
    }

    public static boolean getUseRingtone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ADVANCED_USE_RINGTONE, true);
    }

    public static boolean getUseVibrator(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ADVANCED_USE_VIBRATOR, true);
    }

    public static String getRingtone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_ADVANCED_RINGTONE, "");
    }

    public static int getEchoCancellationTailLength(Context context) {
        String tailLength = PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_ECHO_CANCELLATION_TAIL_LENGTH, "30");

        int retVal = 30;

        try {
            retVal = Integer.parseInt(tailLength);
        } catch (NumberFormatException exc) {
            Log.e(TAG, "TailLength could not be converted to integer");
        }

        return retVal;
    }

    public static MediaEchoFlags getEchoCancellationAggressiveness(Context context) {
        String aggressiveness = PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_ECHO_CANCELLATION_AGGRESSIVENESS, MediaEchoFlags.ECHO_AGGRESSIVENESS_DEFAULT.name());

        MediaEchoFlags echoFlag = MediaEchoFlags.ECHO_AGGRESSIVENESS_DEFAULT;

        try {
            echoFlag = MediaEchoFlags.valueOf(aggressiveness);
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "MediaEcho could not be converted. Using default value.");
        }

        return echoFlag;
    }

    public static boolean getEchoCancellationNoiseReduce(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ECHO_CANCELLATION_NOISE_REDUCTION, false);
    }

    public static boolean getCloseAfterCall(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_ADVANCED_CLOSE_AFTER_CALL, false);
    }

    public static boolean getShowNotification(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_SHOW_NOTIFICATION, false);
    }

    public static boolean getStartAppOnIncomingCall(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ADVANCED_START_APP_ON_INCOMING_CALL, true);
    }

    public static String getDoorPiNumber(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTING_COMMAND_DOORPI_NUMBER, "");
    }
}
