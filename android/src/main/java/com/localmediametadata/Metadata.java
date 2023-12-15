package com.localmediametadata;

import android.os.Bundle;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Metadata {
  private static String getFileName(File file) {
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    } else {
      return fileName;
    }
  }
  private static String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex + 1);
    } else {
      return "";
    }
  }
  private static WritableMap buildMetadata(File file, AudioHeader audioHeader, Tag tag) {
    WritableMap params = Arguments.createMap();
    String name = tag.getFirst(FieldKey.TITLE);
    if ("".equals(name)) name = getFileName(file);
    params.putString("name", name);
    params.putString("singer", tag.getFirst(FieldKey.ARTIST).replaceAll("\\u0000", "、"));
    params.putString("albumName", tag.getFirst(FieldKey.ALBUM));
    params.putDouble("interval", audioHeader.getTrackLength());
    params.putString("bitrate", audioHeader.getBitRate());
    params.putString("type", audioHeader.getEncodingType());
    params.putString("ext", getFileExtension(file.getName()));
    params.putDouble("size", file.length());

    return params;
  }

  static public WritableMap readMetadata(String filePath) throws Exception {
    File file = new File(filePath);
    AudioFile audioFile = AudioFileIO.read(file);
    return buildMetadata(file, audioFile.getAudioHeader(), audioFile.getTagOrCreateDefault());
  }

  static public void writeMetadata(String filePath, Bundle metadata, boolean isOverwrite) throws Exception {
    File file = new File(filePath);
    AudioFile audioFile = AudioFileIO.read(file);

    Tag tag;
    if (isOverwrite) {
      tag = audioFile.createDefaultTag();
      audioFile.setTag(tag);
    } else tag = audioFile.getTagOrCreateDefault();
    tag.setField(FieldKey.TITLE, metadata.getString("name", ""));
    tag.setField(FieldKey.ARTIST, metadata.getString("singer", ""));
    tag.setField(FieldKey.ALBUM, metadata.getString("albumName", ""));
    audioFile.commit();
  }

  private static String encodeBase64(byte[] data) {
    return new String(Base64.encode(data, Base64.NO_WRAP), StandardCharsets.UTF_8);
  }
  public static String readPic(String filePath) throws Exception {
    File file = new File(filePath);
    AudioFile audioFile = AudioFileIO.read(file);
    Artwork artwork = audioFile.getTagOrCreateDefault().getFirstArtwork();
    if (artwork == null) return "";
    if (artwork.isLinked()) return artwork.getImageUrl();
    return "data:" + artwork.getMimeType() +
      ";base64," + encodeBase64(artwork.getBinaryData());
  }

  public static void writeFlacPic(AudioFile audioFile, String picPath) throws Exception {
    FlacTag tag = (FlacTag) audioFile.getTagOrCreateDefault();
    Artwork artwork = ArtworkFactory.createArtworkFromFile(new File(picPath));
    tag.setField(tag.createArtworkField(artwork.getBinaryData(),
      artwork.getPictureType(),
      artwork.getMimeType(),
      artwork.getDescription(),
      artwork.getWidth(),
      artwork.getHeight(),
      0,
      "image/jpeg".equals(artwork.getMimeType()) ? 24 : 32
    ));
    audioFile.commit();
  }
  public static void writePic(String filePath, String picPath) throws Exception {
    File file = new File(filePath);
    AudioFile audioFile = AudioFileIO.read(file);
    if ("flac".equalsIgnoreCase(getFileExtension(filePath))) {
      writeFlacPic(audioFile, picPath);
    } else {
      Tag tag = audioFile.getTagOrCreateDefault();
      tag.setField(ArtworkFactory.createArtworkFromFile(new File(picPath)));
      audioFile.commit();
    }
  }

  public static String decodeString(byte[] data) {
    UniversalDetector detector = new UniversalDetector(null);
    detector.handleData(data, 0, data.length);
    detector.dataEnd();
    String detectedCharset = detector.getDetectedCharset();
    detector.reset();
    try {
      return new String(data, detectedCharset);
    } catch (Exception e) {
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
      return "";
    }
  }
  public static String readLyric(String filePath) throws Exception {
    File file = new File(filePath);
    File lrcFile = new File(file.getParent() + "/" + getFileName(file) + ".lrc");
    if (lrcFile.exists()) {
      String lrc = readLyricFile(lrcFile);
      if (!"".equals(lrc)) return lrc;
    }

    AudioFile audioFile = AudioFileIO.read(file);
    Tag tag = audioFile.getTagOrCreateDefault();
    return tag.getFirst(FieldKey.LYRICS);
  }

  public static void writeLyric(String filePath, String lyric) throws Exception {
    File file = new File(filePath);
    AudioFile audioFile = AudioFileIO.read(file);
    Tag tag = audioFile.getTagOrCreateDefault();
    tag.setField(FieldKey.LYRICS, lyric);
    audioFile.commit();
  }

}
