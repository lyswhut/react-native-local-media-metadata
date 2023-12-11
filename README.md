# react-native-local-media-metadata

Jaudiotagger library for React Native Android

## Installation

```sh
npm install react-native-local-media-metadata
```

## Usage

```js
import { scanFiles, readMetadata, readPic, readLyric } from 'react-native-local-media-metadata';

// ...

scanFiles('/storage/emulated/0/Pictures', ['mp3', 'flac']).then(paths => {
  console.log(paths)
  const path = paths[0]
  if (path) {
    readMetadata(path).then((metadata) => {
      console.log(metadata)
    })
    readPic(path).then((pic) => {
      console.log(pic)
    })
    readLyric(path).then((lrc) => {
      console.log(lrc)
    })
  }
});
```

## License

MIT
