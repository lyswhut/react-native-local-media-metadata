# react-native-local-media-metadata

Jaudiotagger library for React Native Android

## Installation

```sh
npm install github:lyswhut/react-native-local-media-metadata
```

## Usage

```js
import {
  scanFiles,
  readMetadata,
  readPic,
  readLyric,
  writeMetadata,
  writePic,
  writeLyric
} from 'react-native-local-media-metadata';

// ...

scanFiles('/storage/emulated/0/Pictures', ['mp3', 'flac']).then(async paths => {
  console.log(paths)
  const path = paths[0]
  if (path) {
    await readMetadata(path).then((metadata) => {
      console.log(metadata)
    })
    await readPic(path).then((pic) => {
      console.log(pic)
    })
    await readLyric(path).then((lrc) => {
      console.log(lrc)
    })

    await writeMetadata(path, metadata, true).then(() => {
      console.log('writeMetadata success')
    })
    await writePic(path, picPath).then(() => {
      console.log('writePic success')
    })
    await writeLyric(path, lyric).then(() => {
      console.log('writeLyric success')
    })
  }
});
```

## License

MIT
