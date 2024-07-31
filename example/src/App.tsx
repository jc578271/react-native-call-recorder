import * as React from 'react';

import { View, Text, Platform, PermissionsAndroid } from 'react-native';
import {
  onOutgoingCallRecorded,
  removeOutgoingCallRecorded,
  switchRecordStatus,
} from 'react-native-call-recorder';
import { Dirs, FileSystem } from 'react-native-file-access';
import { useCallback } from 'react';

export default function App() {
  const readFile = useCallback(async (filepath: string) => {
    const filenames = filepath.split('/');
    const filename = filenames[filenames.length - 1];

    const file = await FileSystem.cpExternal(filepath, '/' + filename, 'audio');
    console.log(file);
  }, []);

  React.useEffect(() => {
    if (Platform.OS === 'android')
      PermissionsAndroid.requestMultiple([
        'android.permission.READ_PHONE_STATE',
        'android.permission.READ_CALL_LOG',
        'android.permission.RECORD_AUDIO',
        'android.permission.WRITE_EXTERNAL_STORAGE',
        'android.permission.READ_EXTERNAL_STORAGE',
        'android.permission.READ_MEDIA_AUDIO',
      ]).then();
    switchRecordStatus(true);

    console.log(JSON.stringify(Dirs, null, 4));
    onOutgoingCallRecorded(({ filePath, end, number, start }) => {
      // setTimeout(() => {
      console.log(
        JSON.stringify({
          filePath,
          end,
          number,
          start,
        })
      );
      readFile(filePath).then();
      // }, 1000);
    });
    // RNCallRecorder.addPhonesWhiteList(['1234'])
    // RNCallRecorder.switchRecordStatus();

    return () => {
      removeOutgoingCallRecorded();
    };
  }, []);
  return (
    <View>
      <Text>Ok</Text>
    </View>
  );
}
