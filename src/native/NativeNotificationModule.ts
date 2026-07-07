import { NativeModules, Platform } from 'react-native';

type NativeNotificationModuleType = {
  requestNotificationPermission(): Promise<boolean>;
  showLocalNotification(title: string, message: string): Promise<boolean>;
};

const { NativeNotificationModule } = NativeModules;

if (!NativeNotificationModule) {
  throw new Error(
    `NativeNotificationModule is not available on ${Platform.OS}. Check native module registration and rebuild the app.`,
  );
}

export default NativeNotificationModule as NativeNotificationModuleType;
