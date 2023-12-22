package com.localmediametadata;

import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;

public class Metadata {
  private static WritableMap buildMetadata(MediaFile file, AudioHeader audioHeader, Tag tag) {
    WritableMap params = Arguments.createMap();
    String name = tag.getFirst(FieldKey.TITLE);
    if ("".equals(name)) name = Utils.getName(file.getName());
    params.putString("name", name);
    params.putString("singer", tag.getFirst(FieldKey.ARTIST).replaceAll("\\u0000", "、"));
    params.putString("albumName", tag.getFirst(FieldKey.ALBUM));
    params.putDouble("interval", audioHeader.getTrackLength());
    params.putString("bitrate", audioHeader.getBitRate());
    params.putString("type", audioHeader.getEncodingType());
    params.putString("ext", Utils.getFileExtension(file.getName()));
    params.putDouble("size", file.size());

    return params;
  }

  static public WritableMap readMetadata(ReactApplicationContext context, String filePath) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    File file = mediaFile.getFile(false);
    try {
      AudioFile audioFile = AudioFileIO.read(file);
      return buildMetadata(mediaFile, audioFile.getAudioHeader(), audioFile.getTagOrCreateDefault());
    } finally {
      mediaFile.closeFile();
    }
  }

  static public void writeMetadata(ReactApplicationContext context, String filePath, Bundle metadata, boolean isOverwrite) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    File file = mediaFile.getFile(true);

    try {
      AudioFile audioFile = AudioFileIO.read(file);
      Tag tag;
      if (isOverwrite) {
        tag = audioFile.createDefaultTag();
        audioFile.setTag(tag);
      } else tag = audioFile.getTagOrCreateAndSetDefault();
      tag.setField(FieldKey.TITLE, metadata.getString("name", ""));
      tag.setField(FieldKey.ARTIST, metadata.getString("singer", ""));
      tag.setField(FieldKey.ALBUM, metadata.getString("albumName", ""));
      audioFile.commit();
    } finally {
      mediaFile.closeFile();
    }
  }

  public static String readPic(ReactApplicationContext context, String filePath, String picDir) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    File file = mediaFile.getFile(false);
    try {
      AudioFile audioFile = AudioFileIO.read(file);
      Artwork artwork = audioFile.getTagOrCreateDefault().getFirstArtwork();
      if (artwork == null) return "";
      if (artwork.isLinked()) return artwork.getImageUrl();

      File dir = new File(picDir);
      if (!dir.exists() && !dir.mkdirs()) throw new Exception("Directory does not exist");
      File picFile = new File(picDir, Utils.getName(file.getName()) + "." + ImageFormats.getFormatForMimeType(artwork.getMimeType()).toLowerCase());
      try (FileOutputStream fos = new FileOutputStream(picFile)) {
        fos.write(artwork.getBinaryData());
      }
      return picFile.getPath();
    } finally {
      mediaFile.closeFile();
    }
  }

  public static void writeFlacPic(AudioFile audioFile, Artwork artwork) throws Exception {
    FlacTag tag = (FlacTag) audioFile.getTagOrCreateAndSetDefault();
    TagField tagField = tag.createArtworkField(artwork.getBinaryData(),
      artwork.getPictureType(),
      artwork.getMimeType(),
      artwork.getDescription(),
      artwork.getWidth(),
      artwork.getHeight(),
      0,
      "image/jpeg".equals(artwork.getMimeType()) ? 24 : 32
    );
    tag.setField(tagField);
    try {
      audioFile.commit();
    } catch (Exception e) {
      if (e.getMessage().contains("permissions")) {
        tag.deleteArtworkField();
        audioFile.commit();
        tag.setField(tagField);
        audioFile.commit();
      } else throw e;
    }
  }
  public static void writePic(ReactApplicationContext context, String filePath, String picPath) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    File file = mediaFile.getFile(true);
    try {
      AudioFile audioFile = AudioFileIO.read(file);
      if ("".equals(picPath)) {
        audioFile.getTagOrCreateAndSetDefault().deleteArtworkField();
        audioFile.commit();
        return;
      }
      Artwork artwork = ArtworkFactory.createArtworkFromFile(new File(picPath));
      if ("flac".equalsIgnoreCase(Utils.getFileExtension(filePath))) {
        writeFlacPic(audioFile, artwork);
      } else {
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        tag.setField(artwork);
        try {
          audioFile.commit();
        } catch (Exception e) {
          if (e.getMessage().contains("permissions")) {
            tag.deleteArtworkField();
            audioFile.commit();
            tag.setField(artwork);
            audioFile.commit();
          } else throw e;
        }
      }
    } finally {
      mediaFile.closeFile();
    }
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
  public static String readLyricFile(File lrcFile) {
    try {
      FileInputStream fileInputStream = new FileInputStream(lrcFile);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead);
      }
      fileInputStream.close();
      return decodeString(byteArrayOutputStream.toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  public static String readLyric(ReactApplicationContext context, String filePath, boolean isReadLrcFile) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    MediaFile lrcMediaFile = isReadLrcFile ? new MediaFile(context, filePath.substring(0, filePath.lastIndexOf(".")) + ".lrc") : null;
    File file = mediaFile.getFile(false);
    try {
      if (isReadLrcFile && lrcMediaFile.exists()) {
        String lrc = readLyricFile(lrcMediaFile.getFile(false));
        if (!"".equals(lrc)) return lrc;
      }

      org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(file);
      Tag tag = audioFile.getTagOrCreateDefault();
      return tag.getFirst(FieldKey.LYRICS);
    } finally {
      mediaFile.closeFile();
    }
  }

  public static void writeLyric(ReactApplicationContext context, String filePath, String lyric) throws Exception {
    MediaFile mediaFile = new MediaFile(context, filePath);
    File file = mediaFile.getFile(true);
    try {
      AudioFile audioFile = AudioFileIO.read(file);
      Tag tag = audioFile.getTagOrCreateAndSetDefault();
      if ("".equals(lyric)) tag.deleteField(FieldKey.LYRICS);
      else tag.setField(FieldKey.LYRICS, lyric);
      audioFile.commit();
    } finally {
      mediaFile.closeFile();
    }
  }

}
