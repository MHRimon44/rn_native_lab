export type NativeNotificationSource = 'local' | 'push';

export type NativeNotification = {
  id: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  source: NativeNotificationSource;
};
