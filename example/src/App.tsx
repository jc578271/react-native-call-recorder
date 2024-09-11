import * as React from 'react';

import { View, Text, Platform, PermissionsAndroid, Button } from 'react-native';
import {
  downloadAndLoadModel,
  onOutgoingCallRecorded,
  openAccessibilitySettings,
  openSpecificAccessibilitySettings,
  removeOutgoingCallRecorded,
  switchRecordStatus,
  transcribeWav,
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
      <Button
        title={'open'}
        onPress={() => {
          openAccessibilitySettings();
        }}
      />
      <Button
        title={'openSpecificAccessibilitySettings'}
        onPress={() => {
          openSpecificAccessibilitySettings();
        }}
      />
      <Button
        title={'download'}
        onPress={async () => {
          console.log('download');
          try {
            await downloadAndLoadModel(
              'https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip'
            );
          } catch (e) {
            console.log(e);
          }

          console.log('done');
        }}
      />
      <Button
        title={'convert to text'}
        onPress={async () => {
          console.log('convert to text');
          const a = await transcribeWav(
            '/data/user/0/com.callrecorderexample/files/record-outgoing-1726025791422.wav'
          );
          console.log('a', a);
        }}
      />
    </View>
  );
}
