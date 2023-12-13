package com.localmediametadata;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Utils {
  private static class MediaFileFilter implements FilenameFilter {
    final Pattern pattern;
    MediaFileFilter(ArrayList<String> extNames) {
      super();
      String rxp = "mp3|flac";
      if (!extNames.isEmpty()) {
        StringBuilder result = new StringBuilder(extNames.get(0));
        for (int i = 1; i < extNames.size(); i++) {
          result.append("|").append(extNames.get(i));
        }
        rxp = result.toString();
      }
      rxp = "\\.(" + rxp + ")$";
      Log.d("MetaData", "rxp: " + rxp);
      this.pattern = Pattern.compile(rxp);
    }
    @Override
    public boolean accept(File dir, String name) {
      return pattern.matcher(name.toLowerCase()).find();
    }
  }

  public static WritableArray scanAudioFiles(ReactApplicationContext context, String directoryPath, ArrayList<String> extNames) {
    File dir = new File(directoryPath);

    WritableArray audioFilePaths = Arguments.createArray();

    if (dir.exists()) {
      File[] mediaFiles = dir.listFiles(new MediaFileFilter(extNames));
      assert mediaFiles != null;
      for (File file: mediaFiles) {
        if (file.isFile()) audioFilePaths.pushString(file.getAbsolutePath());
      }
      // if (mediaFiles.length > 0) {
      //   String[] filePaths = new String[mediaFiles.length];
      //   for (int i = 0; i < mediaFiles.length; i++) {
      //     filePaths[i] = mediaFiles[i].getAbsolutePath();
      //     Log.d("MediaScanner", filePaths[i]);
      //   }
      //   MediaScannerConnection.scanFile(context, filePaths, null,
      //     (path, uri) -> {
      //       audioFilePaths.pushString(path);
      //       Log.d("MediaScanner", "Scanned " + path + ":");
      //       Log.d("MediaScanner", "-> uri=" + uri);
      //     });
      // }
    }
    return audioFilePaths;
  }
}
