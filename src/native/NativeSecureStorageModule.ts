import { NativeModules, Platform } from 'react-native';

type NativeSecureStorageModuleType = {
  saveValue(key: string, value: string): Promise<boolean>;
  getValue(key: string): Promise<string | null>;
  deleteValue(key: string): Promise<boolean>;
  clearAll(): Promise<boolean>;
};

const { NativeSecureStorageModule } = NativeModules;

if (!NativeSecureStorageModule) {
  throw new Error(
    `NativeSecureStorageModule is not available on ${Platform.OS}. Check native module registration and rebuild the app.`,
  );
}

export default NativeSecureStorageModule as NativeSecureStorageModuleType;
