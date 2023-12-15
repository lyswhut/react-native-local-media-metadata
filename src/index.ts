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
  albumName: string
  singer: string
  name: string
}

export interface MusicMetadataFull {
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
export const readMetadata = async(filePath: string): Promise<MusicMetadataFull | null> => {
  return LocalMediaMetadata.readMetadata(filePath)
}
/**
 * Write Metadata
 * @param filePath
 * @param metadata
 * @param isOverwrite
 * @returns
 */
export const writeMetadata = async(filePath: string, metadata: MusicMetadata, isOverwrite = false): Promise<void> => {
  return LocalMediaMetadata.writeMetadata(filePath, metadata, isOverwrite)
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
 * Write Pic
 * @param filePath
 * @param picPath
 * @returns
 */
export const writePic = async(filePath: string, picPath: string): Promise<void> => {
  return LocalMediaMetadata.writePic(filePath, picPath)
}

/**
 * Read Lyric
 * @param filePath
 * @returns
 */
export const readLyric = async(filePath: string): Promise<string> => {
  return LocalMediaMetadata.readLyric(filePath)
}
/**
 * Write Lyric
 * @param filePath
 * @param lyric
 * @returns
 */
export const writeLyric = async(filePath: string, lyric: string): Promise<void> => {
  return LocalMediaMetadata.writeLyric(filePath, lyric)
}

