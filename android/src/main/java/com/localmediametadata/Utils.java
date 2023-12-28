package com.localmediametadata;

import android.net.Uri;
import android.util.Base64;

import androidx.documentfile.provider.DocumentFile;

import com.facebook.react.bridge.ReactApplicationContext;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Utils {
  public static final String LOG = "Metadata";
//  private static class MediaFileFilter implements FilenameFilter {
//    final Pattern pattern;
//    MediaFileFilter(ArrayList<String> extNames) {
//      super();
//      String rxp = "mp3|flac";
//      if (!extNames.isEmpty()) {
//        StringBuilder result = new StringBuilder(extNames.get(0));
//        for (int i = 1; i < extNames.size(); i++) {
//          result.append("|").append(extNames.get(i));
//        }
//        rxp = result.toString();
//      }
//      rxp = "\\.(" + rxp + ")$";
//      // Log.d("MetaData", "rxp: " + rxp);
//      this.pattern = Pattern.compile(rxp);
//    }
//    @Override
//    public boolean accept(File dir, String name) {
//      return pattern.matcher(name.toLowerCase()).find();
//    }
//  }

  public static boolean isContentUri(String path) {
    return path.startsWith("content://");
  }
  public static boolean isTreeUri(Uri uri) {
    return "tree".equals(uri.getPathSegments().get(0));
  }
  public static File parsePathToFile(String path) {
    if (path.contains("://")) {
      try {
        Uri pathUri = Uri.parse(path);
        return new File(pathUri.getPath());
      } catch (Throwable e) {
        return new File(path);
      }
    }
    return new File(path);
  }

  public static InputStream createInputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }
  public static InputStream createInputStream(ReactApplicationContext context, DocumentFile file) throws FileNotFoundException {
    return context.getContentResolver().openInputStream(file.getUri());
  }

  public static OutputStream createOutputStream(ReactApplicationContext context, Uri uri, boolean append) throws IOException {
    DocumentFile pFile = DocumentFile.fromSingleUri(context, uri).getParentFile();
    if (pFile != null && !pFile.exists()) {
      String name = pFile.getName();
      Objects.requireNonNull(pFile.getParentFile()).createDirectory(name);
    }
    return context.getContentResolver().openOutputStream(uri, append ? "wa" : "w");
  }
  public static OutputStream createOutputStream(File file, boolean append) throws FileNotFoundException {
    File pFile = file.getParentFile();
    if (pFile != null && !pFile.exists()) pFile.mkdirs();
    return new FileOutputStream(file, append);
  }
  public static OutputStream createOutputStream(File file) throws FileNotFoundException {
    return createOutputStream(file, false);
  }
  public static OutputStream createOutputStream(ReactApplicationContext context, Uri uri) throws IOException {
    return createOutputStream(context, uri, false);
  }

  public static String getName(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    } else {
      return fileName;
    }
  }
  public static String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex + 1);
    } else {
      return "";
    }
  }
  private static String encodeBase64(byte[] data) {
    return new String(Base64.encode(data, Base64.NO_WRAP), StandardCharsets.UTF_8);
  }
  public static String decodeString(byte[] data) {
    UniversalDetector detector = new UniversalDetector(null);
    detector.handleData(data, 0, data.length);
    detector.dataEnd();
    String detectedCharset = detector.getDetectedCharset();
    detector.reset();
    if (detectedCharset == null) detectedCharset = "UTF-8";
    try {
      return new String(data, detectedCharset);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

//  public static WritableArray scanAudioFiles(ReactApplicationContext context, String directoryPath, ArrayList<String> extNames) {
//    File dir = new File(directoryPath);
//
//    WritableArray audioFilePaths = Arguments.createArray();
//
//    if (dir.exists()) {
//      File[] mediaFiles = dir.listFiles(new MediaFileFilter(extNames));
//      if (mediaFiles != null) {
//        for (File file: mediaFiles) {
//          if (file.isFile()) audioFilePaths.pushString(file.getAbsolutePath());
//        }
//      }
//      // if (mediaFiles.length > 0) {
//      //   String[] filePaths = new String[mediaFiles.length];
//      //   for (int i = 0; i < mediaFiles.length; i++) {
//      //     filePaths[i] = mediaFiles[i].getAbsolutePath();
//      //     Log.d("MediaScanner", filePaths[i]);
//      //   }
//      //   MediaScannerConnection.scanFile(context, filePaths, null,
//      //     (path, uri) -> {
//      //       audioFilePaths.pushString(path);
//      //       Log.d("MediaScanner", "Scanned " + path + ":");
//      //       Log.d("MediaScanner", "-> uri=" + uri);
//      //     });
//      // }
//    }
//    return audioFilePaths;
//  }
}
