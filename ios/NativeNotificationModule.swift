import Foundation
import UserNotifications
import SQLite3
import UIKit

class NotificationTapStore {
  static let shared = NotificationTapStore()

  private var pendingTap: [String: Any]?
  private var eventEmitter: (([String: Any]) -> Void)?

  private init() {}

  func setEventEmitter(_ emitter: (([String: Any]) -> Void)?) {
    eventEmitter = emitter
  }

  func saveFromUserInfo(_ userInfo: [AnyHashable: Any]) {
    guard let id = userInfo["id"] as? String else {
      return
    }

    let title = userInfo["title"] as? String ?? ""
    let message = userInfo["message"] as? String ?? ""
    let source = userInfo["source"] as? String ?? "local"

    let tap: [String: Any] = [
      "id": id,
      "title": title,
      "message": message,
      "source": source == "NativeNotificationModule" ? "local" : source,
      "openedAt": currentIsoTime()
    ]

    pendingTap = tap

    DispatchQueue.main.async {
      self.eventEmitter?(tap)
    }
  }

  func getPendingTap() -> [String: Any]? {
    return pendingTap
  }

  func clear() {
    pendingTap = nil
  }

  private func currentIsoTime() -> String {
    let formatter = ISO8601DateFormatter()
    formatter.formatOptions = [
      .withInternetDateTime,
      .withFractionalSeconds
    ]
    return formatter.string(from: Date())
  }
}

@objc(NativeNotificationModule)
class NativeNotificationModule: RCTEventEmitter {

  private let databaseName = "rn_native_lab_notifications.sqlite"
  private let tableName = "notifications"

  private var database: OpaquePointer?

  private let sqliteTransient = unsafeBitCast(
    -1,
    to: sqlite3_destructor_type.self
  )

  deinit {
    if database != nil {
      sqlite3_close(database)
      database = nil
    }
  }
  override init() {
    super.init()
    NotificationTapStore.shared.setEventEmitter { [weak self] tap in
      self?.sendEvent(
        withName: "NativeNotificationTapped",
        body: tap
      )
    }

    openDatabase()
    createTableIfNeeded()
  }

  override func supportedEvents() -> [String]! {
    return ["NativeNotificationTapped"]
  }

  override func startObserving() {
    NotificationTapStore.shared.setEventEmitter { [weak self] tap in
      self?.sendEvent(
        withName: "NativeNotificationTapped",
        body: tap
      )
    }
  }

  override func stopObserving() {
    NotificationTapStore.shared.setEventEmitter(nil)
  }
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
    let normalizedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
    let normalizedMessage = message.trimmingCharacters(in: .whitespacesAndNewlines)

    if normalizedTitle.isEmpty {
      reject("INVALID_TITLE", "Notification title cannot be blank", nil)
      return
    }

    if normalizedMessage.isEmpty {
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

      let id = UUID().uuidString
      let createdAt = self.currentIsoTime()

      let content = UNMutableNotificationContent()
      content.title = normalizedTitle
      content.body = normalizedMessage
      content.sound = .default
      content.userInfo = [
        "id": id,
        "source": "NativeNotificationModule",
        "title": normalizedTitle,
        "message": normalizedMessage
      ]

      let trigger = UNTimeIntervalNotificationTrigger(
        timeInterval: 1,
        repeats: false
      )

      let nextBadgeCount = self.getUnreadCountFromDatabase() + 1
      content.badge = NSNumber(value: nextBadgeCount)
      let request = UNNotificationRequest(
        identifier: id,
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

        let saved = self.saveNotificationToDatabase(
          id: id,
          title: normalizedTitle,
          message: normalizedMessage,
          isRead: false,
          source: "local",
          createdAt: createdAt
        )

        if !saved.success {
          reject(
            "SAVE_NOTIFICATION_ERROR",
            saved.message,
            nil
          )
          return
        }
        self.updateAppBadgeCount(nextBadgeCount)
        resolve(true)
      }
    }
  }

  @objc
  func saveNotification(
    _ notification: NSDictionary,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let title = (notification["title"] as? String ?? "")
      .trimmingCharacters(in: .whitespacesAndNewlines)

    let message = (notification["message"] as? String ?? "")
      .trimmingCharacters(in: .whitespacesAndNewlines)

    if title.isEmpty {
      reject("INVALID_TITLE", "Notification title cannot be blank", nil)
      return
    }

    if message.isEmpty {
      reject("INVALID_MESSAGE", "Notification message cannot be blank", nil)
      return
    }

    let id = notification["id"] as? String ?? UUID().uuidString
    let source = notification["source"] as? String ?? "local"
    let createdAt = notification["createdAt"] as? String ?? currentIsoTime()
    let isRead = notification["isRead"] as? Bool ?? false

    let result = saveNotificationToDatabase(
      id: id,
      title: title,
      message: message,
      isRead: isRead,
      source: source,
      createdAt: createdAt
    )

    if result.success {
      resolve(true)
    } else {
      reject("SAVE_NOTIFICATION_ERROR", result.message, nil)
    }
  }

  @objc
  func getNotifications(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    guard database != nil else {
      reject("DATABASE_NOT_READY", "Notification database is not ready", nil)
      return
    }

    let query = """
    SELECT id, title, message, is_read, source, created_at
    FROM \(tableName)
    ORDER BY created_at DESC
    """

    var statement: OpaquePointer?

    if sqlite3_prepare_v2(database, query, -1, &statement, nil) != SQLITE_OK {
      reject(
        "GET_NOTIFICATIONS_ERROR",
        "Failed to prepare notification query",
        nil
      )
      return
    }

    var notifications: [[String: Any]] = []

    while sqlite3_step(statement) == SQLITE_ROW {
      let id = readColumnText(statement, index: 0)
      let title = readColumnText(statement, index: 1)
      let message = readColumnText(statement, index: 2)
      let isRead = sqlite3_column_int(statement, 3) == 1
      let source = readColumnText(statement, index: 4)
      let createdAt = readColumnText(statement, index: 5)

      notifications.append([
        "id": id,
        "title": title,
        "message": message,
        "isRead": isRead,
        "source": source,
        "createdAt": createdAt
      ])
    }

    sqlite3_finalize(statement)

    resolve(notifications)
  }

  @objc
  func markAsRead(
    _ id: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let normalizedId = id.trimmingCharacters(in: .whitespacesAndNewlines)

    if normalizedId.isEmpty {
      reject("INVALID_ID", "Notification id cannot be blank", nil)
      return
    }

    guard database != nil else {
      reject("DATABASE_NOT_READY", "Notification database is not ready", nil)
      return
    }

    let query = """
    UPDATE \(tableName)
    SET is_read = 1
    WHERE id = ?
    """

    var statement: OpaquePointer?

    if sqlite3_prepare_v2(database, query, -1, &statement, nil) != SQLITE_OK {
      reject(
        "MARK_NOTIFICATION_READ_ERROR",
        "Failed to prepare mark as read query",
        nil
      )
      return
    }

    sqlite3_bind_text(statement, 1, normalizedId, -1, sqliteTransient)

    if sqlite3_step(statement) == SQLITE_DONE {
      sqlite3_finalize(statement)
      updateAppBadgeCount(getUnreadCountFromDatabase())
      resolve(true)
    } else {
      sqlite3_finalize(statement)
      reject(
        "MARK_NOTIFICATION_READ_ERROR",
        "Failed to mark notification as read",
        nil
      )
    }
  }

  @objc
  func clearNotifications(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    guard database != nil else {
      reject("DATABASE_NOT_READY", "Notification database is not ready", nil)
      return
    }

    let query = "DELETE FROM \(tableName)"

    if sqlite3_exec(database, query, nil, nil, nil) == SQLITE_OK {
      updateAppBadgeCount(0)
      resolve(true)
    } else {
      reject(
        "CLEAR_NOTIFICATIONS_ERROR",
        "Failed to clear notifications",
        nil
      )
    }
  }

  private func openDatabase() {
    let fileManager = FileManager.default

    guard let documentsDirectory = fileManager.urls(
      for: .documentDirectory,
      in: .userDomainMask
    ).first else {
      return
    }

    let databaseUrl = documentsDirectory.appendingPathComponent(databaseName)

    if sqlite3_open(databaseUrl.path, &database) != SQLITE_OK {
      database = nil
    }
  }

  private func createTableIfNeeded() {
    guard database != nil else {
      return
    }

    let createTableQuery = """
    CREATE TABLE IF NOT EXISTS \(tableName) (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      message TEXT NOT NULL,
      is_read INTEGER NOT NULL DEFAULT 0,
      source TEXT NOT NULL DEFAULT 'local',
      created_at TEXT NOT NULL
    );
    """

    sqlite3_exec(database, createTableQuery, nil, nil, nil)

    let createIndexQuery = """
    CREATE INDEX IF NOT EXISTS idx_notifications_created_at
    ON \(tableName)(created_at DESC);
    """

    sqlite3_exec(database, createIndexQuery, nil, nil, nil)
  }

  private func saveNotificationToDatabase(
    id: String,
    title: String,
    message: String,
    isRead: Bool,
    source: String,
    createdAt: String
  ) -> (success: Bool, message: String) {
    guard database != nil else {
      return (false, "Notification database is not ready")
    }

    let query = """
    INSERT OR REPLACE INTO \(tableName)
    (id, title, message, is_read, source, created_at)
    VALUES (?, ?, ?, ?, ?, ?)
    """

    var statement: OpaquePointer?

    if sqlite3_prepare_v2(database, query, -1, &statement, nil) != SQLITE_OK {
      return (false, "Failed to prepare insert statement")
    }

    sqlite3_bind_text(statement, 1, id, -1, sqliteTransient)
    sqlite3_bind_text(statement, 2, title, -1, sqliteTransient)
    sqlite3_bind_text(statement, 3, message, -1, sqliteTransient)
    sqlite3_bind_int(statement, 4, isRead ? 1 : 0)
    sqlite3_bind_text(statement, 5, source, -1, sqliteTransient)
    sqlite3_bind_text(statement, 6, createdAt, -1, sqliteTransient)

    if sqlite3_step(statement) == SQLITE_DONE {
      sqlite3_finalize(statement)
      return (true, "Notification saved")
    }

    sqlite3_finalize(statement)
    return (false, "Failed to insert notification")
  }

  private func readColumnText(
    _ statement: OpaquePointer?,
    index: Int32
  ) -> String {
    guard let text = sqlite3_column_text(statement, index) else {
      return ""
    }

    return String(cString: text)
  }

  private func currentIsoTime() -> String {
    let formatter = ISO8601DateFormatter()
    formatter.formatOptions = [
      .withInternetDateTime,
      .withFractionalSeconds
    ]
    return formatter.string(from: Date())
  }

  private func getUnreadCountFromDatabase() -> Int {
    guard database != nil else {
      return 0
    }

    let query = """
    SELECT COUNT(*)
    FROM \(tableName)
    WHERE is_read = 0
    """

    var statement: OpaquePointer?
    var count = 0

    if sqlite3_prepare_v2(database, query, -1, &statement, nil) == SQLITE_OK {
      if sqlite3_step(statement) == SQLITE_ROW {
        count = Int(sqlite3_column_int(statement, 0))
      }
    }

    sqlite3_finalize(statement)

    return count
  }

  private func updateAppBadgeCount(_ count: Int) {
    DispatchQueue.main.async {
      UIApplication.shared.applicationIconBadgeNumber = count
    }
  }

  @objc
  func getInitialNotification(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    if let tap = NotificationTapStore.shared.getPendingTap() {
      resolve(tap)
    } else {
      resolve(nil)
    }
  }

  @objc
  func clearInitialNotification(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    NotificationTapStore.shared.clear()
    resolve(true)
  }

  @objc
  func getUnreadCount(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    resolve(getUnreadCountFromDatabase())
  }

  @objc
  func deleteNotification(
    _ notificationId: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let normalizedId = notificationId.trimmingCharacters(in: .whitespacesAndNewlines)

    if normalizedId.isEmpty {
      reject("INVALID_ID", "Notification id cannot be blank", nil)
      return
    }

    guard database != nil else {
      reject("DATABASE_NOT_READY", "Notification database is not ready", nil)
      return
    }

    let query = """
    DELETE FROM \(tableName)
    WHERE id = ?
    """

    var statement: OpaquePointer?

    if sqlite3_prepare_v2(database, query, -1, &statement, nil) != SQLITE_OK {
      reject(
        "DELETE_NOTIFICATION_ERROR",
        "Failed to prepare delete notification query",
        nil
      )
      return
    }

    sqlite3_bind_text(statement, 1, normalizedId, -1, sqliteTransient)

    if sqlite3_step(statement) == SQLITE_DONE {
      sqlite3_finalize(statement)
      updateAppBadgeCount(getUnreadCountFromDatabase())
      resolve(true)
    } else {
      sqlite3_finalize(statement)
      reject(
        "DELETE_NOTIFICATION_ERROR",
        "Failed to delete notification",
        nil
      )
    }
  }

  @objc
  func markAllAsRead(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    guard database != nil else {
      reject("DATABASE_NOT_READY", "Notification database is not ready", nil)
      return
    }

    let query = """
    UPDATE \(tableName)
    SET is_read = 1
    """

    if sqlite3_exec(database, query, nil, nil, nil) == SQLITE_OK {
      updateAppBadgeCount(0)
      resolve(true)
    } else {
      reject(
        "MARK_ALL_NOTIFICATIONS_READ_ERROR",
        "Failed to mark all notifications as read",
        nil
      )
    }
  }

  @objc
  func syncBadgeCount(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let unreadCount = getUnreadCountFromDatabase()
    updateAppBadgeCount(unreadCount)
    resolve(true)
  }

  @objc
  override static func requiresMainQueueSetup() -> Bool {
    return false
  }
}