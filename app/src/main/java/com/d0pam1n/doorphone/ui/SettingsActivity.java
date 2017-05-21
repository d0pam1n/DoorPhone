package com.d0pam1n.doorphone.ui;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.d0pam1n.doorphone.Enums.VideoFormat;
import com.d0pam1n.doorphone.R;
import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.MediaEchoFlags;
import com.d0pam1n.siplib.Model.SipAccount;
import com.d0pam1n.siplib.SipCommands;

import java.util.List;

/**
 * Activity providing all app preferences.
 *
 * @author Andreas Pfister.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            if (preference instanceof RingtonePreference) {
                String ringtoneTitle = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(value.toString())).getTitle(preference.getContext());
                preference.setSummary(ringtoneTitle);
                return true;
            }

            if (preference.getKey().equals(SettingsUtil.SETTING_ECHO_CANCELLATION_AGGRESSIVENESS)) {

                try {
                    switch (MediaEchoFlags.valueOf((String)value)) {
                        case ECHO_AGGRESSIVENESS_DEFAULT:
                            preference.setSummary(preference.getContext().getResources().getStringArray(R.array.settings_echo_cancellation_aggressiveness_entries)[0]);
                            break;
                        case ECHO_AGGRESSIVENESS_CONVERVATIVE:
                            preference.setSummary(preference.getContext().getResources().getStringArray(R.array.settings_echo_cancellation_aggressiveness_entries)[1]);
                            break;
                        case ECHO_AGGRESSIVENESS_MODERATE:
                            preference.setSummary(preference.getContext().getResources().getStringArray(R.array.settings_echo_cancellation_aggressiveness_entries)[2]);
                            break;
                        case ECHO_AGGRESSIVENESS_AGGRESSIV:
                            preference.setSummary(preference.getContext().getResources().getStringArray(R.array.settings_echo_cancellation_aggressiveness_entries)[3]);
                            break;
                        default:
                            break;
                    }
                } catch (IllegalArgumentException exc) {
                    preference.setSummary("");
                }

                return true;
            }

            if (preference.getKey().equals(SettingsUtil.SETTING_VIDEO_FORMAT)) {

                try {
                    int videoFormat = VideoFormat.valueOf((String)value).ordinal();
                    preference.setSummary(preference.getContext().getResources().getStringArray(R.array.settings_video_format_entries)[videoFormat]);
                } catch (IllegalArgumentException exc) {
                    preference.setSummary("");
                }

                return true;
            }

            if (!(preference instanceof CheckBoxPreference)) {
                String stringValue = value.toString();

                preference.setSummary(stringValue);
            }

            return true;
        }
    };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if (!(preference instanceof CheckBoxPreference)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SipPreferenceFragment.class.getName().equals(fragmentName)
                || VideoPreferenceFragment.class.getName().equals(fragmentName)
                || AudioPreferenceFragment.class.getName().equals(fragmentName)
                || CommandsPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationsPreferenceFragment.class.getName().equals(fragmentName)
                || AdvancedPreferenceFragment.class.getName().equals(fragmentName)
                || EchoCancellationPreferenceFragment.class.getName().equals(fragmentName)
                || SipAccountPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SipPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sip);
            setHasOptionsMenu(true);

            findPreference("echo_cancellation_preferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Header header = new Header();
                    header.titleRes = R.string.pref_header_echo_cancellation;
                    header.fragment = "com.d0pam1n.doorphone.ui.SettingsActivity$EchoCancellationPreferenceFragment";
                    ((SettingsActivity)getActivity()).onHeaderClick(header, 0);
                    return true;
                }
            });

            findPreference("sip_account_preferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Header header = new Header();
                    header.titleRes = R.string.pref_header_sip_account;
                    header.fragment = "com.d0pam1n.doorphone.ui.SettingsActivity$SipAccountPreferenceFragment";
                    ((SettingsActivity)getActivity()).onHeaderClick(header, 0);
                    return true;
                }
            });
        }

        @Override
        public void onPause() {


            super.onPause();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdvancedPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_START_SERVICE_ON_BOOT));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_START_APP_ON_INCOMING_CALL));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VideoPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_video);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_VIDEO_PATH));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_VIDEO_FORMAT));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AudioPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_audio);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_USE_RINGTONE));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_USE_VIBRATOR));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_RINGTONE));
        }

        @Override
        public void onPause() {
            SipCommands.setRingtoneOn(getActivity().getApplicationContext(), SettingsUtil.getUseRingtone(getActivity().getApplicationContext()));
            SipCommands.setVibratorOn(getActivity().getApplicationContext(), SettingsUtil.getUseVibrator(getActivity().getApplicationContext()));
            SipCommands.setRingtone(getActivity().getApplicationContext(), SettingsUtil.getRingtone(getActivity().getApplicationContext()));

            super.onPause();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CommandsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_commands);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ADVANCED_UNLOCK_DTMF));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_COMMAND_DOORPI_NUMBER));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notifications);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SHOW_NOTIFICATION));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SipAccountPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sip_account);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_HOST));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_PASSWORD));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_USERNAME));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_PORT));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_REALM));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_SIP_TRANSPORT));
        }

        @Override
        public void onPause() {
            SipAccount account = SettingsUtil.getConfiguredSipAccount(getActivity().getApplicationContext());
            SipCommands.setAccount(getActivity().getApplicationContext(), account);
            BaseActivity.activeAccount = account.getIdUri();

            super.onPause();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class EchoCancellationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sip_echo_cancellation);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ECHO_CANCELLATION_TAIL_LENGTH));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ECHO_CANCELLATION_NOISE_REDUCTION));
            bindPreferenceSummaryToValue(findPreference(SettingsUtil.SETTING_ECHO_CANCELLATION_AGGRESSIVENESS));
        }

        @Override
        public void onPause() {
            SipCommands.setEchoCancellation(getActivity().getApplicationContext(),
                    MediaEchoFlags.ECHO_MODE_WEBRTC,
                    SettingsUtil.getEchoCancellationAggressiveness(getActivity().getApplicationContext()),
                    SettingsUtil.getEchoCancellationTailLength(getActivity().getApplicationContext()),
                    SettingsUtil.getEchoCancellationNoiseReduce(getActivity().getApplicationContext()));

            super.onPause();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
