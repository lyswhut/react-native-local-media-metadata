package com.localmediametadata;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.facebook.react.bridge.ReactApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MediaFile {
  final ReactApplicationContext context;
  private File file = null;
  private DocumentFile dFile = null;
  private ParcelFileDescriptor parcelFileDescriptor = null;
  private File tempFile;
  private boolean isWrite;
  MediaFile(ReactApplicationContext context, String path) {
    this.context = context;
    if (Utils.isContentUri(path)) {
      try {
        Uri uri = Uri.parse(path);
        DocumentFile df = Utils.isTreeUri(uri)
          ? DocumentFile.fromTreeUri(context, uri)
          : DocumentFile.fromSingleUri(context, uri);
        if (df != null) {
          this.dFile = df;
          return;
        }
      } catch (Exception ignored) {}
    }
    this.file = Utils.parsePathToFile(path);
  }

  public boolean isDocFile() {
    return this.file == null;
  }
  private String createPath(String name) {
    return context.getCacheDir().getAbsolutePath() + "/local-media-metadata-temp-file/" + UUID.randomUUID() + "." + Utils.getFileExtension(name);
  }

  private File createTempFile() throws IOException {
    this.tempFile = new File(createPath(isDocFile() ? dFile.getName() : file.getName()));
    Log.d("MediaFile", "creating temp file: " + tempFile.getAbsolutePath());
    try (InputStream inputStream = isDocFile() ? Utils.createInputStream(context, dFile) : Utils.createInputStream(file);
         OutputStream outputStream = Utils.createOutputStream(tempFile)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, length);
      }
    }
    return tempFile;
  }
  private File createFileFromDocumentFile(boolean isWrite) throws IOException {
    if (!dFile.exists()) return null;
    String name = dFile.getName();
    try {
      ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(dFile.getUri(), isWrite ? "rw" : "r");
      String linkFileName = "/proc/self/fd/" + parcelFileDescriptor.getFd();
      File file = new DescriptorFile(linkFileName, name);
      if (file.canRead() && (!isWrite || file.canWrite())) return file;
    } catch (Exception ignored) { closeFile(); }
    return createTempFile();
  }
  private File createFile(boolean isWrite) throws IOException {
    if (!isWrite || file.canWrite()) return file;
    return createTempFile();
  }
  public File getFile(boolean isWrite) throws IOException {
    this.isWrite = isWrite;
    return isDocFile()
      ? this.createFileFromDocumentFile(isWrite)
      : this.createFile(isWrite);
  }
  public File getTempFile() throws IOException {
    return createTempFile();
  }
  public void closeFile() throws IOException {
    if (parcelFileDescriptor != null) {
      try {
        parcelFileDescriptor.close();
      } catch (Exception ignored) {}
      parcelFileDescriptor = null;
    }
    if (tempFile != null) {
      Log.d("MediaFile", "closeFile");
      if (isWrite) {
        try (InputStream inputStream = Utils.createInputStream(tempFile);
             OutputStream outputStream = isDocFile()
               ? Utils.createOutputStream(context, this.dFile.getUri())
               : Utils.createOutputStream(this.file)) {
          byte[] buffer = new byte[1024];
          int length;
          while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
          }
        }
      }
      try {
        tempFile.delete();
      } catch (Exception ignored) {}
      tempFile = null;
    }
  }

  public boolean exists() {
    return isDocFile()
      ? this.dFile.exists()
      : this.file.exists();
  }
  public String getName() {
    return isDocFile()
      ? this.dFile.getName()
      : this.file.getName();
  }
  public long size() {
    return isDocFile()
      ? this.dFile.length()
      : this.file.length();
  }
}
