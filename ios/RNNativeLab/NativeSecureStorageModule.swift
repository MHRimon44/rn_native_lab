import Foundation
import Security

@objc(NativeSecureStorageModule)
class NativeSecureStorageModule: NSObject {

  private let serviceName = "com.rnnativelab.securestorage"

  @objc
  func saveValue(
    _ key: String,
    value: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    if key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      reject("INVALID_KEY", "Storage key cannot be blank", nil)
      return
    }

    guard let valueData = value.data(using: .utf8) else {
      reject("INVALID_VALUE", "Value cannot be converted to data", nil)
      return
    }

    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key
    ]

    SecItemDelete(query as CFDictionary)

    let attributes: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key,
      kSecValueData as String: valueData
    ]

    let status = SecItemAdd(attributes as CFDictionary, nil)

    if status == errSecSuccess {
      resolve(true)
    } else {
      reject(
        "SAVE_SECURE_VALUE_ERROR",
        "Failed to save value. OSStatus: \(status)",
        nil
      )
    }
  }

  @objc
  func getValue(
    _ key: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    if key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      reject("INVALID_KEY", "Storage key cannot be blank", nil)
      return
    }

    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key,
      kSecReturnData as String: true,
      kSecMatchLimit as String: kSecMatchLimitOne
    ]

    var item: CFTypeRef?
    let status = SecItemCopyMatching(query as CFDictionary, &item)

    if status == errSecItemNotFound {
      resolve(nil)
      return
    }

    if status != errSecSuccess {
      reject(
        "GET_SECURE_VALUE_ERROR",
        "Failed to read value. OSStatus: \(status)",
        nil
      )
      return
    }

    guard let data = item as? Data else {
      reject("INVALID_STORED_DATA", "Stored value is not valid data", nil)
      return
    }

    let value = String(data: data, encoding: .utf8)

    resolve(value)
  }

  @objc
  func deleteValue(
    _ key: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    if key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      reject("INVALID_KEY", "Storage key cannot be blank", nil)
      return
    }

    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key
    ]

    let status = SecItemDelete(query as CFDictionary)

    if status == errSecSuccess || status == errSecItemNotFound {
      resolve(true)
    } else {
      reject(
        "DELETE_SECURE_VALUE_ERROR",
        "Failed to delete value. OSStatus: \(status)",
        nil
      )
    }
  }

  @objc
  func clearAll(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName
    ]

    let status = SecItemDelete(query as CFDictionary)

    if status == errSecSuccess || status == errSecItemNotFound {
      resolve(true)
    } else {
      reject(
        "CLEAR_SECURE_STORAGE_ERROR",
        "Failed to clear secure storage. OSStatus: \(status)",
        nil
      )
    }
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}