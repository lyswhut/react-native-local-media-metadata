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
    private final ReactApplicationContext context;
    private final String filePath;
    public ReadMetadata(ReactApplicationContext context, String filePath) {
      this.context = context;
      this.filePath = filePath;
    }
    @Override
    public WritableMap call() {
      try {
        return Metadata.readMetadata(this.context, this.filePath);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Metadata Error:");
        err.printStackTrace();
        return null;
      }
    }
  }
  static class WriteMetadata implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String filePath;
    private final Bundle metadata;
    private final boolean isOverwrite;
    public WriteMetadata(ReactApplicationContext context, String filePath, Bundle metadata, boolean isOverwrite) {
      this.context = context;
      this.filePath = filePath;
      this.metadata = metadata;
      this.isOverwrite = isOverwrite;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writeMetadata(this.context, this.filePath, this.metadata, this.isOverwrite);
      return null;
    }
  }

  static class ReadPic implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String filePath;
    private final String picDir;
    public ReadPic(ReactApplicationContext context, String filePath, String picDir) {
      this.context = context;
      this.filePath = filePath;
      this.picDir = picDir;
    }
    @Override
    public String call() {
      try {
        return Metadata.readPic(this.context, this.filePath, this.picDir);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Pic Error:");
        err.printStackTrace();
        return "";
      }
    }
  }
  static class WritePic implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String filePath;
    private final String picPath;
    public WritePic(ReactApplicationContext context, String filePath, String picPath) {
      this.context = context;
      this.filePath = filePath;
      this.picPath = picPath;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writePic(this.context, this.filePath, this.picPath);
      return null;
    }
  }

  static class ReadLyric implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String filePath;
    private final boolean isReadLrcFile;
    public ReadLyric(ReactApplicationContext context, String filePath, boolean isReadLrcFile) {
      this.context = context;
      this.filePath = filePath;
      this.isReadLrcFile = isReadLrcFile;
    }
    @Override
    public String call() {
      try {
        return Metadata.readLyric(this.context, this.filePath, this.isReadLrcFile);
      } catch (Exception err) {
        Log.e("ReadMetadata", "Read Lyric Error: ");
        err.printStackTrace();
        return "";
      }
    }
  }
  static class WriteLyric implements Callable<Object> {
    private final ReactApplicationContext context;
    private final String filePath;
    private final String lyric;
    public WriteLyric(ReactApplicationContext context, String filePath, String lyric) {
      this.context = context;
      this.filePath = filePath;
      this.lyric = lyric;
    }
    @Override
    public Object call() throws Exception {
      Metadata.writeLyric(this.context, this.filePath, this.lyric);
      return null;
    }
  }

//  static class ScanFiles implements Callable<Object> {
//    private final ReactApplicationContext context;
//    private final String dirPath;
//    private final ArrayList<String> extNames;
//    public ScanFiles(ReactApplicationContext context, String dirPath, ArrayList<String> extNames) {
//      this.context = context;
//      this.dirPath = dirPath;
//      this.extNames = extNames;
//    }
//
//    @Override
//    public WritableArray call() {
//      return Utils.scanAudioFiles(this.context, this.dirPath, this.extNames);
//    }
//  }
}
