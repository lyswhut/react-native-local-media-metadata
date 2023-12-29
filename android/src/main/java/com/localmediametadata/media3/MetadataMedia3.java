package com.localmediametadata.media3;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.MetadataRetriever;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.extractor.metadata.flac.PictureFrame;
import androidx.media3.extractor.metadata.id3.ApicFrame;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.localmediametadata.Utils;

import org.jaudiotagger.tag.id3.valuepair.ImageFormats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

@OptIn(markerClass = UnstableApi.class)
public class MetadataMedia3 {
  private static class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
      new Thread(r).start();
    }
  }
//  private static class DirectExecutor implements Executor {
//    public void execute(Runnable r) {
//      r.run();
//    }
//  }
//  public static void getMetadata(ReactApplicationContext context, String uri) {
//    ListenableFuture<TrackGroupArray> trackGroupsFuture = MetadataRetriever.retrieveMetadata(context, MediaItem.fromUri(uri));
//    Futures.addCallback(trackGroupsFuture, new FutureCallback<>() {
//      @Override
//      public void onSuccess(TrackGroupArray trackGroups) {
//        Log.d("Metadata", "trackGroups length: " + trackGroups.length);
//        for (int i = 0; i < trackGroups.length; i++) {
//          Log.d("Metadata", "index: " + i + " format length: " + trackGroups.get(i).length);
//          Metadata metadata = trackGroups.get(i).getFormat(0).metadata;
//          if (metadata == null) continue;
//          Log.d("Metadata", "index: " + i + " metadata length: " + metadata.length());
//          for(int i2 = 0; i2 < metadata.length(); i2++) {
//            Metadata.Entry entry = metadata.get(i2);
//            if ((entry instanceof ApicFrame pic)) {
//              Log.d("Metadata", "id3 ApicFrame pictureType: " + pic.pictureType + " pictureData: " + pic.pictureData.length);
//            } else if (entry instanceof TextInformationFrame id3) {
//              Log.d("Metadata", "id3 TextInformationFrame key: " + id3.id.toUpperCase() + " value: " + id3.values.get(0));
//            } else if (entry instanceof BinaryFrame binaryFrame) {
//              Log.d("Metadata", "id3 BinaryFrame key: " + binaryFrame.id + " data: " + binaryFrame.data.length);
//              if ("USLT".equalsIgnoreCase(binaryFrame.id)) Log.d("Metadata", "id3 UNSYNCEDLYRICS: " + new String(binaryFrame.data, StandardCharsets.UTF_8));
//            }
//          }
//        }
//      }
//      @Override
//      public void onFailure(@NonNull Throwable t) {
//        Log.d("Metadata", "error: " + t);
//      }
//    }, new DirectExecutor());
//  }
//  public static void getMetadata(ReactApplicationContext context, String uri) {
//    ListenableFuture<TrackGroupArray> trackGroupsFuture = MetadataRetriever.retrieveMetadata(context, MediaItem.fromUri(uri));
//    Futures.addCallback(trackGroupsFuture, new FutureCallback<>() {
//      @Override
//      public void onSuccess(TrackGroupArray trackGroups) {
//        Log.d(Utils.LOG, "trackGroups length: " + trackGroups.length);
//        for (int i = 0; i < trackGroups.length; i++) {
//          Format format = trackGroups.get(i).getFormat(0);
//          Metadata metadata = format.metadata;
//          if (metadata == null) continue;
//          Log.d(Utils.LOG, "index: " + i + " getFormatlength: " + trackGroups.get(i).length + " metadata: " + metadata.length());
////          Bundle result = Parser.parseMetadata(metadata);
//
////          result.putDouble("interval", audioHeader.getTrackLength());
////          result.putString("bitrate", audioHeader.getBitRate());
////          result.putString("type", audioHeader.getEncodingType());
////          result.putString("ext", Utils.getFileExtension(file.getName()));
////          result.putDouble("size", file.size());
////          callback.resolve(null);
//        }
//      }
//
//      @Override
//      public void onFailure(@NonNull Throwable t) {
//        Log.d(Utils.LOG, "error: " + t);
////        callback.resolve(null);
//      }
//    }, new ThreadPerTaskExecutor());
//  }

  private static String writePic(ReactApplicationContext context, String filePath, String picDir, String mimeType, byte[] data) {
    // Log.d(Utils.LOG, "mimeType: " + mimeType + " data: " + data.length);
    File dir = new File(picDir);
    if (!dir.exists() && !dir.mkdirs()) return "";
    DocumentFile dFile = DocumentFile.fromSingleUri(context, Uri.parse(filePath));
    String ext = ImageFormats.getFormatForMimeType(mimeType);
    ext = ext == null ? "jpg" : ext.toLowerCase();
    File picFile = new File(picDir, Utils.getName(dFile.getName()) + "." + ext);
    try (FileOutputStream fos = new FileOutputStream(picFile)) {
      fos.write(data);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
    return picFile.getPath();
  }
  public static void readPic(ReactApplicationContext context, String uri, String picDir, Promise promise) {
    ListenableFuture<TrackGroupArray> trackGroupsFuture = MetadataRetriever.retrieveMetadata(context, MediaItem.fromUri(uri));
    Futures.addCallback(trackGroupsFuture, new FutureCallback<>() {
      @Override
      public void onSuccess(TrackGroupArray trackGroups) {
        for (int i = 0; i < trackGroups.length; i++) {
          Format format = trackGroups.get(i).getFormat(0);
          Metadata metadata = format.metadata;
          if (metadata == null) continue;
          for(int i2 = 0; i2 < metadata.length(); i2++) {
            Metadata.Entry entry = metadata.get(i2);
            if (entry instanceof ApicFrame) {
              ApicFrame pic = (ApicFrame) entry;
              if ("APIC".equals(pic.id) && pic.pictureType == 3) {
                promise.resolve(writePic(context, uri, picDir, pic.mimeType, pic.pictureData));
                return;
              }
            } else if (entry instanceof PictureFrame) {
              PictureFrame pic = (PictureFrame) entry;
              if (pic.pictureType == 3) {
                promise.resolve(writePic(context, uri, picDir, pic.mimeType, pic.pictureData));
                return;
              }
            }
          }
          promise.resolve("");
        }
      }

      @Override
      public void onFailure(@NonNull Throwable t) {
        Log.d(Utils.LOG, "error: " + t);
        promise.reject("-1", "read failed: " + t.getMessage());
      }
    }, new ThreadPerTaskExecutor());
  }
//  public static void readLyric(ReactApplicationContext context, String uri, Promise promise) {
//    ListenableFuture<TrackGroupArray> trackGroupsFuture = MetadataRetriever.retrieveMetadata(context, MediaItem.fromUri(uri));
//    Futures.addCallback(trackGroupsFuture, new FutureCallback<>() {
//      @Override
//      public void onSuccess(TrackGroupArray trackGroups) {
//        for (int i = 0; i < trackGroups.length; i++) {
//          Format format = trackGroups.get(i).getFormat(0);
//          Metadata metadata = format.metadata;
//          if (metadata == null) continue;
//          for(int i2 = 0; i2 < metadata.length(); i2++) {
//            Metadata.Entry entry = metadata.get(i2);
//            if ((entry instanceof VorbisComment comment) && "LYRICS".equalsIgnoreCase(comment.key)) {
//              promise.resolve(comment.value);
//              return;
//            }
//          }
//          promise.resolve("");
//        }
//      }
//
//      @Override
//      public void onFailure(@NonNull Throwable t) {
//        Log.d(Utils.LOG, "error: " + t);
//        promise.reject("-1", "read failed: " + t.getMessage());
//      }
//    }, new ThreadPerTaskExecutor());
//  }
}
