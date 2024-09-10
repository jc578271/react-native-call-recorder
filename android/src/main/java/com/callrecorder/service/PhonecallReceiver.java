package com.callrecorder.service;

import java.util.Date;
import java.util.Objects;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;


public abstract class PhonecallReceiver extends BroadcastReceiver {
  private static final String TAG = "PhonecallReceiver";

  //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

  private static int lastState = TelephonyManager.CALL_STATE_IDLE;
  private static Date callStartTime;
  private static boolean isIncoming;
  private static String savedNumber;  //because the passed incoming is only valid in ringing


  @Override
  public void onReceive(final Context context, Intent intent) {
//    Log.d(TAG, Objects.requireNonNull(intent.getAction()));
    TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    telephony.listen(new PhoneStateListener() {
      @Override
      public void onCallStateChanged(int state, String phoneNumber) {
//                Log.i("tate", String.valueOf(state));
//                Log.i("num", phoneNumber);
        onCustomCallStateChanged(context, state, phoneNumber);
      }
    }, PhoneStateListener.LISTEN_CALL_STATE);

//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
//                == PackageManager.PERMISSION_GRANTED) {
//            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                Intent callIntent = intent;
//                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    telephony.registerTelephonyCallback(context.getMainExecutor(), new TelephonyCallback.CallStateListener() {
//                        @Override
//                        public void onCallStateChanged(int state) {
//                            onCustomCallStateChanged(context, state, phoneNumber);
//                        }
//                    });
//                }
//            } else {
//                telephony.listen(new PhoneStateListener() {
//                    @Override
//                    public void onCallStateChanged(int state, String phoneNumber) {
//                        onCustomCallStateChanged(context, state, phoneNumber);
//                    }
//                }, PhoneStateListener.LISTEN_CALL_STATE);
//            }
//        }
  }

  //Derived classes should override these to respond to specific events of interest
  protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);

  protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);

  protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

  protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);

  protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

  protected abstract void onMissedCall(Context ctx, String number, Date start);

  //Deals with actual events

  //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
  //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
  public void onCustomCallStateChanged(Context context, int state, String number) {
    if (lastState == state) {
      //No change, debounce extras
      return;
    }
    switch (state) {
      case TelephonyManager.CALL_STATE_RINGING:
        isIncoming = true;
        callStartTime = new Date();
        savedNumber = number;
        onIncomingCallReceived(context, number, callStartTime);
        break;
      case TelephonyManager.CALL_STATE_OFFHOOK:
        //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
        if (lastState != TelephonyManager.CALL_STATE_RINGING) {
          isIncoming = false;
          callStartTime = new Date();
          onOutgoingCallStarted(context, number, callStartTime);
        } else {
          isIncoming = true;
          callStartTime = new Date();
          onIncomingCallAnswered(context, number, callStartTime);
        }

        break;
      case TelephonyManager.CALL_STATE_IDLE:
        //Went to idle-  this is the end of a call.  What type depends on previous state(s)
        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
          //Ring but no pickup-  a miss
          onMissedCall(context, number, callStartTime);
        } else if (isIncoming) {
          onIncomingCallEnded(context, number, callStartTime, new Date());
        } else {
          onOutgoingCallEnded(context, number, callStartTime, new Date());
        }
        break;
    }
    lastState = state;
  }
}
