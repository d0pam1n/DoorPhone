package com.d0pam1n.siplib.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.d0pam1n.siplib.CodecPriority;
import com.d0pam1n.siplib.Model.SipAccount;
import com.d0pam1n.siplib.pjsip.PjSipMediaEchoFlags;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Util for writing and reading library settings.
 *
 * @author Andreas Pfister
 */

public class SharedPreferencesUtil {

    private static final String PREFS_NAME =  "SipLib_prefs";
    private static final String PREFS_KEY_ACCOUNTS = "accounts";
    private static final String PREFS_KEY_CODEC_PRIORITIES = "codec_priorities";
    private static final String PREFS_KEY_VIBRATOR_ON = "vibrator_on";
    private static final String PREFS_KEY_RINGTONE_ON = "ringtong_on";
    private static final String PREFS_KEY_RINGTONG_PATH = "ringtone_path";
    private static final String PREFS_ECHO_CANCELLATION_MODE = "echo_cancellation_mode";
    private static final String PREFS_ECHO_CANCELLATION_AGGRESSIVENESS = "echo_cancellation_aggressiveness";
    private static final String PREFS_ECHO_CANCELLATION_USE_NOISE_SUPPRESSOR = "echo_cancellation_use_noise_suppressor";
    private static final String PREFS_ECHO_CANCELLATION_TAIL_LENGTH = "echo_cancellation_tail_length";

    public static void persistConfiguredCodecPriorities(Context context, ArrayList<CodecPriority> codecPriorities) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_CODEC_PRIORITIES, new Gson().toJson(codecPriorities)).apply();
    }

    public static void persistConfiguredAccounts(Context context, List<SipAccount> configuredAccounts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_ACCOUNTS, new Gson().toJson(configuredAccounts)).apply();
    }

    public static ArrayList<SipAccount> loadConfiguredAccounts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String accounts = prefs.getString(PREFS_KEY_ACCOUNTS, "");

        if (accounts.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type listType = new TypeToken<ArrayList<SipAccount>>(){}.getType();
            return new Gson().fromJson(accounts, listType);
        }
    }

    public static ArrayList<CodecPriority> getConfiguredCodecPriorities(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String codecPriorities = prefs.getString(PREFS_KEY_CODEC_PRIORITIES, "");
        if (codecPriorities.isEmpty()) {
            return null;
        }

        Type listType = new TypeToken<ArrayList<CodecPriority>>(){}.getType();
        return new Gson().fromJson(codecPriorities, listType);
    }

    public static void setVibratorOn(Context context, boolean on) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_KEY_VIBRATOR_ON, on).apply();
    }

    public static boolean getUseVibrator(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREFS_KEY_VIBRATOR_ON, true);
    }

    public static void setRingtoneOn(Context context, boolean on) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_KEY_RINGTONE_ON, on).apply();
    }

    public static boolean getRingtoneOn(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREFS_KEY_RINGTONE_ON, true);
    }

    public static void setRingtone(Context context, String path) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_RINGTONG_PATH, path).apply();
    }

    public static String getRingtone(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREFS_KEY_RINGTONG_PATH, "");
    }

    public static void setEchoCancellationMode(Context context, long mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(PREFS_ECHO_CANCELLATION_MODE, mode);
    }

    public static long getEchoCancellationMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(PREFS_ECHO_CANCELLATION_MODE, PjSipMediaEchoFlags.ECHO_WEBRTC);
    }

    public static void setEchoCancellationAggressiveness(Context context, long aggressiveness) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(PREFS_ECHO_CANCELLATION_AGGRESSIVENESS, aggressiveness);
    }

    public static long getEchoCancellationAggressiveness(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(PREFS_ECHO_CANCELLATION_AGGRESSIVENESS, PjSipMediaEchoFlags.ECHO_AGGRESSIVENESS_DEFAULT);
    }

    public static void setEchoCancellationUseNoiseSuppressor(Context context, boolean useNoiseSuppressor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_ECHO_CANCELLATION_USE_NOISE_SUPPRESSOR, useNoiseSuppressor);
    }

    public static boolean getEchoCancellationUseNoiseSuppressor(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREFS_ECHO_CANCELLATION_USE_NOISE_SUPPRESSOR, false);
    }

    public static void setEchoCancellationTailLength(Context context, int tailLength) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(PREFS_ECHO_CANCELLATION_TAIL_LENGTH, tailLength);
    }

    public static int getEchoCancellationTailLength(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREFS_ECHO_CANCELLATION_TAIL_LENGTH, 30);
    }
}
