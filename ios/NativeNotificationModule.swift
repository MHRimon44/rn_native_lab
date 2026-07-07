import Foundation
import UserNotifications

@objc(NativeNotificationModule)
class NativeNotificationModule: NSObject {

  @objc
  func requestNotificationPermission(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    let center = UNUserNotificationCenter.current()

    center.requestAuthorization(
      options: [.alert, .sound, .badge]
    ) { granted, error in
      if let error = error {
        reject(
          "REQUEST_NOTIFICATION_PERMISSION_ERROR",
          error.localizedDescription,
          error
        )
        return
      }

      resolve(granted)
    }
  }

  @objc
  func showLocalNotification(
    _ title: String,
    message: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    if title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      reject("INVALID_TITLE", "Notification title cannot be blank", nil)
      return
    }

    if message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      reject("INVALID_MESSAGE", "Notification message cannot be blank", nil)
      return
    }

    let center = UNUserNotificationCenter.current()

    center.getNotificationSettings { settings in
      if settings.authorizationStatus != .authorized &&
          settings.authorizationStatus != .provisional {
        reject(
          "NOTIFICATION_PERMISSION_DENIED",
          "Notification permission is not granted",
          nil
        )
        return
      }

      let content = UNMutableNotificationContent()
      content.title = title
      content.body = message
      content.sound = .default
      content.userInfo = [
        "source": "NativeNotificationModule",
        "title": title,
        "message": message
      ]

      let trigger = UNTimeIntervalNotificationTrigger(
        timeInterval: 1,
        repeats: false
      )

      let request = UNNotificationRequest(
        identifier: UUID().uuidString,
        content: content,
        trigger: trigger
      )

      center.add(request) { error in
        if let error = error {
          reject(
            "SHOW_LOCAL_NOTIFICATION_ERROR",
            error.localizedDescription,
            error
          )
          return
        }

        resolve(true)
      }
    }
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}