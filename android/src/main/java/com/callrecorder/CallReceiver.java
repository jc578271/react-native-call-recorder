package com.callrecorder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.callrecorder.service.PhonecallReceiver;
import com.facebook.react.bridge.ReactApplicationContext;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {
  private static final String TAG = "CallReceiver";
  private static ReactApplicationContext reactContext = null;

  public CallReceiver() {
    super();
  }

  CallReceiver(ReactApplicationContext context) {
    super();
    reactContext = context;
  }

  @Override
  protected void onIncomingCallReceived(Context ctx, String number, Date start) {
    startReceiver(ctx, "IncomingCallReceived", number, start);
  }

  @Override
  protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
    startReceiver(ctx, "IncomingCallAnswered", number, start);
  }

  @Override
  protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    endReceiver(ctx, "IncomingCallEnded", number, start, end);
  }

  @Override
  protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    startReceiver(ctx, "OutgoingCallStarted", number, start);
  }

  @Override
  protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    endReceiver(ctx, "OutgoingCallEnded", number, start, end);
  }

  @Override
  protected void onMissedCall(Context ctx, String number, Date start) {
    startReceiver(ctx, "MissedCall", number, start);
  }

  private void startReceiver(Context ctx, String key, String number, Date start) {
    Log.d(TAG, "Call answered, sending broadcast to " + key);

    // Send a system-wide broadcast to trigger the recording in AccessibilityService
    Intent broadcastIntent = new Intent(reactContext.getPackageName() + "." + key);
    broadcastIntent.putExtra("key", key);
    broadcastIntent.putExtra("number", number);
    broadcastIntent.putExtra("start", String.valueOf(start.getTime()));
    ctx.sendBroadcast(broadcastIntent);
  }

  private void endReceiver(Context ctx, String key, String number, Date start, Date end) {
    Log.d(TAG, "Call ended, sending broadcast to " + key);

    /// Send a system-wide broadcast to stop recording in AccessibilityService
    Intent broadcastIntent = new Intent(reactContext.getPackageName() + "." + key);
    broadcastIntent.putExtra("key", key);
    broadcastIntent.putExtra("number", number);
    broadcastIntent.putExtra("start", String.valueOf(start.getTime()));
    broadcastIntent.putExtra("end", String.valueOf(end.getTime()));
    ctx.sendBroadcast(broadcastIntent);
  }
}
