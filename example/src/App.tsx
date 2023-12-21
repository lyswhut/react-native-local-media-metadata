import * as React from 'react';

import { StyleSheet, ScrollView, Text, Image } from 'react-native';
import { readMetadata, readPic, readLyric, writeMetadata, writePic, writeLyric } from 'react-native-local-media-metadata';
import { Dirs, FileSystem } from 'react-native-file-system/src/index';
// import { Dirs } from 'react-native-file-access';
// import { openDocumentTree, getPersistedUriPermissions, listFiles } from 'react-native-scoped-storage';
import { requestStoragePermission } from './utils';

// console.log(Dirs)
const dirPath = 'content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures'
const filePath = 'content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures%2FAndy%20-%20%E9%98%BF%E6%9D%9C.mp3'
// const filePath = 'content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures%2F%E9%92%B1%E5%A5%B3%E5%8F%8B%20-%20%E8%92%8B%E8%92%8B%E3%80%81%E5%B0%8F%E9%AC%BC%E3%80%81%E4%B9%94%E6%B4%8B.flac'

const handleDir = async() => {
  // let dir = await openDocumentTree(true);
  // console.log(dir)
  // const dirs = await getPersistedUriPermissions()
  // console.log(dirs)
  // if (!dirs.includes(dirPath)) return
  // console.log('await listFiles(dirPath)')
  // console.log((await listFiles(dirPath)).map(u => `${u.name}  ${u.uri}`).join('\n'))
  // console.log(await readMetadata(filePath).catch(e => console.log(e)))
  // console.log(await readPic(filePath, Dirs.CacheDir + '/local-media-pic').catch(e => console.log(e)))
}

export default function App() {
  const [result, setResult] = React.useState<string[]>([]);
  const [metadata, setMetadata] = React.useState<string>('');
  const [pic, setPic] = React.useState<string>('');
  const [lyric, setLyric] = React.useState<string>('');

  React.useEffect(() => {
    requestStoragePermission().then(async(result) => {
      console.log(result)
      // getAllStorages().then((storages) => {
      //   setResult(storages)
      // })
      // getExternalStoragePath().then((storage: string | null) => {
      //   setMetadata(storage ?? '')
      // })
      void handleDir()

      readPic(filePath, Dirs.CacheDir + '/local-media-pic').then(async(picPath) => {
        setPic('file://' + picPath)
        console.log('picPath', picPath)
        if (!picPath) return
        await writePic(filePath, '').then(() => {
          console.log('writePic success')
        })
        await writePic(filePath, picPath).then(() => {
          console.log('writePic success')
        })
      }).catch(err => {
        console.log(err)
      })

      FileSystem.ls(dirPath).then(async files => {
      // //   // console.log(dir)
      // //   // console.log(paths)
        const paths = files.filter(file => /\.(mp3|flac)$/i.test(file.name)).slice(0, 10)
        setResult(paths.map(f => f.name))
        const path = paths[0]?.path
        // let path = filePath
        if (!path) return
        console.log(path)
        let lyric = ''
        const [metadata, picPath] = await Promise.all([
          readMetadata(path).then((metadata) => {
            setMetadata(JSON.stringify(metadata, null, 2))
            return metadata
          }),
          readPic(path, Dirs.CacheDir + '/local-media-pic').then(async(picPath) => {
            setPic('file://' + picPath)
            return picPath
          }),
          readLyric(path).then(_lyric => {
            setLyric(_lyric)
            lyric = _lyric
          })
        ])
        console.log('picPath', metadata, picPath)
        if (metadata) {
          await writeMetadata(path, metadata, true).then(() => {
            console.log('writeMetadata success')
          })
          if (picPath) {
            await writePic(path, '').then(() => {
              console.log('writePic success')
            })
            await writePic(path, picPath).then(() => {
              console.log('writePic success')
            })
          }
          if (lyric) {
            await writeLyric(path, lyric).then(() => {
              console.log('writeLyric success')
            })
          }
        }
      });
    })
  }, []);

  return (
    <ScrollView style={styles.container}>
      <Text>Result: {'\n' + result.join('\n')}</Text>
      <Text>Metadata: {'\n' + metadata}</Text>
      { pic ? <Image style={{ width: 200, height: 200 }} source={{ uri: pic }} /> : null }
      { lyric ? <Text>Lyric: {'\n' + lyric}</Text> : null }
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    // alignItems: 'center',
    // justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
