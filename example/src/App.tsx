import * as React from 'react';

import { StyleSheet, ScrollView, Text, Image } from 'react-native';
import { scanFiles, readMetadata, readPic, readLyric } from 'react-native-local-media-metadata';

export default function App() {
  const [result, setResult] = React.useState<string[]>([]);
  const [metadata, setMetadata] = React.useState<string>('');
  const [pic, setPic] = React.useState<string>('');
  const [lyric, setLyric] = React.useState<string>('');

  React.useEffect(() => {
    scanFiles('/storage/emulated/0/Pictures', ['mp3', 'flac']).then(paths => {
      console.log(paths)
      setResult(paths)
      const path = paths[0]
      if (path) {
        readMetadata(path).then((metadata) => {
          setMetadata(JSON.stringify(metadata, null, 2))
        })
        readPic(path).then(setPic)
        readLyric(path).then(setLyric)
      }
    });
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
