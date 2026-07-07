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

RCT_EXTERN_METHOD(saveNotification:
                  (NSDictionary *)notification
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getNotifications:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(markAsRead:
                  (NSString *)id
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clearNotifications:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end