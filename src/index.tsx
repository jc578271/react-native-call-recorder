import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-call-recorder' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const CallRecorder = NativeModules.CallRecorder
  ? NativeModules.CallRecorder
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return CallRecorder.multiply(a, b);
}

const nativeEventEmitter = new NativeEventEmitter(CallRecorder);

export function onOutgoingCallRecorded(
  listener: (params: {
    filePath: string;
    number: string;
    start: string;
    end: string;
  }) => void
) {
  return nativeEventEmitter.addListener('onOutgoingCallRecorded', listener);
}

export function removeOutgoingCallRecorded() {
  return nativeEventEmitter.removeAllListeners('onOutgoingCallRecorded');
}

export function addPhonesWhiteList(phones: string[]): Promise<string[]> {
  return CallRecorder.addPhonesWhiteList(phones);
}
export function clearWhiteList(): void {
  return CallRecorder.clearWhiteList();
}

export function getWhiteList(): Promise<string[]> {
  return CallRecorder.getWhiteList();
}
export function deletePhoneWhiteList(phone: string): Promise<string[]> {
  return CallRecorder.deletePhoneWhiteList(phone);
}

export function addPhonesBlackList(phone: string[]): Promise<string[]> {
  return CallRecorder.addPhonesBlackList(phone);
}
export function clearBlackList(): void {
  return CallRecorder.clearBlackList();
}
export function getBlackList(): Promise<string[]> {
  return CallRecorder.getBlackList();
}
export function deletePhoneBlackList(phone: string): Promise<string[]> {
  return CallRecorder.deletePhoneBlackList(phone);
}

export function switchRecordStatus(status: boolean): void {
  return CallRecorder.switchRecordStatus(status);
}

export function openAccessibilitySettings(): void {
  return CallRecorder.openAccessibilitySettings();
}

export function openSpecificAccessibilitySettings(): void {
  return CallRecorder.openSpecificAccessibilitySettings();
}
