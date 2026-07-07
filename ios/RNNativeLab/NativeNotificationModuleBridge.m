#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(NativeNotificationModule, NSObject)

RCT_EXTERN_METHOD(requestNotificationPermission:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(showLocalNotification:
                  (NSString *)title
                  message:(NSString *)message
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end