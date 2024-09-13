package com.callrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;

@ReactModule(name = CallRecorderModule.NAME)
public class CallRecorderModule extends ReactContextBaseJavaModule {
  private static final String TAG = "CallRecorderModule";
  private final ReactApplicationContext reactContext;
  RecordAccessibilityService recordCallModule = null;
  //  private Model model;
//  private String modelName;
//  private Utils commonUtil;
//  private static final int NOTIFICATION_ID = 1;
//  private static final int NOTIFICATION_ID_2 = 2;
//  private NotificationCompat.Builder builder;
//  private NotificationManagerCompat notificationManager;

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

//    commonUtil = new Utils(getReactApplicationContext());

    /* setup notification */
//    notificationManager = NotificationManagerCompat.from(reactContext);
//    createNotificationChannel(reactContext);
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

//  /* convert wav file to text */
//  @ReactMethod
//  public void downloadAndLoadModel(String modelUrl, Promise promise) {
//    Log.d(TAG, "downloadAndLoadModel: " + modelUrl);
//    OkHttpClient client = new OkHttpClient();
//    Request request = new Request.Builder().url(modelUrl).build();
//    Context context = getReactApplicationContext();
//
//    Utils modelInfo = new Utils(modelUrl, context);
//
//    File modelFile = modelInfo.getModelFile();
//    String folderName = modelInfo.getFolderName();
//    File targetDir = modelInfo.getTargetDir();
//    File modelFolder = modelInfo.getModelFolder();
//
//    if (modelFile.exists()) {
//      _loadModel(targetDir, folderName, modelFolder, modelFile, promise);
//      return;
//    }
//
//    new Thread(() -> {
//      try {
//        Response response = client.newCall(request).execute();
//        if (!response.isSuccessful()) {
//          promise.reject("Download Error", "Failed to download the model");
//          return;
//        }
//
//        // Save the model to internal storage
//        InputStream is = response.body().byteStream();
//        FileOutputStream fos = new FileOutputStream(modelFile);
//        byte[] buffer = new byte[4096];
//        long total = 0;
//        long fileLength = response.body().contentLength();
//        int bytesRead;
//
//        builder = new NotificationCompat.Builder(getReactApplicationContext(), "download_channel")
//          .setSmallIcon(android.R.drawable.stat_sys_download)
//          .setContentTitle("Downloading Vosk Model")
//          .setContentText("Download in progress")
//          .setPriority(NotificationCompat.PRIORITY_LOW)
//          .setOngoing(true)
//          .setProgress(100, 0, false);
//
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//          return;
//        }
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
//
//        while ((bytesRead = is.read(buffer)) != -1) {
//          total += bytesRead;
//          fos.write(buffer, 0, bytesRead);
//
//          // Update the progress notification
//          int progress = (int) (total * 100 / fileLength);
//          builder.setProgress(100, progress, false);
//          notificationManager.notify(NOTIFICATION_ID, builder.build());
//        }
//        fos.flush();
//        fos.close();
//        is.close();
//
//        // Cancel the notification after showing completion
//        notificationManager.cancel(NOTIFICATION_ID);
//
//        _loadModel(targetDir, folderName, modelFolder, modelFile, promise);
//      } catch (IOException e) {
//        Log.e(TAG, "DownloadError: " + e.getMessage());
//        promise.reject("DownloadError", e.getMessage());
//      }
//    }).start();
//
//  }
//
//  @ReactMethod
//  public boolean getModelAvailable(String modelUrl, Promise promise) {
//    Utils modelInfo = new Utils(modelUrl, getReactApplicationContext());
//    boolean a = modelInfo.getModelFile().exists() && modelInfo.getModelFolder().exists();
//    promise.resolve(a);
//    return a;
//  }
//
//  @ReactMethod
//  public void getCurrentModel(Promise promise) {
//    promise.resolve(modelName);
//  }
//
//  @ReactMethod
//  public void getModels(Promise promise) {
//    promise.resolve(commonUtil.getModels());
//  }
//
//  @ReactMethod
//  public void loadModel(String folderName, Promise promise) {
//    _loadModel(commonUtil.getTargetDir(), folderName, null, null, promise);
//  }
//
//  @ReactMethod
//  public void transcribeWav(String wavFilePath, Promise promise) {
//    if (model == null) {
//      promise.reject("ModelNotLoaded", "Model is not loaded. Call loadModel() first.");
//      return;
//    }
//
//    new Thread(() -> {
//      try {
//        Recognizer recognizer = new Recognizer(model, 16000);
//        FileInputStream fileInputStream = new FileInputStream(wavFilePath);
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//
//        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//          recognizer.acceptWaveForm(buffer, bytesRead);
//        }
//
//        String result = recognizer.getFinalResult();
//        recognizer.close();
//        fileInputStream.close();
//
//        promise.resolve(result);
//      } catch (IOException e) {
//        promise.reject("TranscriptionError", e.getMessage());
//      }
//    }).start();
//  }
//
//  private void _loadModel(File targetDir, String folderName, @Nullable File modelFolder, @Nullable File modelFile, Promise promise) {
//    try {
//      if (modelFolder != null && !modelFolder.exists()) {
//        // Unpack the downloaded ZIP file
//        try {
//          Utils.unzip(modelFile, targetDir);
//        } catch (IOException e) {
//          promise.reject("UnzipError", "Failed to unzip model: " + e.getMessage());
//          return;
//        }
//      }
//
//      // Load the model after unzipping
//      model = new Model(targetDir.getAbsolutePath() + "/" + folderName);
//      modelName = folderName;
//      promise.resolve(folderName);
//    } catch (Exception e) {
//      promise.reject("ModelLoadError", "Failed to load model: " + e.getMessage());
//    }
//  }
//
//  private void createNotificationChannel(Context context) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//      Log.d(TAG, "createNotificationChannel");
//      CharSequence name = "Download Channel";
//      String description = "Channel for download progress notifications";
//      int importance = NotificationManager.IMPORTANCE_LOW;
//      NotificationChannel channel = new NotificationChannel("download_channel", name, importance);
//      channel.setDescription(description);
//      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//      notificationManager.createNotificationChannel(channel);
//    }
//  }
}
