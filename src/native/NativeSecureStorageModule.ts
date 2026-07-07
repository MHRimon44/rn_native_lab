import { NativeModules, Platform } from 'react-native';

export type SecureStorageResult = {
  success: boolean;
  key?: string;
  message: string;
  source: string;
};

type NativeSecureStorageModuleType = {
  saveValue(key: string, value: string): Promise<boolean>;
  getValue(key: string): Promise<string | null>;
  deleteValue(key: string): Promise<boolean>;
  clearAll(): Promise<boolean>;

  saveToken(token: string): Promise<SecureStorageResult>;
  getToken(): Promise<string | null>;
  deleteToken(): Promise<SecureStorageResult>;
  hasValue(key: string): Promise<boolean>;

  isBiometricAvailable(): Promise<boolean>;
  getTokenWithBiometric(): Promise<string | null>;
};

const { NativeSecureStorageModule } = NativeModules;

if (!NativeSecureStorageModule) {
  throw new Error(
    `NativeSecureStorageModule is not available on ${Platform.OS}. Check native module registration and rebuild the app.`,
  );
}

export default NativeSecureStorageModule as NativeSecureStorageModuleType;
