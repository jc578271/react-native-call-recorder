package com.callrecorder;

import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
  private File modelFile;
  private File targetDir;
  private File modelFolder;
  private String folderName;
  private final Context reactContext;

  Utils(Context context) {
    reactContext = context;
    new File(context.getFilesDir(), "zip_models").mkdir();
    targetDir = new File(context.getFilesDir(), "models");
  }

  Utils(String modelUrl, Context context) {
    reactContext = context;
    String fileName;
    try {
      URL url = new URL(modelUrl);
      fileName = new File(url.getPath()).getName(); // Extract the filename
    } catch (MalformedURLException e) {
      return;
    }

    // Get the default internal storage path and create the file with the extracted name
    new File(context.getFilesDir(), "zip_models").mkdir();
    modelFile = new File(context.getFilesDir(), "zip_models/" + fileName);
    folderName = fileName.substring(0, fileName.lastIndexOf('.'));
    targetDir = new File(context.getFilesDir(), "models");
    modelFolder = new File(targetDir.getPath() + "/" + folderName);
  }

  public File getModelFile() {
    return modelFile;
  }

  public File getTargetDir() {
    return targetDir;
  }

  public File getModelFolder() {
    return modelFolder;
  }

  public String getFolderName() {
    return folderName;
  }

  public WritableArray getModels() {
    File[] files = targetDir.listFiles();
    if (files != null && files.length > 0) {
      // Create an array to store file names
      WritableArray fileNames = Arguments.createArray();

      for (File file : files) {
        fileNames.pushString(file.getName());  // Add each file name to the array
      }

      Log.d("Utils", "getModels: " + fileNames);

      return fileNames;  // Return the array of file names
    }
    return Arguments.createArray();
  }

  public static void unzip(File zipFile, File targetDirectory) throws IOException {
    if (!targetDirectory.exists()) {
      targetDirectory.mkdirs();
    }

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        File entryDestination = new File(targetDirectory, entry.getName());
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try (FileOutputStream fos = new FileOutputStream(entryDestination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, length);
            }
          }
        }
      }
    }
  }
}
