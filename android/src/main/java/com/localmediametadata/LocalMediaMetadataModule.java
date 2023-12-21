package com.localmediametadata;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = LocalMediaMetadataModule.NAME)
public class LocalMediaMetadataModule extends ReactContextBaseJavaModule {
  public static final String NAME = "LocalMediaMetadata";

  private final ReactApplicationContext reactContext;

  public LocalMediaMetadataModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

//  @ReactMethod
//  public void scanFiles(String dirPath, ReadableArray _extNames, Promise promise) {
//    ArrayList<String> extNames = new ArrayList<>();
//    for (Object obj : _extNames.toArrayList()) extNames.add(String.valueOf(obj));
//
//    AsyncTask.runTask(new MetadataCallable.ScanFiles(reactContext, dirPath, extNames), promise);
//  }

  @ReactMethod
  public void readMetadata(String filePath, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.ReadMetadata(reactContext, filePath), promise);
  }
  @ReactMethod
  public void writeMetadata(String filePath, ReadableMap metadata, boolean isOverwrite, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.WriteMetadata(reactContext, filePath, Arguments.toBundle(metadata), isOverwrite), promise);
  }

  @ReactMethod
  public void readPic(String filePath, String picDir, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.ReadPic(reactContext, filePath, picDir), promise);
  }
  @ReactMethod
  public void writePic(String filePath, String picPath, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.WritePic(reactContext, filePath, picPath), promise);
  }

  @ReactMethod
  public void readLyric(String filePath, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.ReadLyric(reactContext, filePath), promise);
  }
  @ReactMethod
  public void writeLyric(String filePath, String lyric, Promise promise) {
    AsyncTask.runTask(new MetadataCallable.WriteLyric(reactContext, filePath, lyric), promise);
  }
}
