import { NativeModules, Platform } from 'react-native';
import {
  NativeCameraCaptureResult,
  NativeCouponResult,
  NativeFullSizeCameraResult,
  NativeOrderArraySummary,
  NativeOrderInput,
  NativeOrderItem,
  NativeOrderSummary,
  NativePermissionResult,
} from '../types/nativeDebug';

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
  openCamera(): Promise<NativeCameraCaptureResult>;
  openCameraFullSize(): Promise<NativeFullSizeCameraResult>;
};

const { NativeDebugModule } = NativeModules;

if (!NativeDebugModule && Platform.OS === 'android') {
  throw new Error(
    'NativeDebugModule is not available. Check Android package registration and rebuild the app.',
  );
}

export default NativeDebugModule as NativeDebugModuleType;
