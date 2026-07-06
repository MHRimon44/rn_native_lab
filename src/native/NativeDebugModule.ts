import { NativeModules, Platform } from 'react-native';

export type NativeOrderSummary = {
  orderId: string;
  customerName: string;
  amount: number;
  status: string;
  isHighValue: boolean;
  source: string;
};

export type NativeOrderItem = {
  orderId: string;
  customerName: string;
  amount: number;
  status: string;
  isHighValue: boolean;
  source: string;
};

export type NativeOrderInput = {
  orderId: string;
  customerName?: string;
  amount: number;
  status?: string;
};

export type NativeOrderArraySummary = {
  totalOrders: number;
  totalAmount: number;
  highValueOrders: number;
  deliveredOrders: number;
  source: string;
  message: string;
};
export type NativeDebugEventPayload = {
  message: string;
  source: string;
  timestamp: number;
};

export type OrderSyncProgressPayload = {
  orderId: string;
  progress: number;
  isCompleted: boolean;
  message: string;
};

export type NativeCouponResult = {
  couponCode: string;
  amount: number;
  discountPercent: number;
  discountAmount: number;
  finalAmount: number;
  message: string;
  source: string;
};

export type NativePermissionResult = {
  permission: string;
  granted: boolean;
  status: 'GRANTED' | 'DENIED';
  message: string;
  source: string;
};

type NativeDebugModuleType = {
  getNativeGreeting(name: string): Promise<string>;
  openDebugScreen(orderId: string, amount: string): Promise<boolean>;
  getOrderSummary(orderId: string, amount: number): Promise<NativeOrderSummary>;
  getRecentOrders(maxCount: number): Promise<NativeOrderItem[]>;
  createOrderFromMap(input: NativeOrderInput): Promise<
    NativeOrderSummary & {
      message: string;
    }
  >;
  summarizeOrdersFromArray(
    orders: NativeOrderInput[],
  ): Promise<NativeOrderArraySummary>;
  emitTestEvent(message: string): Promise<boolean>;
  startFakeOrderSync(orderId: string): Promise<string>;

  validateCouponWithCallback(
    couponCode: string,
    amount: number,
    successCallback: (result: NativeCouponResult) => void,
    errorCallback: (code: string, message: string) => void,
  ): void;

  checkCameraPermission(): Promise<NativePermissionResult>;
  requestCameraPermission(): Promise<NativePermissionResult>;
};

const { NativeDebugModule } = NativeModules;

if (!NativeDebugModule && Platform.OS === 'android') {
  throw new Error(
    'NativeDebugModule is not available. Check Android package registration and rebuild the app.',
  );
}

export default NativeDebugModule as NativeDebugModuleType;
