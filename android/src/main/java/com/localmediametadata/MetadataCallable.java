package com.localmediametadata;

import android.os.Bundle;
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
  static class WriteMetadata implements Callable<Object> {
    private final String filePath;
    private final Bundle metadata;
    private final boolean isOverwrite;
    public WriteMetadata(String filePath, Bundle metadata, boolean isOverwrite) {
      this.filePath = filePath;
      this.metadata = metadata;
      this.isOverwrite = isOverwrite;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writeMetadata(this.filePath, this.metadata, this.isOverwrite);
      return null;
    }
  }

  static class ReadPic implements Callable<Object> {
    private final String filePath;
    private final String picDir;
    public ReadPic(String filePath, String picDir) {
      this.filePath = filePath;
      this.picDir = picDir;
    }
    @Override
    public String call() {
      try {
        return Metadata.readPic(this.filePath, this.picDir);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Pic Error: " + err.getMessage());
        return "";
      }
    }
  }
  static class WritePic implements Callable<Object> {
    private final String filePath;
    private final String picPath;
    public WritePic(String filePath, String picPath) {
      this.filePath = filePath;
      this.picPath = picPath;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writePic(this.filePath, this.picPath);
      return null;
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
  static class WriteLyric implements Callable<Object> {
    private final String filePath;
    private final String lyric;
    public WriteLyric(String filePath, String lyric) {
      this.filePath = filePath;
      this.lyric = lyric;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writeLyric(this.filePath, this.lyric);
      return null;
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
