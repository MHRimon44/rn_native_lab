import { NativeModules, Platform } from 'react-native';

export type NativeOrderSummary = {
  orderId: string;
  customerName: string;
  amount: number;
  status: string;
  isHighValue: boolean;
  source: string;
};

type NativeDebugModuleType = {
  getNativeGreeting(name: string): Promise<string>;
  openDebugScreen(orderId: string, amount: string): Promise<boolean>;
  getOrderSummary(orderId: string, amount: number): Promise<NativeOrderSummary>;
};

const { NativeDebugModule } = NativeModules;

if (!NativeDebugModule && Platform.OS === 'android') {
  throw new Error(
    'NativeDebugModule is not available. Check Android package registration and rebuild the app.',
  );
}

export default NativeDebugModule as NativeDebugModuleType;
