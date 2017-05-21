package com.d0pam1n.doorphone.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.d0pam1n.doorphone.DoorPhoneApplication;
import com.d0pam1n.doorphone.Enums.VideoFormat;
import com.d0pam1n.doorphone.R;
import com.d0pam1n.doorphone.utils.NotificationBuilderUtil;
import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.BroadcastHandler.BroadcastEventReceiver;
import com.d0pam1n.siplib.SipCommands;

import org.pjsip.pjsua2.pjsip_inv_state;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * Main activity for all sip and video functionalities.
 *
 * @author Andreas Pfister
 */

public class MainActivity extends BaseActivity implements IVLCVout.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mCallId;
    private boolean mCallActive;
    private boolean mIsMicrophoneMuted;
    private boolean mCloseAfterCall;
    private boolean mIdleCall;
    private boolean mIncomingCall;

    private AudioManager mAudioManager;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private FrameLayout mSurfaceFrame;

    private LibVLC mLibvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mIsPlaying;
    private String mMediaUrl;
    private VideoFormat mCurrentSize;
    private int mVideoVisibleWidth;
    private int mVideoVisibleHeight;
    private int mSarNum;
    private int mSarDen;

    private RelativeLayout mMainLayout;

    private FloatingActionButton mVideoButton;
    private FloatingActionButton mCallButton;
    private FloatingActionButton mUnlockButton;
    private FloatingActionButton mMicrophoneButton;

    private BroadcastEventReceiver sipEvents = new BroadcastEventReceiver() {
        @Override
        public void onCallState(String accountID, int callID, pjsip_inv_state callStateCode,
        long connectTimestamp, boolean isLocalHold, boolean isLocalMute) {
            if (callStateCode == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                setActiveCall(true);
                NotificationBuilderUtil.hideIncomingCallNotification(this.getReceiverContext());
                mIdleCall = false;
                return;
            } else {


                if (callStateCode == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    NotificationBuilderUtil.hideIncomingCallNotification(this.getReceiverContext());
                    close();
                }

                if (callStateCode == pjsip_inv_state.PJSIP_INV_STATE_CALLING
                        || callStateCode == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING
                        || callStateCode == pjsip_inv_state.PJSIP_INV_STATE_INCOMING
                        || callStateCode == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                    mIdleCall = true;
                    return;
                }

                setActiveCall(false);
                mIdleCall = false;
            }
        }

        @Override
        public void onIncomingCall(String accountID, int callID, String remoteUri) {
            mCallId = callID;
            mIdleCall = true;
            mIncomingCall = true;
            if (SettingsUtil.getShowNotification(this.getReceiverContext())) {
                NotificationBuilderUtil.showIncomingCallNotification(this.getReceiverContext(), accountID, callID);
            }
        }

        @Override
        public void onOutgoingCall(String accountID, int callID, String number) {
            mCallId = callID;
            setActiveCall(true);
        }
    };

    private MediaPlayer.EventListener listener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.EncounteredError:
                    Toast.makeText(MainActivity.this, R.string.message_could_not_play_video, Toast.LENGTH_LONG).show();
                    stopPlayer();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        setMicMuted(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DoorPhoneApplication.getQuitApp()) {
            DoorPhoneApplication.setQuitApp(false);
            SipCommands.stopService(MainActivity.this);
            finish();
            return;
        }

        sipEvents.register(this);
        mCallId = getIntent().getIntExtra(getString(R.string.param_call_id), 0);

        SipCommands.getCallStatus(this, SettingsUtil.getConfiguredSipAccount(this).getIdUri(), mCallId);

        mCloseAfterCall = getIntent().getBooleanExtra(getString(R.string.param_close_after_call), false);

        uncheckAllMenuItems();

        if (getIntent().hasExtra(getString(R.string.param_player_playing))) {
            mIsPlaying = getIntent().getBooleanExtra(getString(R.string.param_player_playing), false);
            getIntent().removeExtra(getString(R.string.param_player_playing));
        } else {
            mIsPlaying = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.param_player_playing), false);
        }

        mCurrentSize = SettingsUtil.getVideoFormat(this);
        mMediaUrl = SettingsUtil.getVideoPath(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        createPlayer(mMediaUrl);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause () {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getString(R.string.param_player_playing), mIsPlaying).apply();

        releasePlayer();

        sipEvents.unregister(this);
    }

    @Override
    protected void onDestroy () {
        releasePlayer();

        super.onDestroy();
        System.gc();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        sipEvents.register(this);
        mCallId = intent.getIntExtra(getString(R.string.param_call_id), 0);

        SipCommands.getCallStatus(this, SettingsUtil.getConfiguredSipAccount(this).getIdUri(), mCallId);

        mIsPlaying = intent.getBooleanExtra(getString(R.string.param_player_playing), false);
        mCloseAfterCall = getIntent().getBooleanExtra(getString(R.string.param_close_after_call), false);

        super.onNewIntent(intent);
    }

    private void createPlayer(String mediaUrl) {
        releasePlayer();

        if(mediaUrl.isEmpty()) return;

        try {

            // Create LibVLC
            mLibvlc = new LibVLC();

            // Create media player
            mMediaPlayer = new MediaPlayer(mLibvlc);

            final IVLCVout vout = mMediaPlayer.getVLCVout();

            vout.setVideoView(mSurfaceView);
            vout.addCallback(this);
            vout.attachViews();

            final Media media = new Media(mLibvlc, Uri.parse(mediaUrl));
            mMediaPlayer.setMedia(media);
            mMediaPlayer.setEventListener(listener);
            //media.release();
            if(mIsPlaying) {
                startPlayer();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }

    }

    private void releasePlayer() {
        if (mLibvlc == null)
            return;
        stopPlayer();
        mMediaPlayer.getVLCVout().removeCallback(this);
        mMediaPlayer.getVLCVout().detachViews();
        mSurfaceHolder = null;
        mLibvlc.release();
        mLibvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private void startPlayer() {

        mSurfaceHolder.setKeepScreenOn(true);

        if(mMediaPlayer != null) {
            mMediaPlayer.play();
        }

        mVideoButton.setImageResource(R.drawable.ic_video_off);
        mVideoButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
        mIsPlaying = true;
    }

    private void stopPlayer() {
        mSurfaceHolder.setKeepScreenOn(false);
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        mVideoButton.setImageResource(R.drawable.ic_video_on);
        mVideoButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
        mIsPlaying = false;

        LayoutParams lp = mSurfaceFrame.getLayoutParams();
        lp.height = 0;
        lp.width = 0;
        mSurfaceFrame.setLayoutParams(lp);

        mSurfaceView.invalidate();
    }

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        int sw;
        int sh;

        sw = mMainLayout.getWidth();
        sh = mMainLayout.getHeight();
        double dw = sw, dh = sh;
        boolean isPortrait;
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }
        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
			/* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
			/* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        SurfaceView surface;
        SurfaceHolder surfaceHolder;
        FrameLayout surfaceFrame;

        surface = mSurfaceView;
        surfaceHolder = mSurfaceHolder;
        surfaceFrame = mSurfaceFrame;

        // force surface buffer size
        surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
    }

    private void setActiveCall(final boolean isCallActive) {

        if (isCallActive) {
            mCallButton.setImageResource(R.drawable.ic_call_end);
            mCallButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
        } else {
            mCallButton.setImageResource(R.drawable.ic_call);
            mCallButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
        }

        setMicMuted(false);
        mCallActive = isCallActive;
    }

    /**
     * Initializes UI.
     */
    private void initUI() {

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_main, contentFrameLayout);

        mMainLayout = (RelativeLayout) findViewById(R.id.content_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.video);
        mSurfaceFrame = (FrameLayout) findViewById(R.id.frameLayout);

        mVideoButton = (FloatingActionButton) findViewById(R.id.button_video_play);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mMediaUrl.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.message_not_media_url, Toast.LENGTH_LONG).show();
                    return;
                }

                if(mMediaPlayer != null) {
                    if(mMediaPlayer.isPlaying()) {
                        stopPlayer();
                    } else {
                        startPlayer();
                    }
                }
            }
        });

        mCallButton = (FloatingActionButton) findViewById(R.id.button_call);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isSipConnected) {
                    return;
                }

                Context context = MainActivity.this.getApplicationContext();

                if(mCallActive) {
                    SipCommands.hangUpCall(context, activeAccount, mCallId);
                } else if (!mIdleCall) {
                    SipCommands.makeCall(context, activeAccount, SettingsUtil.getDoorPiNumber(context));
                } else if (mIncomingCall) {
                    SipCommands.acceptIncomingCall(context, activeAccount, mCallId);
                }
            }
        });

        mUnlockButton = (FloatingActionButton) findViewById(R.id.button_door_unlock);
        mUnlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isSipConnected) {
                    return;
                }

                Context context = MainActivity.this.getApplicationContext();

                if (mCallActive) {
                    SipCommands.sendDTMF(context, activeAccount, mCallId, SettingsUtil.getDTMFUnlockCode(context));
                }
            }
        });

        mMicrophoneButton = (FloatingActionButton) findViewById(R.id.button_microphone);
        mMicrophoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCallActive) {
                    setMicMuted(!mIsMicrophoneMuted);
                }
            }
        });
    }

    private void setMicMuted(boolean state) {
        int workingAudioMode = mAudioManager.getMode();

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        mAudioManager.setMicrophoneMute(state);
        mAudioManager.setSpeakerphoneOn(!state);

        if (state == false) {
            mMicrophoneButton.setImageResource(R.drawable.microphone);
        } else {
            mMicrophoneButton.setImageResource(R.drawable.microphone_off);
        }

        mIsMicrophoneMuted = state;
        mAudioManager.setMode(workingAudioMode);
    }

    private void close() {
        if(mCloseAfterCall && SettingsUtil.getCloseAfterCall(this)) {
            setActiveCall(false);
            finish();
        }
    }

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth  = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        changeSurfaceLayout();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeSurfaceLayout();
    }
}
