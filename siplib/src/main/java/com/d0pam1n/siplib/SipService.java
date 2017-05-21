package com.d0pam1n.siplib;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.d0pam1n.siplib.BroadcastHandler.BroadcastEventReceiver;
import com.d0pam1n.siplib.BroadcastHandler.BroadcastEventSender;
import com.d0pam1n.siplib.Model.SipAccount;
import com.d0pam1n.siplib.Utils.SharedPreferencesUtil;
import com.d0pam1n.siplib.pjsip.PjSipAccount;
import com.d0pam1n.siplib.pjsip.PjSipCall;
import com.d0pam1n.siplib.pjsip.PjSipMediaEchoFlags;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 * The core background service handling communication to the sip server and
 * executing commands sent to the library.
 *
 * @author Andreas Pfister
 */

public class SipService extends Service {

    private static final String TAG = SipService.class.getSimpleName();
    private static final long[] VIBRATOR_PATTERN = {0, 1000, 1000};
    private static final String START_LINPHONE_LOGS = " ==== Phone information dump ====";
    private static final String AGENT_NAME = "DoorPhone"; // TODO make accessible

    private static ConcurrentHashMap<String, PjSipAccount> sActiveSipAccounts = new ConcurrentHashMap<>();

    private List<SipAccount> mConfiguredAccounts = new ArrayList<>();
    private MediaPlayer mRingTone;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private Uri mRingtoneUri;
    private BroadcastEventSender mBroadcastEventSender;
    private volatile boolean mStarted;
    private String mNotificationTitle;
    private Endpoint mEndpoint;

    private long mEchoMode;
    private long mEchoAggressiveness;
    private int mEchoTailLength;
    private boolean mUseNoiseSuppressor;

    private BroadcastEventReceiver eventReceiver = new BroadcastEventReceiver();


    public SipService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationTitle = getString(R.string.service_name);

        // Dump some debugging information to the logs
        Log.i(TAG, START_LINPHONE_LOGS);

        dumpDeviceInformation();
        dumpInstalledSipSerivceInformation();

        loadNativeLibraries();

        if (SharedPreferencesUtil.getRingtone(this) == "") {
            mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(SipService.this, RingtoneManager.TYPE_RINGTONE);
        } else {
            mRingtoneUri = Uri.parse(SharedPreferencesUtil.getRingtone(this));
        }

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mEchoMode = SharedPreferencesUtil.getEchoCancellationMode(this);
        mEchoAggressiveness = SharedPreferencesUtil.getEchoCancellationAggressiveness(this);
        mEchoTailLength = SharedPreferencesUtil.getEchoCancellationTailLength(this);
        mUseNoiseSuppressor = SharedPreferencesUtil.getEchoCancellationUseNoiseSuppressor(this);

        mBroadcastEventSender = new BroadcastEventSender(SipService.this);

        mConfiguredAccounts = SharedPreferencesUtil.loadConfiguredAccounts(this);
        addAllConfiguredAccounts();

        eventReceiver.register(this);

        Log.d(TAG, "SipService started!");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {

            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case ACTION_SET_ACCOUNT:
                        setAccount(intent);
                        break;
                    case ACTION_REMOVE_ACCOUNT:
                        removeAccount(intent);
                        break;
                    case ACTION_MAKE_CALL:
                        makeCall(intent);
                        break;
                    case ACTION_HANG_UP_CALL:
                        hangUpCall(intent);
                        break;
                    case ACTION_HANG_UP_CALLS:
                        hangUpActiveCalls(intent);
                        break;
                    case ACTION_SEND_DTMF:
                        sendDTMF(intent);
                        break;
                    case ACTION_ACCEPT_INCOMING_CALL:
                        acceptIncomingCall(intent);
                        break;
                    case ACTION_GET_CODEC_PRIORITIES:
                        getCodecPriorities();
                        break;
                    case ACTION_SET_CODEC_PRIORITIES:
                        setCodecPriorities(intent);
                        break;
                    case ACTION_GET_REGISTRATION_STATUS:
                        getRegistrationStatus(intent);
                        break;
                    case ACTION_REREGISTER_ACCOUNT:
                        reRegisterAccount(intent);
                        break;
                    case ACTION_SET_RINGTONE_ON:
                        setRingToneOn(intent);
                        break;
                    case ACTION_SET_VIBRATOR_ON:
                        setVibratorOn(intent);
                        break;
                    case ACTION_SET_RINGTONE:
                        setRingtone(intent);
                        break;
                    case ACTION_SET_ECHO_CANCELLATION_SETTINGS:
                        setEchoCancellationSettings(intent);
                        break;
                    case ACTION_GET_CALL_STATUS:
                        getCallStatus(intent);
                        break;
                    default:
                        break;
                }
            }


            if (mConfiguredAccounts.isEmpty()) {
                // No more configured accounts. Service does not need to run.
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying SipService");
        stopStack();
        super.onDestroy();
    }

    /**
     * Starts ringtone.
     */
    public synchronized void startRingtone() {
        if (SharedPreferencesUtil.getUseVibrator(this)) {
            mVibrator.vibrate(VIBRATOR_PATTERN, 0);
        }

        if (SharedPreferencesUtil.getRingtoneOn(this)) {
            try {
                mRingTone = MediaPlayer.create(this, mRingtoneUri);
                mRingTone.setLooping(true);

                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
                mRingTone.setVolume(volume, volume);

                mRingTone.start();
            } catch (Exception exc) {
                Log.e(TAG, "Error while trying to play ringtone!", exc);
            }
        }
    }

    /**
     * Stops ringtone.
     */
    public synchronized void stopRingtone() {
        mVibrator.cancel();

        if (mRingTone != null) {
            try {
                if (mRingTone.isPlaying())
                    mRingTone.stop();
            } catch (Exception ignored) { }

            try {
                mRingTone.reset();
                mRingTone.release();
            } catch (Exception ignored) { }
        }
    }

    /**
     * Gets the audio device manager of the sip stack endpoint.
     * @return The audio device manager of the current sip stack endpoint.
     */
    public synchronized AudDevManager getAudDevManager() {
        return mEndpoint.audDevManager();
    }

    /**
     * Gets the {@link BroadcastEventSender}.
     * @return Instance of {@link BroadcastEventSender}.
     */
    public BroadcastEventSender getBroadcastEventSender() {
        return mBroadcastEventSender;
    }

    /**
     * Logs information about the device.
     */
    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        //MANUFACTURER doesn't exist in android 1.5.
        //sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        Log.i(TAG, sb.toString());
    }

    /**
     * Logs information about the version of SipService.
     */
    private void dumpInstalledSipSerivceInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException nnfe) {}

        if (info != null) {
            Log.i(TAG, "SipService version is " + info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i(TAG, "SipService version is unknown");
        }
    }

    /**
     * Loads the native PJSIP libraries.
     */
    private void loadNativeLibraries() {
        try {
            System.loadLibrary("pjsua2");
            Log.d(TAG, "PJSIP pjsua2 loaded");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "Error while loading PJSIP pjsua2 native library", error);
            throw new RuntimeException(error);
        }
    }

    /**
     * Starts the PJSIP stack.
     */
    private void startStack() {

        if (mStarted) return;

        try {
            Log.d(TAG, "Starting PJSIP stack");
            mEndpoint = new Endpoint();
            mEndpoint.libCreate();
            EpConfig epConfig = new EpConfig();
            epConfig.getUaConfig().setUserAgent(AGENT_NAME);
            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(8000);
            epConfig.getMedConfig().setQuality(10);
            epConfig.getMedConfig().setSndPlayLatency(0);
            epConfig.getMedConfig().setSndRecLatency(0);
            epConfig.getMedConfig().setNoVad(true);

            epConfig.getMedConfig().setEcTailLen(mEchoTailLength);
            epConfig.getMedConfig().setThreadCnt(2);
            epConfig.getMedConfig().setEcOptions(getEchoCancellationOptions());

            mEndpoint.libInit(epConfig);
            TransportConfig udpTransport = new TransportConfig();
            udpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            TransportConfig tcpTransport = new TransportConfig();
            tcpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport);
            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, tcpTransport);
            mEndpoint.libStart();
            ArrayList<CodecPriority> codecPriorities = SharedPreferencesUtil.getConfiguredCodecPriorities(this);
            if (codecPriorities != null) {
                for (CodecPriority codecPriority : codecPriorities) {
                    mEndpoint.codecSetPriority(codecPriority.getCodecId(), (short) codecPriority.getPriority());
                }
            } else {
                mEndpoint.codecSetPriority("PCMA/8000", (short) (CodecPriority.PRIORITY_MAX - 1));
                mEndpoint.codecSetPriority("PCMU/8000", (short) (CodecPriority.PRIORITY_MAX - 2));
                //mEndpoint.codecSetPriority("G729/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/16000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/32000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("GSM/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("G722/16000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("G7221/16000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("G7221/32000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("ilbc/8000", (short) CodecPriority.PRIORITY_DISABLED);
            }

            Log.d(TAG, "PJSIP stack started");
            mStarted = true;
            mBroadcastEventSender.stackStatus(true);

        } catch (Exception exc) {
            Log.e(TAG, "Error while starting PJSIP", exc);
            mStarted = false;
            mBroadcastEventSender.stackStatus(false);
        }
    }

    /**
     * Shuts down PJSIP Stack
     * @throws Exception if an error occurs while trying to shut down the stack
     */
    private void stopStack() {

        if (!mStarted) return;

        try {
            Log.d(TAG, "Stopping PJSIP...");

            removeAllActiveAccounts();

            Runtime.getRuntime().gc();

            mEndpoint.libDestroy();
            mEndpoint.delete();
            mEndpoint = null;

            Log.d(TAG, "PJSIP stopped");
            mBroadcastEventSender.stackStatus(false);

        } catch (Exception exc) {
            Log.e(TAG, "Error while stopping PJSIP", exc);

        } finally {
            mStarted = false;
            mEndpoint = null;
        }
    }

    /**
     * Restarts PJSIP Stack
     */
    private void restartStack() {
        stopStack();
        addAllConfiguredAccounts();
    }

    private long getEchoCancellationOptions() {
        if (mUseNoiseSuppressor) {
            return mEchoMode|PjSipMediaEchoFlags.ECHO_USE_NOISE_SUPPRESSOR|mEchoAggressiveness;
        } else {
            return mEchoMode|mEchoAggressiveness;
        }
    }

    /**
     * Sends disconnected call state broadcast.
     * @param accountID The account ID uri.
     * @param callID The call ID.
     */
    private void sendCallDisconnected(String accountID, int callID) {
        mBroadcastEventSender.callState(accountID, callID,
                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.swigValue(), 0, false, false);
    }

    /**
     * Gets a {@link PjSipCall}.
     * @param accountID The account ID uri.
     * @param callID The call ID.
     * @return
     */
    private PjSipCall getCall(String accountID, int callID) {
        PjSipAccount account = sActiveSipAccounts.get(accountID);

        if (account == null) {
            return null;
        }

        return account.getCall(callID);
    }

    /**
     * Gets the call status.
     * Sends an {@link BroadcastEventSender#callState(String, int, int, long, boolean, boolean)}
     * @param intent The caller intent.
     */
    private void GetCallStatus(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        PjSipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            sendCallDisconnected(accountID, callID);
            return;
        }

        mBroadcastEventSender.callState(accountID, callID, sipCall.getCurrentState().swigValue(),
                                        sipCall.getConnectTimestamp(), sipCall.isLocalHold(), sipCall.isLocalMute());
    }

    /**
     * Sends DTMF using RFC2833
     * @param intent The caller intent.
     */
    private void sendDTMF(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);
        String dtmf = intent.getStringExtra(PARAM_DTMF);

        PjSipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            sendCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.sendDTMF(dtmf);
        } catch (Exception exc) {
            Log.e(TAG, "Error while dialing dtmf: " + dtmf + ". AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    /**
     * Accepts incoming call.
     * @param intent The caller intent.
     */
    private void acceptIncomingCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        PjSipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            sendCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.acceptIncomingCall();
        } catch (Exception exc) {
            Log.e(TAG, "Error while accepting incoming call. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void makeCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        String number = intent.getStringExtra(PARAM_NUMBER);

        try {
            PjSipCall call = sActiveSipAccounts.get(accountID).addOutgoingCall(number);
            mBroadcastEventSender.outgoingCall(accountID, call.getId(), number);
        } catch (Exception ex) {
            Log.e(TAG, "Error while making call. AccountID: "
                    + accountID + ", number: " + number);
        }
    }

    /**
     * Hangs up call.
     * @param intent The caller intent.
     */
    private void hangUpCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        PjSipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            sendCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.hangUp();
        } catch (Exception exc) {
            Log.e(TAG, "Error while hanging up call", exc);
        }
    }

    /**
     * Hangs up calls for account.
     * @param intent The caller intent.
     */
    private void hangUpActiveCalls(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        PjSipAccount account = sActiveSipAccounts.get(accountID);
        if (account == null) {
            return;
        }

        Set<Integer> activeCallIDs = account.getCallIDs();

        for (int callID : activeCallIDs) {
            PjSipCall sipCall = getCall(accountID, callID);

            if (sipCall == null) {
                sendCallDisconnected(accountID, callID);
                continue;
            }

            try {
                sipCall.hangUp();
            } catch (Exception exc) {
                Log.e(TAG, "Error while hanging up call", exc);
            }
        }
    }

    /**
     * Resets configured accounts.
     * @param intent The caller intent.
     */
    private void resetAccounts(Intent intent) {
        Iterator<SipAccount> it = mConfiguredAccounts.iterator();

        while (it.hasNext()) {
            SipAccount account = it.next();

            try {
                removeAccount(account.getIdUri());
                it.remove();
            } catch (Exception exc) {
                Log.e(TAG, "Error while removing account " + account.getIdUri(), exc);
            }

            SharedPreferencesUtil.persistConfiguredAccounts(this, mConfiguredAccounts);
        }
    }

    /**
     * Removes account from configured accounts.
     * @param intent The caller intent.
     */
    private void removeAccount(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        Iterator<SipAccount> it = mConfiguredAccounts.iterator();

        while (it.hasNext()) {
            SipAccount account = it.next();

            if (account.getIdUri().equals(accountID)) {
                try {
                    removeAccount(accountID);
                    it.remove();
                    SharedPreferencesUtil.persistConfiguredAccounts(this, mConfiguredAccounts);
                } catch (Exception exc) {
                    Log.e(TAG, "Error while removing account " + accountID, exc);
                }
                break;
            }
        }
    }

    /**
     * Saves account in configured accounts.
     * @param intent The caller intent.
     */
    private void setAccount(Intent intent) {
        SipAccount account = intent.getParcelableExtra(PARAM_ACCOUNT_DATA);

        int index = mConfiguredAccounts.indexOf(account);
        if (index == -1) {
            resetAccounts(intent);

            try {
                setCodecPriorities(intent);
                addAccount(account);
                mConfiguredAccounts.add(account);
                SharedPreferencesUtil.persistConfiguredAccounts(this, mConfiguredAccounts);
            } catch (Exception exc) {
                Log.e(TAG, "Error while adding " + account.getIdUri(), exc);
            }
        } else {

            try {
                removeAccount(account.getIdUri());
                setCodecPriorities(intent);
                addAccount(account);
                mConfiguredAccounts.set(index, account);
                SharedPreferencesUtil.persistConfiguredAccounts(this, mConfiguredAccounts);
            } catch (Exception exc) {
                Log.e(TAG, "Error while reconfiguring " + account.getIdUri());
            }

        }
    }

    /**
     * Gets a list of codecs priorities.
     * @return List of codecs priorities.
     */
    private ArrayList<CodecPriority> getCodecPriorityList() {
        startStack();

        if (!mStarted) {
            return null;
        }

        try {
            CodecInfoVector codecs = mEndpoint.codecEnum();
            if (codecs == null || codecs.size() == 0) {
                return null;
            }

            ArrayList<CodecPriority> codecPrioritiesList = new ArrayList<>((int) codecs.size());

            for (int i = 0; i < (int)codecs.size(); i++) {
                CodecInfo codecInfo = codecs.get(i);
                CodecPriority newCodec = new CodecPriority(codecInfo.getCodecId(),
                        codecInfo.getPriority());
                if (!codecPrioritiesList.contains(newCodec))
                    codecPrioritiesList.add(newCodec);
                codecInfo.delete();
            }

            codecs.delete();

            Collections.sort(codecPrioritiesList);
            return codecPrioritiesList;
        } catch (Exception exc) {
            Log.e(TAG, "Error while getting codec priority list!", exc);
            return null;
        }
    }

    /**
     * Sets codec priorities.
     * Sends {@link BroadcastEventSender#codecPriorities(ArrayList)}.
     */
    private void getCodecPriorities() {
        ArrayList<CodecPriority> codecs = getCodecPriorityList();

        if (codecs != null) {
            mBroadcastEventSender.codecPriorities(codecs);
        }
    }

    /**
     * Sets codec priorities
     * Sends {@link BroadcastEventSender#codecPrioritiesSetStatus(boolean)}.
     * @param intent The caller intent.
     */
    private void setCodecPriorities(Intent intent) {
        ArrayList<CodecPriority> codecPriorities = intent.getParcelableArrayListExtra(PARAM_CODEC_PRIORITIES);

        if (codecPriorities == null) {
            return;
        }

        startStack();

        if (!mStarted) {
            mBroadcastEventSender.codecPrioritiesSetStatus(false);
            return;
        }

        try {
            StringBuilder log = new StringBuilder();
            log.append("Codec priorities successfully set. The priority order is now:\n");

            for (CodecPriority codecPriority : codecPriorities) {
                mEndpoint.codecSetPriority(codecPriority.getCodecId(), (short) codecPriority.getPriority());
                log.append(codecPriority.toString()).append("\n");
            }

            SharedPreferencesUtil.persistConfiguredCodecPriorities(this, codecPriorities);
            Log.d(TAG, log.toString());
            mBroadcastEventSender.codecPrioritiesSetStatus(true);

        } catch (Exception exc) {
            Log.e(TAG, "Error while setting codec priorities", exc);
            mBroadcastEventSender.codecPrioritiesSetStatus(false);
        }
    }

    /**
     * Gets the registration status.
     * Sends {@link BroadcastEventSender#registrationState(String, int)}.
     * @param intent The caller intent.
     */
    private void getRegistrationStatus(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        if (!mStarted || sActiveSipAccounts.get(accountID) == null) {
            mBroadcastEventSender.registrationState("", 400);
            return;
        }

        PjSipAccount account = sActiveSipAccounts.get(accountID);
        try {
            mBroadcastEventSender.registrationState(accountID, account.getInfo().getRegStatus().swigValue());
        } catch (Exception exc) {
            Log.e(TAG, "Error while getting registration status for " + accountID, exc);
        }
    }

    private void getCallStatus(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        PjSipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            mBroadcastEventSender.callState(accountID, callID,
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.swigValue(), 0, false, false);
            return;
        }

        mBroadcastEventSender.callState(accountID, callID, sipCall.getCurrentState().swigValue(),
                sipCall.getConnectTimestamp(), sipCall.isLocalHold(),
                sipCall.isLocalMute());
    }

    /**
     * Reregisters an account.
     * @param intent The caller intent.
     */
    private void reRegisterAccount(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        if (sActiveSipAccounts.containsKey(accountID)) {
            try {
                sActiveSipAccounts.get(accountID).setRegistration(true);
            } catch (Exception exc) {
                Log.e(TAG, "Error while reregistering account", exc);
            }
        }
    }

    /**
     * Sets a flag indicating whether the ringtone should be used or not.
     * @param intent The caller intent.
     */
    private void setRingToneOn(Intent intent) {
        boolean ringtoneOn = intent.getBooleanExtra(PARAM_RINGTONE_ON, true);

        SharedPreferencesUtil.setRingtoneOn(this, ringtoneOn);
    }

    /**
     * Sets a flag indicating whether the vibrator should be used or not.
     * @param intent The caller intent.
     */
    private void setVibratorOn(Intent intent) {
        boolean vibratorOn = intent.getBooleanExtra(PARAM_VIBRATOR_ON, true);

        SharedPreferencesUtil.setVibratorOn(this, vibratorOn);
    }

    /**
     * Sets the path to the ringtone.
     * @param intent The caller intent.
     */
    private void setRingtone(Intent intent) {
        String ringtone = intent.getStringExtra(PARAM_RINGTONE);

        mRingtoneUri = Uri.parse(ringtone);
        SharedPreferencesUtil.setRingtone(this, ringtone);
    }

    private void setEchoCancellationSettings(Intent intent) {
        long mode;
        long aggressiveness;

        int tailLength = intent.getIntExtra(PARAM_ECHO_CANCELLATION_TAIL_LENGTH, 30);
        boolean useNoiseSuppressor = intent.getBooleanExtra(PARAM_ECHO_CANCELLATION_NOISE_REDUCTION, false);

        switch ((MediaEchoFlags) intent.getSerializableExtra(PARAM_ECHO_CANCELLATION_MODE)) {
            case ECHO_MODE_WEBRTC:
                mode = PjSipMediaEchoFlags.ECHO_WEBRTC;
                break;
            default:
                mode = PjSipMediaEchoFlags.ECHO_WEBRTC;
                break;
        }

        switch ((MediaEchoFlags) intent.getSerializableExtra(PARAM_ECHO_CANCELLATION_AGGRESSIVENESS)) {
            case ECHO_AGGRESSIVENESS_CONVERVATIVE:
                aggressiveness = PjSipMediaEchoFlags.ECHO_AGGRESSIVENESS_CONVERVATIVE;
                break;
            case ECHO_AGGRESSIVENESS_MODERATE:
                aggressiveness = PjSipMediaEchoFlags.ECHO_AGGRESSIVENESS_MODERATE;
                break;
            case ECHO_AGGRESSIVENESS_AGGRESSIV:
                aggressiveness = PjSipMediaEchoFlags.ECHO_AGGRESSIVENESS_AGGRESSIV;
                break;
            default:
                aggressiveness = PjSipMediaEchoFlags.ECHO_AGGRESSIVENESS_DEFAULT;
                break;
        }

        SharedPreferencesUtil.setEchoCancellationMode(this, mode);
        SharedPreferencesUtil.setEchoCancellationAggressiveness(this, aggressiveness);
        SharedPreferencesUtil.setEchoCancellationUseNoiseSuppressor(this, useNoiseSuppressor);
        SharedPreferencesUtil.setEchoCancellationTailLength(this, tailLength);

        mEchoMode = mode;
        mEchoAggressiveness = aggressiveness;
        mEchoTailLength = tailLength;
        mUseNoiseSuppressor = useNoiseSuppressor;

        try {
            mEndpoint.audDevManager().setEcOptions(mEchoTailLength, getEchoCancellationOptions());
        } catch (Exception exc) {
            Log.e(TAG, "Echo cancellation options could not be set.");
        }
    }

    /**
     * Removes all active accounts.
     */
    private void removeAllActiveAccounts() {
        if (!sActiveSipAccounts.isEmpty()) {
            for (String accountID : sActiveSipAccounts.keySet()) {
                try {
                    removeAccount(accountID);
                } catch (Exception exc) {
                    Log.e(TAG, "Error while removing " + accountID);
                }
            }
        }
    }

    /**
     * Adds all configured accounts to the sip stack.
     */
    private void addAllConfiguredAccounts() {
        if (!mConfiguredAccounts.isEmpty()) {
            for (SipAccount accountData : mConfiguredAccounts) {
                try {
                    addAccount(accountData);
                } catch (Exception exc) {
                    Log.e(TAG, "Error while adding " + accountData.getIdUri());
                }
            }
        }
    }

    /**
     * Adds a new SIP Account and performs initial registration.
     * @param account SIP account to add
     */
    private void addAccount(SipAccount account) throws Exception {
        String accountString = account.getIdUri();

        if (!sActiveSipAccounts.containsKey(accountString)) {
            startStack();
            PjSipAccount pjSipAndroidAccount = new PjSipAccount(this, account);
            pjSipAndroidAccount.create();
            sActiveSipAccounts.put(accountString, pjSipAndroidAccount);
            Log.e(TAG, "SIP account " + account.getIdUri() + " successfully added");
        }
    }

    /**
     * Removes a SIP Account and performs un-registration.
     */
    private void removeAccount(String accountID) throws Exception {
        PjSipAccount account = sActiveSipAccounts.remove(accountID);

        if (account == null) {
            Log.e(TAG, "No account for ID: " + accountID);
            return;
        }

        Log.d(TAG, "Removing SIP account " + accountID);
        account.delete();
        Log.d(TAG, "SIP account " + accountID + " successfully removed");
    }
}
