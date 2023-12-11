package com.localmediametadata;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.List;

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

  @ReactMethod
  public void scanFiles(String dirPath, ReadableArray _extNames, Promise promise) {
    TaskRunner taskRunner = new TaskRunner();
    ArrayList<String> extNames = new ArrayList<>();
    for (Object obj : _extNames.toArrayList()) extNames.add(String.valueOf(obj));

    try {
      taskRunner.executeAsync(new MetadataCallable.ScanAudioFiles(reactContext, dirPath, extNames), promise::resolve);
    } catch (Exception err) {
      promise.reject("-1", err.getMessage());
    }
  }

  @ReactMethod
  public void readMetadata(String filePath, Promise promise) {
    TaskRunner taskRunner = new TaskRunner();
    try {
      taskRunner.executeAsync(new MetadataCallable.ReadMetadata(filePath), promise::resolve);
    } catch (Exception err) {
      promise.reject("-1", err.getMessage());
    }
  }

  @ReactMethod
  public void readPic(String filePath, Promise promise) {
    TaskRunner taskRunner = new TaskRunner();
    try {
      taskRunner.executeAsync(new MetadataCallable.ReadPic(filePath), promise::resolve);
    } catch (Exception err) {
      promise.reject("-1", err.getMessage());
    }
  }

  @ReactMethod
  public void readLyric(String filePath, Promise promise) {
    TaskRunner taskRunner = new TaskRunner();
    try {
      taskRunner.executeAsync(new MetadataCallable.ReadLyric(filePath), promise::resolve);
    } catch (Exception err) {
      promise.reject("-1", err.getMessage());
    }
  }
}
