import { NativeModules, Platform } from 'react-native';

type NativeDebugModuleType = {
  getNativeGreeting(name: string): Promise<string>;
  openDebugScreen(orderId: string, amount: string): Promise<boolean>;
};

const { NativeDebugModule } = NativeModules;

if (!NativeDebugModule && Platform.OS === 'android') {
  throw new Error(
    'NativeDebugModule is not available. Check Android package registration and rebuild the app.',
  );
}

export default NativeDebugModule as NativeDebugModuleType;
