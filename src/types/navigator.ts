export type RootStackParamList = {
  Home: undefined;
  NativeDebug: undefined;
  NativeDevice: undefined;
  NativeSecureStorage: undefined;
  NativeNotification:
    | {
        initialNotificationId?: string;
      }
    | undefined;
};
