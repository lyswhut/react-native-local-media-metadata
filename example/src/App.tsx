import * as React from 'react';

import { StyleSheet, ScrollView, Text, Image } from 'react-native';
import { scanFiles, readMetadata, readPic, readLyric, writeMetadata, writePic, writeLyric } from 'react-native-local-media-metadata';
import { writeFile } from 'react-native-fs';
import { requestStoragePermission } from './utils';

export default function App() {
  const [result, setResult] = React.useState<string[]>([]);
  const [metadata, setMetadata] = React.useState<string>('');
  const [pic, setPic] = React.useState<string>('');
  const [lyric, setLyric] = React.useState<string>('');

  React.useEffect(() => {
    requestStoragePermission().then((result) => {
      console.log(result)
      scanFiles('/storage/emulated/0/Pictures', ['mp3', 'flac']).then(async paths => {
        // console.log(paths)
        setResult(paths)
        const path = paths[0]
        if (!path) return
        console.log(path)
        let picPath = ''
        let lyric = ''
        const [metadata] = await Promise.all([
          readMetadata(path).then((metadata) => {
            setMetadata(JSON.stringify(metadata, null, 2))
            return metadata
          }),
          readPic(path).then(async(pic) => {
            setPic(pic)
            if (!pic) return
            picPath = path.substring(0, path.lastIndexOf('.') + 1) + pic.split(';')[0]?.split('/')[1]
            await writeFile(picPath, pic.split(',')[1]!, 'base64')
            return pic
          }),
          readLyric(path).then(_lyric => {
            setLyric(_lyric)
            lyric = _lyric
          })
        ])
        console.log(picPath)
        if (metadata) {
          await writeMetadata(path, metadata, true).then(() => {
            console.log('writeMetadata success')
          })
          if (picPath) {
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
