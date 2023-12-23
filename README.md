# react-native-local-media-metadata

Jaudiotagger library for React Native Android

## Installation

```sh
npm install github:lyswhut/react-native-local-media-metadata
```

## Usage

```js
import {
  readMetadata,
  readPic,
  readLyric,
  writeMetadata,
  writePic,
  writeLyric
} from 'react-native-local-media-metadata';

// ...

const path = '...'
await readMetadata(path).then((metadata) => {
  console.log(metadata)
})
await readPic(path).then((pic) => {
  console.log(pic)
})
await readLyric(path).then((lrc) => {
  console.log(lrc)
})

await writeMetadata(path, metadata, false).then(() => {
  console.log('writeMetadata success')
})
await writePic(path, picPath).then(() => {
  console.log('writePic success')
})
await writeLyric(path, lyric, true).then(() => {
  console.log('writeLyric success')
})
```

## License

MIT
