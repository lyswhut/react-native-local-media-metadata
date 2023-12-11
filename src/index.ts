import { NativeModules } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-local-media-metadata' doesn't seem to be linked. Make sure: \n\n` +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const LocalMediaMetadata = NativeModules.LocalMediaMetadata
  ? NativeModules.LocalMediaMetadata
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

/**
 * Read dir files
 * @param dirPath
 * @param extNames
 * @returns
 */
export const scanFiles = async(dirPath: string, extNames: string[]): Promise<string[]> => {
  return LocalMediaMetadata.scanFiles(dirPath, extNames)
}

export interface MusicMetadata {
  type: 'mp3' | 'flac' | 'ogg' | 'wav'
  bitrate: string
  interval: number
  size: number
  ext: 'mp3' | 'flac' | 'ogg' | 'wav'
  albumName: string
  singer: string
  name: string
}

/**
 * Read Metadata
 * @param filePath
 * @returns
 */
export const readMetadata = async(filePath: string): Promise<MusicMetadata | null> => {
  return LocalMediaMetadata.readMetadata(filePath)
}

/**
 * Read Pic
 * @param filePath
 * @returns
 */
export const readPic = async(filePath: string): Promise<string> => {
  return LocalMediaMetadata.readPic(filePath)
}

/**
 * Read Lyric
 * @param filePath
 * @returns
 */
export const readLyric = async(filePath: string): Promise<string> => {
  return LocalMediaMetadata.readLyric(filePath)
}

