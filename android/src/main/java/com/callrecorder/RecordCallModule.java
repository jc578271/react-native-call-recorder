package com.callrecorder;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

import com.callrecorder.service.RecordService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class RecordCallModule extends AccessibilityService {
  private static final String TAG = "RecordCallModule";

  private static RecordService recordService = null;
  private static ReactApplicationContext reactContext = null;
  public static ArrayList<String> whitelist = new ArrayList<String>();
  public static ArrayList<String> blacklist = new ArrayList<String>();
  public static Boolean isRecord = false;

  public RecordCallModule() {
    super();
  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
  }

  @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
  @Override
  public void onCreate() {
    super.onCreate();

    Log.d(TAG, reactContext.getPackageName());

    String packageName = reactContext.getPackageName();

    // Register the system-wide broadcast receiver to listen for the custom intents
    IntentFilter filter = new IntentFilter();
    filter.addAction(packageName + ".IncomingCallReceived");
    filter.addAction(packageName + ".IncomingCallAnswered");
    filter.addAction(packageName + ".IncomingCallEnded");
    filter.addAction(packageName + ".OutgoingCallStarted");
    filter.addAction(packageName + ".OutgoingCallEnded");
    filter.addAction(packageName + ".MissedCall");

    // Register using the correct context
    try {
      registerReceiver(recordingReceiver, filter, RECEIVER_EXPORTED);
      Log.d(TAG, "Receiver registered");
    } catch (Exception e) {
      Log.e(TAG, "Error registering receiver: " + e.getMessage());
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Unregister the broadcast receiver to prevent memory leaks
    unregisterReceiver(recordingReceiver);
  }

  @Override
  public void onInterrupt() {
    // Handle interruption if needed
    recordService.stopRecord();
  }

  RecordCallModule(ReactApplicationContext context) {
    super();
    reactContext = context;
  }

  private void startRecord(String name, String start) {
    recordService = new RecordService();
    recordService.setFileName(name + start.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + ".mp3");
    recordService.setPath(reactContext.getFilesDir().getPath());
    recordService.startRecord();
  }

  // Define the BroadcastReceiver
  private BroadcastReceiver recordingReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String packageName = reactContext.getPackageName();
      String action = intent.getAction();
      String number = intent.getStringExtra("number");
      String start = intent.getStringExtra("start");
      String end = intent.getStringExtra("end");

      /* IncomingCallReceived */
      if ((packageName+".IncomingCallReceived").equals(action)) {
        Log.i("CALL_RECORDER", "RECEIVED" + number);
        WritableMap params = Arguments.createMap();
        params.putString("number", number);
        params.putString("type", "INCOMING_RECEIVED" + whitelist.size() + " " + blacklist.size());
        emitDeviceEvent("onIncomingCallReceived", params);
        return;
      }

      /* IncomingCallAnswered */
      if ((packageName+".IncomingCallAnswered").equals(action)) {
        Log.i("CALL_RECORDER", "ANSWERED" + whitelist.size() + " " + blacklist.size());
        Log.i("isRecord", isRecord.toString());
        if (isRecord == false) {
          WritableMap params = Arguments.createMap();
          params.putString("number", number);
          params.putString("type", "INCOMING_ANSWERED");
          params.putString("reason", "Record is disabled");
          emitDeviceEvent("onBlockRecordPhoneCall", params);
        } else {
          if (whitelist.isEmpty() && blacklist.isEmpty()) {
            startRecord("record-incoming-", start);
          } else if (whitelist.size() > 0 && blacklist.size() == 0) {
            if (whitelist.contains(number)) {
              startRecord("record-incoming-", start);
            } else {
              WritableMap params = Arguments.createMap();
              params.putString("number", number);
              params.putString("type", "INCOMING_ANSWERED");
              params.putString("reason", "This phone is not exist in white list");
              emitDeviceEvent("onBlockRecordPhoneCall", params);
            }
          } else if (blacklist.size() > 0 && whitelist.size() == 0) {
            if (blacklist.contains(number)) {
              WritableMap params = Arguments.createMap();
              params.putString("number", number);
              params.putString("type", "INCOMING_ANSWERED");
              params.putString("reason", "This phone in black list");
              emitDeviceEvent("onBlockRecordPhoneCall", params);
            } else {
              startRecord("record-incoming-", start);
            }
          } else {
            WritableMap params = Arguments.createMap();
            params.putString("number", number);
            params.putString("type", "INCOMING_ANSWERED");
            params.putString("reason", "Use whitelist or blacklist");
            emitDeviceEvent("onBlockRecordPhoneCall", params);
          }
        }
        return;
      }

      /* IncomingCallEnded */
      if ((packageName+".IncomingCallEnded").equals(action)) {
        if (recordService != null) {
          String path = recordService.stopRecord();
          WritableMap params = Arguments.createMap();
          params.putString("filePath", path);
          params.putString("number", number);
          params.putString("start", start);
          params.putString("end", end);
          emitDeviceEvent("onIncomingCallRecorded", params);
        }
        return;
      }

      /* OutgoingCallStarted */
      if ((packageName+".OutgoingCallStarted").equals(action)) {
        Log.i(TAG, "onOutgoingCallStarted");
        if (isRecord == false) {
          WritableMap params = Arguments.createMap();
          params.putString("number", number);
          params.putString("type", "OUTGOING_ANSWERED");
          params.putString("reason", "Record is disabled");
          emitDeviceEvent("onBlockRecordPhoneCall", params);
        } else {
          if (whitelist.isEmpty() && blacklist.isEmpty()) {
            startRecord("record-outgoing-", start);
          } else if (whitelist.size() > 0 && blacklist.size() == 0) {
            if (whitelist.contains(number)) {
              startRecord("record-outgoing-", start);
            } else {
              WritableMap params = Arguments.createMap();
              params.putString("number", number);
              params.putString("type", "OUTGOING_ANSWERED");
              params.putString("reason", "This phone is not exist in white list");
              emitDeviceEvent("onBlockRecordPhoneCall", params);
            }
          } else if (blacklist.size() > 0 && whitelist.size() == 0) {
            if (blacklist.contains(number)) {
              WritableMap params = Arguments.createMap();
              params.putString("number", number);
              params.putString("type", "OUTGOING_ANSWERED");
              params.putString("reason", "This phone in black list");
              emitDeviceEvent("onBlockRecordPhoneCall", params);
            } else {
              startRecord("record-outgoing-", start);
            }
          } else {
            WritableMap params = Arguments.createMap();
            params.putString("number", number);
            params.putString("type", "OUTGOING_ANSWERED");
            params.putString("reason", "Use whitelist or blacklist");
            emitDeviceEvent("onBlockRecordPhoneCall", params);
          }
        }
        return;
      }

      /* OutgoingCallEnded */
      if ((packageName+".OutgoingCallEnded").equals(action)) {
        if (recordService != null) {
          String path = recordService.stopRecord();
          WritableMap params = Arguments.createMap();
          params.putString("filePath", path);
          params.putString("number", number);
          params.putString("start", start);
          params.putString("end", end);
          emitDeviceEvent("onOutgoingCallRecorded", params);
        }
        return;
      }

      /* MissedCall */
      if ((packageName+".MissedCall").equals(action)) {
        WritableMap params = Arguments.createMap();
        params.putString("number", number);
        params.putString("date", start);
        emitDeviceEvent("onMissedCall", params);
        return;
      }
    }
  };

  private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
    // A method for emitting from the native side to JS
    // https://facebook.github.io/react-native/docs/native-modules-android.html#sending-events-to-javascript
    Log.i("CALL_START", reactContext.getPackageCodePath());
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
  }

}
