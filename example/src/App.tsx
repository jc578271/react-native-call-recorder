import * as React from 'react';

import {
  View,
  Text,
  Platform,
  PermissionsAndroid,
  Button,
  // FlatList,
} from 'react-native';
import {
  // downloadAndLoadModel,
  getCurrentModel,
  getModelAvailable,
  // getModels,
  // loadModel,
  onOutgoingCallRecorded,
  openAccessibilitySettings,
  openSpecificAccessibilitySettings,
  removeOutgoingCallRecorded,
  switchRecordStatus,
  // transcribeWav,
} from 'react-native-call-recorder';
import { Dirs, FileSystem } from 'react-native-file-access';
import { useCallback, useEffect, useState } from 'react';

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
        // 'android.permission.POST_NOTIFICATIONS',
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

  const irl =
    'https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip';

  const [modelAvailable, setModelAvailable] = useState(false);
  const [currentModel, setCurrentModel] = useState('');
  // const [models, setModels] = useState<string[]>([]);
  useEffect(() => {
    getModelAvailable(irl).then((res) => {
      setModelAvailable(res);
    });
    getCurrentModel().then((res) => {
      setCurrentModel(res);
    });
  }, []);

  return (
    <View>
      <Text>Model available: {modelAvailable ? 'true' : 'false'}</Text>
      <Text>Current Model: {currentModel}</Text>
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
      {/*<Button*/}
      {/*  title={'download'}*/}
      {/*  onPress={async () => {*/}
      {/*    console.log('download');*/}
      {/*    try {*/}
      {/*      setCurrentModel(await downloadAndLoadModel(irl));*/}
      {/*    } catch (e) {*/}
      {/*      console.log(e);*/}
      {/*    }*/}

      {/*    console.log('done');*/}
      {/*  }}*/}
      {/*/>*/}
      {/*<Button*/}
      {/*  title={'convert to text'}*/}
      {/*  onPress={async () => {*/}
      {/*    console.log('convert to text');*/}
      {/*    const a = await transcribeWav(*/}
      {/*      Dirs.SDCardDir + '/Download/New_Real_Conversation_Lessons.wav'*/}
      {/*    );*/}
      {/*    console.log('a', a);*/}
      {/*  }}*/}
      {/*/>*/}
      {/*<Button*/}
      {/*  title={'get models'}*/}
      {/*  onPress={async () => {*/}
      {/*    const a = await getModels();*/}
      {/*    setModels(a);*/}
      {/*    console.log('models: ', a);*/}
      {/*  }}*/}
      {/*/>*/}
      {/*<FlatList*/}
      {/*  data={models}*/}
      {/*  renderItem={({ item }) => (*/}
      {/*    <Button*/}
      {/*      title={item}*/}
      {/*      onPress={async () => {*/}
      {/*        const a = await loadModel(item);*/}
      {/*        setCurrentModel(a);*/}
      {/*      }}*/}
      {/*    />*/}
      {/*  )}*/}
      {/*/>*/}
      {/*<Button*/}
      {/*  title={'load files in sdcard'}*/}
      {/*  onPress={async () => {*/}
      {/*    try {*/}
      {/*      if (Dirs.SDCardDir != null) {*/}
      {/*        const result = await FileSystem.ls(Dirs.SDCardDir + '/Download');*/}
      {/*        console.log('result', result);*/}
      {/*      }*/}
      {/*      const result = await FileSystem.ls(Dirs.DocumentDir);*/}
      {/*      console.log('result', result);*/}
      {/*    } catch (e) {*/}
      {/*      console.log('error', e);*/}
      {/*    }*/}
      {/*  }}*/}
      {/*/>*/}
    </View>
  );
}
