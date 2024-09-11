package com.callrecorder;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.FileWriter;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

@ReactModule(name = CallRecorderModule.NAME)
public class CallRecorderModule extends ReactContextBaseJavaModule {
  private static final String TAG = "CallRecorderModule";
  private final ReactApplicationContext reactContext;
  RecordAccessibilityService recordCallModule = null;
  private Model model;

  public static final String NAME = "CallRecorder";

  public CallRecorderModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.recordCallModule = new RecordAccessibilityService(reactContext);
    CallReceiver callReceiver = new CallReceiver(reactContext);

    IntentFilter filter = new IntentFilter();
    filter.addAction("android.intent.action.PHONE_STATE");
    filter.addAction("android.intent.action.NEW_OUTGOING_CALL");

//    reactContext.registerReceiver(this.recordCallModule, filter);
    reactContext.registerReceiver(callReceiver, filter);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  @ReactMethod
  public void addPhonesWhiteList(ReadableArray phoneWhileList, Promise promise) {
    if (this.recordCallModule == null) {
      promise.reject("Error: Set phone whitelist", "recordCallModule is null");
    } else {
      for (int i = 0; i < phoneWhileList.size(); i++) {
        ReadableType type = phoneWhileList.getType(i);
        if (type.name() != "String") {
          promise.reject("Phone type", "Phone with index " + i + " has " + type.name() + " type!");
        } else {
          this.recordCallModule.whitelist.add(phoneWhileList.getString(i));
        }
      }
      promise.resolve(Arguments.createMap());
    }
  }

  @ReactMethod
  public void clearWhitelist() {
    this.recordCallModule.whitelist = new ArrayList<String>();
  }

  @ReactMethod
  public void getWhiteList(Promise promise) {
    WritableArray wl = Arguments.createArray();
    for (int i = 0; i < this.recordCallModule.whitelist.size(); i++) {
      wl.pushString(this.recordCallModule.whitelist.get(i));
    }
    WritableMap map = Arguments.createMap();
    map.putArray("whitelist", wl);
    promise.resolve(map);
  }

  @ReactMethod
  public void deletePhoneWhiteList(String number, Promise promise) {
    if (this.recordCallModule.whitelist.contains(number)) {
      this.recordCallModule.whitelist.remove(number);
      promise.resolve(this.recordCallModule.whitelist);
    } else {
      promise.reject("error", "whitelist not contain number " + number);
    }
  }

  @ReactMethod
  public void addPhonesBlackList(ReadableArray phoneBlackList, Promise promise) {
    if (this.recordCallModule == null) {
      promise.reject("Error: Set phone whitelist", "recordCallModule is null");
    } else {
      for (int i = 0; i < phoneBlackList.size(); i++) {
        ReadableType type = phoneBlackList.getType(i);
        if (type.name() != "String") {
          promise.reject("Phone type", "Phone with index " + i + " has " + type.name() + " type!");
        } else {
          this.recordCallModule.blacklist.add(phoneBlackList.getString(i));
        }
      }
      promise.resolve(Arguments.createMap());
    }
  }

  @ReactMethod
  public void clearBlackList() {
    this.recordCallModule.blacklist = new ArrayList<String>();
  }

  @ReactMethod
  public void deletePhoneBlackList(String number, Promise promise) {
    if (this.recordCallModule.blacklist.contains(number)) {
      this.recordCallModule.blacklist.remove(number);
      promise.resolve(this.recordCallModule.blacklist);
    } else {
      promise.reject("error", "whitelist not contain number " + number);
    }
  }

  @ReactMethod
  public void getBlackList(Promise promise) {
    WritableArray wl = Arguments.createArray();
    for (int i = 0; i < this.recordCallModule.blacklist.size(); i++) {
      wl.pushString(this.recordCallModule.blacklist.get(i));
    }
    WritableMap map = Arguments.createMap();
    map.putArray("blacklist", wl);
    promise.resolve(map);
  }

  @ReactMethod
  public void switchRecordStatus(Boolean status) {
    this.recordCallModule.isRecord = status;
  }

  @ReactMethod
  public void openAccessibilitySettings() {
    Context context = getReactApplicationContext();
    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @ReactMethod
  public void openSpecificAccessibilitySettings() {
    Context context = getReactApplicationContext();
    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//    String packageName = context.getPackageName();
//    String accessibilityService = packageName + "/" + com.callrecorder.service.RecordService.class.getCanonicalName();
//    intent.putExtra(Settings.EXTRA_ACCESSIBILITY_SERVICES, accessibilityService);

    context.startActivity(intent);
  }

  /* convert wav file to text */
  @ReactMethod
  public void downloadAndLoadModel(String modelUrl, Promise promise) {
    Log.d(TAG, "downloadAndLoadModel: " + modelUrl);
    OkHttpClient client = new OkHttpClient();

    // Extract the filename from the URL
    String fileName;
    try {
      URL url = new URL(modelUrl);
      fileName = new File(url.getPath()).getName(); // Extract the filename
    } catch (MalformedURLException e) {
      promise.reject("URLMalformed", "Invalid URL: " + e.getMessage());
      return;
    }

    // Get the default internal storage path and create the file with the extracted name
    Context context = getReactApplicationContext();
    File modelFile = new File(context.getFilesDir(), fileName);
    String folderName = fileName.substring(0, fileName.lastIndexOf('.'));
    File targetDir = new File(context.getFilesDir(), "models");
    File modelFolder = new File(targetDir.getPath() + "/" + folderName);

    Log.d(TAG, "modelFile.exists(): " + modelFile.exists());
    if (modelFile.exists()) {
      promise.reject("modelFile error", modelFile.getPath() + " is existed");
      return;
    }

    if (modelFolder.exists()) {
      promise.reject("modelFolder error", modelFolder.getPath() + " is existed");
      return;
    }


    Request request = new Request.Builder().url(modelUrl).build();

    Log.d(TAG, "downloadAndLoadModel fileName:" + fileName);

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        Log.e(TAG, "DownloadError: " + e.getMessage());
        promise.reject("DownloadError", e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          promise.reject("DownloadError", "Failed to download model");
          return;
        }

        // Save the model to internal storage
        try (InputStream is = response.body().byteStream();
             FileOutputStream fos = new FileOutputStream(modelFile)) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
          }
          fos.flush();
        }

        // Unpack the downloaded ZIP file
        try {
          ZipUtils.unzip(modelFile, targetDir);
        } catch (IOException e) {
          promise.reject("UnzipError", "Failed to unzip model: " + e.getMessage());
          return;
        }

        try {
          // Load the model after unzipping
          model = new Model(targetDir.getPath() + "/" + folderName);
          promise.resolve("Model downloaded and loaded successfully. File saved as: " + fileName);
        } catch (Exception e) {
          promise.reject("ModelLoadError", "Failed to load model: " + e.getMessage());
        }
      }
    });
  }

  private void loadModel() {

  }

  @ReactMethod
  public void transcribeWav(String wavFilePath, Promise promise) {
    if (model == null) {
      promise.reject("ModelNotLoaded", "Model is not loaded. Call loadModel() first.");
      return;
    }

    new Thread(() -> {
      try {
        Recognizer recognizer = new Recognizer(model, 16000);
        FileInputStream fileInputStream = new FileInputStream(wavFilePath);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
          recognizer.acceptWaveForm(buffer, bytesRead);
        }

        String result = recognizer.getFinalResult();
        recognizer.close();
        fileInputStream.close();

        promise.resolve(result);
      } catch (IOException e) {
        promise.reject("TranscriptionError", e.getMessage());
      }
    }).start();
  }
}
