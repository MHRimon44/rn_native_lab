import { NativeModules, Platform } from 'react-native';
import { NativeDeviceSummary } from '../types/nativeDevice';

type NativeDeviceModuleType = {
  getAppVersion(): Promise<string>;
  getBuildNumber(): Promise<string>;
  getDeviceModel(): Promise<string>;
  getOSVersion(): Promise<string>;
  getBatteryLevel(): Promise<number>;
  isBatteryCharging(): Promise<boolean>;
  getDeviceSummary(): Promise<NativeDeviceSummary>;
};

const { NativeDeviceModule } = NativeModules;

if (!NativeDeviceModule && Platform.OS === 'android') {
  throw new Error(
    'NativeDeviceModule is not available. Check Android package registration and rebuild the app.',
  );
}

export default NativeDeviceModule as NativeDeviceModuleType;
