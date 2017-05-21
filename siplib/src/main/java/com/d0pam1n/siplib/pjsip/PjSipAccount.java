package com.d0pam1n.siplib.pjsip;

import android.util.Log;

import com.d0pam1n.siplib.Model.SipAccount;
import com.d0pam1n.siplib.SipService;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.HashMap;
import java.util.Set;

/**
 * Wrapper around PJSUA2 Account object.
 *
 * @author Andreas Pfister
 */

public class PjSipAccount extends Account {
    private static final String LOG_TAG = PjSipAccount.class.getSimpleName();

    private HashMap<Integer, PjSipCall> mActiveCalls = new HashMap<>();
    private SipAccount mData;
    private SipService mService;

    public PjSipAccount(SipService service, SipAccount account) {
        super();
        this.mService = service;
        this.mData = account;
    }

    public SipService getSipService() {
        return mService;
    }

    public SipAccount getData() {
        return mData;
    }

    public void create() throws Exception {
        create(mData.getAccountConfig());
    }

    protected void removeCall(int callId) {
        PjSipCall call = mActiveCalls.get(callId);

        if (call != null) {
            Log.d(LOG_TAG, "Removing call with ID: " + callId);
            mActiveCalls.remove(callId);
        }
    }

    public PjSipCall getCall(int callId) {
        return mActiveCalls.get(callId);
    }

    public Set<Integer> getCallIDs() {
        return mActiveCalls.keySet();
    }

    public PjSipCall addIncomingCall(int callId) {

        PjSipCall call = new PjSipCall(this, callId);
        mActiveCalls.put(callId, call);
        Log.d(LOG_TAG, "Added incoming call with ID " + callId + " to " + mData.getIdUri());
        return call;
    }

    public PjSipCall addOutgoingCall(final String numberToDial) {
        PjSipCall call = new PjSipCall(this);

        CallOpParam callOpParam = new CallOpParam();
        try {
            if (numberToDial.startsWith("sip:")) {
                call.makeCall(numberToDial, callOpParam);
            } else {
                if ("*".equals(mData.getRealm())) {
                    call.makeCall("sip:" + numberToDial, callOpParam);
                } else {
                    call.makeCall("sip:" + numberToDial + "@" + mData.getRealm(), callOpParam);
                }
            }
            mActiveCalls.put(call.getId(), call);
            Log.d(LOG_TAG, "New outgoing call with ID: " + call.getId());

            return call;

        } catch (Exception exc) {
            Log.e(LOG_TAG, "Error while making outgoing call", exc);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PjSipAccount that = (PjSipAccount) o;

        return mData.equals(that.mData);

    }

    @Override
    public int hashCode() {
        return mData.hashCode();
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        mService.getBroadcastEventSender().registrationState(mData.getIdUri(), prm.getCode().swigValue());
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {

        PjSipCall call = addIncomingCall(prm.getCallId());

        if (mActiveCalls.size() > 1) {
            call.declineIncomingCall();
            Log.d(LOG_TAG, "sending busy to call ID: " + prm.getCallId());
            //TODO: notification of missed call
            return;
        }

        try {
            // Answer with 180 Ringing
            CallOpParam callOpParam = new CallOpParam();
            callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            call.answer(callOpParam);
            Log.d(LOG_TAG, "Sending 180 ringing");

            mService.startRingtone();

            PjSipCallInfo contactInfo = new PjSipCallInfo(call.getInfo());

            mService.getBroadcastEventSender().incomingCall(mData.getIdUri(), prm.getCallId(), contactInfo.getRemoteUri());

        } catch (Exception exc) {
            Log.e(LOG_TAG, "Error while getting call info", exc);
        }
    }
}
