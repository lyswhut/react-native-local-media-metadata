package com.localmediametadata;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class MetadataCallable {
  static class ReadMetadata implements Callable<Object> {
    private final String filePath;
    public ReadMetadata(String filePath) {
      this.filePath = filePath;
    }
    @Override
    public WritableMap call() {
      try {
        return Metadata.readMetadata(this.filePath);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Metadata Error: " + err.getMessage());
        return null;
      }
    }
  }

  static class ReadPic implements Callable<Object> {
    private final String filePath;
    public ReadPic(String filePath) {
      this.filePath = filePath;
    }
    @Override
    public String call() {
      try {
        return Metadata.readPic(this.filePath);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Pic Error: " + err.getMessage());
        return "";
      }
    }
  }

  static class ReadLyric implements Callable<Object> {
    private final String filePath;
    public ReadLyric(String filePath) {
      this.filePath = filePath;
    }
    @Override
    public String call() {
      try {
        return Metadata.readLyric(this.filePath);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Lyric Error: " + err.getMessage());
        return "";
      }
    }
  }

  static class ScanFiles implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String dirPath;
    private final ArrayList<String> extNames;
    public ScanFiles(ReactApplicationContext context, String dirPath, ArrayList<String> extNames) {
      this.context = context;
      this.dirPath = dirPath;
      this.extNames = extNames;
    }

    @Override
    public WritableArray call() {
      return Utils.scanAudioFiles(this.context, this.dirPath, this.extNames);
    }
  }
}
