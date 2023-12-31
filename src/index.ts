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

const writeQueue = new Map<string, { promise: Promise<void>, num: number }>()
const waitQueuePromise = async(key: string, run: () => Promise<void>): Promise<void> => {
  let task = writeQueue.get(key)
  if (!task) task = { promise: Promise.resolve(), num: 0 }
  task.num++
  task.promise = task.promise.finally(run).finally(() => {
    let task = writeQueue.get(key)
    if (!task) return
    task.num--
    if (task.num > 0) writeQueue.set(key, task)
    else writeQueue.delete(key)
  })
  writeQueue.set(key, task)
  return task.promise
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
  return waitQueuePromise(filePath, () => {
    return LocalMediaMetadata.writeMetadata(filePath, metadata, isOverwrite)
  })
}

/**
 * Read Pic
 * @param filePath
 * @param picDir
 * @returns filePath
 */
export const readPic = async(filePath: string, picDir: string): Promise<string> => {
  return LocalMediaMetadata.readPic(filePath, picDir)
}
/**
 * Write Pic
 * @param filePath
 * @param picPath
 * @returns
 */
export const writePic = async(filePath: string, picPath: string): Promise<void> => {
  return waitQueuePromise(filePath, () => {
    return LocalMediaMetadata.writePic(filePath, picPath)
  })
}

/**
 * Read Lyric
 * @param filePath
 * @param isReadLrcFile
 * @returns
 */
export const readLyric = async(filePath: string, isReadLrcFile: boolean = true): Promise<string> => {
  return LocalMediaMetadata.readLyric(filePath, isReadLrcFile)
}
/**
 * Write Lyric
 * @param filePath
 * @param lyric
 * @returns
 */
export const writeLyric = async(filePath: string, lyric: string): Promise<void> => {
  return waitQueuePromise(filePath, () => {
    return LocalMediaMetadata.writeLyric(filePath, lyric)
  })
}

