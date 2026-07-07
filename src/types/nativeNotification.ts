export type NativeNotificationSource = 'local' | 'push';

export type NativeNotification = {
  id: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  source: NativeNotificationSource;
};

export type NativeNotificationTap = {
  id: string;
  title: string;
  message: string;
  source: NativeNotificationSource;
  openedAt: string;
};
