import { NativeModules, Platform } from 'react-native';
import {
  NativeNotification,
  NativeNotificationTap,
} from '../types/nativeNotification';

type NativeNotificationModuleType = {
  requestNotificationPermission(): Promise<boolean>;
  showLocalNotification(title: string, message: string): Promise<boolean>;
  saveNotification(notification: NativeNotification): Promise<boolean>;
  getNotifications(): Promise<NativeNotification[]>;
  markAsRead(id: string): Promise<boolean>;
  clearNotifications(): Promise<boolean>;
  getInitialNotification(): Promise<NativeNotificationTap | null>;
  clearInitialNotification(): Promise<boolean>;
  addListener(eventName: string): void;
  removeListeners(count: number): void;
  getUnreadCount(): Promise<number>;
  deleteNotification(id: string): Promise<boolean>;
  markAllAsRead(): Promise<boolean>;
  syncBadgeCount(): Promise<boolean>;
};

const { NativeNotificationModule } = NativeModules;

if (!NativeNotificationModule) {
  throw new Error(
    `NativeNotificationModule is not available on ${Platform.OS}. Check native module registration and rebuild the app.`,
  );
}

export default NativeNotificationModule as NativeNotificationModuleType;
