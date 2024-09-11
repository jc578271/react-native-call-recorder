package com.callrecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

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
