package com.callrecorder.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import java.io.IOException;

public class RecordService extends AccessibilityService {

    private MediaRecorder mediaRecorder;
    private String fileName;
    private String path;

    public static final String LOG_TAG_S = "MyService:";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void startRecord() {
        String file = path + "/" + fileName;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setOutputFile(file);
        mediaRecorder.setAudioEncodingBitRate(64000);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioSamplingRate(16000);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

            Log.e("ReactNativePhoneCallRecorder", "started: " + file);
        } catch (IOException e) {
            Log.e("ReactNativePhoneCallRecorder", "Microphone is already in use by another app. IOException: " + e);
        } catch (IllegalStateException e) {
            Log.e("ReactNativePhoneCallRecorder", "Microphone is already in use by another app. IllegalStateException: " + e);
        } catch (Exception e) {
            Log.e("ReactNativePhoneCallRecorder", "Something went wrong in start recording. Exception: " + e);
        }
    }

    public String stopRecord() {
        try {
            mediaRecorder.stop();
            Log.i("ReactNativePhoneCallRecorder", "stop record");
        } catch (Exception e) {
            mediaRecorder.reset();
            mediaRecorder.release();
            Log.e("stopping_failed", "Stop failed:" + e);
        }

        mediaRecorder.reset();
        mediaRecorder.release();
        return path + "/" + fileName;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onServiceConnected() {
    System.out.println("onServiceConnected");

    //==============================Record Audio while  Call received===============//

    WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    FrameLayout layout = new FrameLayout(this);

    WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_FULLSCREEN |
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS|
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
      PixelFormat.TRANSLUCENT);
    params.gravity = Gravity.TOP;

    windowManager.addView(layout, params);
    layout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {

        //You can either get the information here or on onAccessibilityEvent



        Log.e(LOG_TAG_S, "Window view touched........:");
        Log.e(LOG_TAG_S, "Window view touched........:");
        return true;
      }
    });

    //==============To Record Audio wile Call received=================


    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
    info.notificationTimeout = 100;
    info.packageNames = null;
    setServiceInfo(info);


    try {
      startRecord();
    } catch (Exception e) {
      e.printStackTrace();
    }



    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        // This method will be executed once the timer is over
        stopRecord();
      }
    }, 30000);

  }
}
