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

  getPlatformNameSync?: () => string;
  getAppVersionSync?: () => string;
  getBuildNumberSync?: () => string;
};

const { NativeDeviceModule } = NativeModules;

if (!NativeDeviceModule) {
  throw new Error(
    `NativeDeviceModule is not available on ${Platform.OS}. Check native module registration and rebuild the app.`,
  );
}

export default NativeDeviceModule as NativeDeviceModuleType;
