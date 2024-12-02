package com.localmediametadata.media3;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.container.MdtaMetadataEntry;
import androidx.media3.extractor.metadata.flac.PictureFrame;
import androidx.media3.extractor.metadata.id3.ApicFrame;
import androidx.media3.extractor.metadata.id3.TextInformationFrame;
import androidx.media3.extractor.metadata.vorbis.VorbisComment;

import com.localmediametadata.Utils;

import java.nio.charset.StandardCharsets;

@OptIn(markerClass = UnstableApi.class)
public class Parser {
  /**
   * ID3 Metadata (MP3)
   *
   * https://en.wikipedia.org/wiki/ID3
   */
  private static Bundle handleId3Metadata(Metadata metadata) {
    String title = null, artist = null, album = null, lyric = null, date = null, genre = null;

    for(int i = 0; i < metadata.length(); i++) {
      Metadata.Entry entry = metadata.get(i);

      if (entry instanceof TextInformationFrame id3) {
        // ID3 text tag
        String id = id3.id.toUpperCase();
        Log.d(Utils.LOG, "id3 TextInformationFrame key: " + id + " value: " + id3.values.get(0));

        switch (id) {
          case "TIT2":
          case "TT2":
            title = id3.values.get(0);
            break;
          case "TALB":
          case "TOAL":
          case "TAL":
            album = id3.values.get(0);
            break;
          case "TOPE":
          case "TPE1":
          case "TP1":
            artist = id3.values.get(0);
            break;
          case "TDRC":
          case "TOR":
            date = id3.values.get(0);
            break;
          case "TCON":
          case "TCO":
            genre = id3.values.get(0);
            break;
          case "USLT":
            lyric = id3.values.get(0);
            break;
        }
      } else if ((entry instanceof ApicFrame apicFrame)) {
        Log.d(Utils.LOG, "id3 ApicFrame pictureType: " + apicFrame.pictureType + " pictureData: " + apicFrame.pictureData.length);
      }
    }

    if (title != null || artist != null || album != null) {
      Bundle bundle = new Bundle();
      bundle.putString("name", title);
      bundle.putString("singer", artist);
      bundle.putString("albumName", album);
//      Log.d(Utils.LOG, "id3 title: " + title + " artist: " + artist + " album: " + album);
//      Log.d(Utils.LOG, "id3 lyric: " + lyric);
      return bundle;
    } else return null;
  }

  /**
   * Vorbis Comments (Vorbis, FLAC, Opus, Speex, Theora)
   *
   * https://xiph.org/vorbis/doc/v-comment.html
   */
  private static Bundle handleVorbisCommentMetadata(Metadata metadata) {
    String title = null, artist = null, album = null, lyric = null, date = null, genre = null;

    for (int i = 0; i < metadata.length(); i++) {
      Metadata.Entry entry = metadata.get(i);

      if ((entry instanceof PictureFrame)) {
        PictureFrame pic = (PictureFrame) entry;

        Log.d(Utils.LOG, "vorbis-comment pictureType: " + pic.pictureType + " pictureData: " + pic.pictureData.length);
      }

      if (!(entry instanceof VorbisComment comment)) continue;

      String key = comment.key;
      Log.d(Utils.LOG, "vorbis-comment key: " + key + " value: " + comment.value);

      switch (key) {
        case "TITLE":
          title = comment.value;
          break;
        case "ARTIST":
          artist = comment.value;
          break;
        case "ALBUM":
          album = comment.value;
          break;
        case "DATE":
          date = comment.value;
          break;
        case "GENRE":
          genre = comment.value;
          break;
        case "LYRICS":
          lyric = comment.value;
          break;
      }
    }

    if (title != null || artist != null || album != null || lyric != null) {
      Bundle bundle = new Bundle();
      bundle.putString("name", title);
      bundle.putString("singer", artist);
      bundle.putString("albumName", album);
//      Log.d(Utils.LOG, "vorbis-comment title: " + title + " artist: " + artist + " album: " + album);
//      Log.d(Utils.LOG, "vorbis-comment lyric: " + lyric);
      return bundle;
    } else return null;
  }

  /**
   * QuickTime MDTA metadata (mov, qt)
   *
   * https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/Metadata/Metadata.html
   */
  private static Bundle handleQuickTimeMetadata(Metadata metadata) {
    String title = null, artist = null, album = null, date = null, genre = null;

    for (int i = 0; i < metadata.length(); i++) {
      Metadata.Entry entry = metadata.get(i);

      if (!(entry instanceof MdtaMetadataEntry)) continue;

      MdtaMetadataEntry mdta = (MdtaMetadataEntry) entry;
      String key = mdta.key;

      try {
        switch (key) {
          case "com.apple.quicktime.title":
            title = new String(mdta.value, StandardCharsets.UTF_8);
            break;
          case "com.apple.quicktime.artist":
            artist = new String(mdta.value, StandardCharsets.UTF_8);
            break;
          case "com.apple.quicktime.album":
            album = new String(mdta.value, StandardCharsets.UTF_8);
            break;
          case "com.apple.quicktime.creationdate":
            date = new String(mdta.value, StandardCharsets.UTF_8);
            break;
          case "com.apple.quicktime.genre":
            genre = new String(mdta.value, StandardCharsets.UTF_8);
            break;
        }
      } catch(Exception ex) {
        // Ignored
      }
    }

    if (title != null || artist != null || album != null || date != null || genre != null) {
//      Log.d(Utils.LOG, "vorbis-comment title: " + title + " artist: " + artist + "album: " + album);
      Bundle bundle = new Bundle();
      bundle.putString("name", title);
      bundle.putString("singer", artist);
      bundle.putString("albumName", album);
      return bundle;
    } else return null;
  }

  public static Bundle parseMetadata(Metadata metadata) {
    Bundle result = handleId3Metadata(metadata);
    if (result == null) result = handleVorbisCommentMetadata(metadata);
    if (result == null) result = handleQuickTimeMetadata(metadata);
    if (result == null) result = new Bundle();
    return result;
  }
}
