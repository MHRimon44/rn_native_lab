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

export type NativeCameraCaptureResult = {
  success: boolean;
  message: string;
  width: number;
  height: number;
  source: string;
};

export type NativeFullSizeCameraResult = {
  success: boolean;
  message: string;
  filePath: string | null;
  fileUri: string | null;
  fileName: string | null;
  source: string;
};
