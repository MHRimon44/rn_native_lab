import Foundation
import Security
import LocalAuthentication

@objc(NativeSecureStorageModule)
class NativeSecureStorageModule: NSObject {

  private let serviceName = "com.rnnativelab.securestorage"
  private let tokenKey = "access_token"
  private let source = "iOS Keychain"
  private let maxKeyLength = 100
  private let maxValueLength = 10000

  private func validateKey(_ key: String) -> String? {
    let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines)

    if normalizedKey.isEmpty {
      return "Storage key cannot be blank"
    }

    if normalizedKey.count > maxKeyLength {
      return "Storage key cannot be longer than \(maxKeyLength) characters"
    }

    let pattern = "^[A-Za-z0-9._-]+$"

    if normalizedKey.range(of: pattern, options: .regularExpression) == nil {
      return "Storage key can only contain letters, numbers, dot, underscore, and hyphen"
    }

    return nil
  }

  private func validateValue(_ value: String) -> String? {
    if value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      return "Storage value cannot be blank"
    }

    if value.count > maxValueLength {
      return "Storage value cannot be longer than \(maxValueLength) characters"
    }

    return nil
  }

  private func resultMap(
    success: Bool,
    key: String?,
    message: String
  ) -> [String: Any] {
    var result: [String: Any] = [
      "success": success,
      "message": message,
      "source": source
    ]

    if let key = key {
      result["key"] = key
    }

    return result
  }

  private func saveRawValue(
    key: String,
    value: String
  ) -> OSStatus {
    guard let valueData = value.data(using: .utf8) else {
      return errSecParam
    }

    let deleteQuery: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key
    ]

    SecItemDelete(deleteQuery as CFDictionary)

    let attributes: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key,
      kSecValueData as String: valueData
    ]

    return SecItemAdd(attributes as CFDictionary, nil)
  }

  private func readRawValue(key: String) -> (status: OSStatus, value: String?) {
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
      return (status, nil)
    }

    if status != errSecSuccess {
      return (status, nil)
    }

    guard let data = item as? Data else {
      return (errSecDecode, nil)
    }

    let value = String(data: data, encoding: .utf8)
    return (status, value)
  }

  private func deleteRawValue(key: String) -> OSStatus {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: serviceName,
      kSecAttrAccount as String: key
    ]

    return SecItemDelete(query as CFDictionary)
  }

  @objc
  func saveValue(
    _ key: String,
    value: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines)

    if let keyError = validateKey(normalizedKey) {
      reject("INVALID_KEY", keyError, nil)
      return
    }

    if let valueError = validateValue(value) {
      reject("INVALID_VALUE", valueError, nil)
      return
    }

    let status = saveRawValue(key: normalizedKey, value: value)

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
    let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines)

    if let keyError = validateKey(normalizedKey) {
      reject("INVALID_KEY", keyError, nil)
      return
    }

    let result = readRawValue(key: normalizedKey)

    if result.status == errSecItemNotFound {
      resolve(nil)
      return
    }

    if result.status != errSecSuccess {
      reject(
        "GET_SECURE_VALUE_ERROR",
        "Failed to read value. OSStatus: \(result.status)",
        nil
      )
      return
    }

    resolve(result.value)
  }

  @objc
  func deleteValue(
    _ key: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines)

    if let keyError = validateKey(normalizedKey) {
      reject("INVALID_KEY", keyError, nil)
      return
    }

    let status = deleteRawValue(key: normalizedKey)

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
  func saveToken(
    _ token: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    if let valueError = validateValue(token) {
      reject("INVALID_TOKEN", valueError, nil)
      return
    }

    let status = saveRawValue(key: tokenKey, value: token)

    if status == errSecSuccess {
      resolve(
        resultMap(
          success: true,
          key: tokenKey,
          message: "Token saved securely"
        )
      )
    } else {
      reject(
        "SAVE_TOKEN_ERROR",
        "Failed to save token. OSStatus: \(status)",
        nil
      )
    }
  }

  @objc
  func getToken(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let result = readRawValue(key: tokenKey)

    if result.status == errSecItemNotFound {
      resolve(nil)
      return
    }

    if result.status != errSecSuccess {
      reject(
        "GET_TOKEN_ERROR",
        "Failed to read token. OSStatus: \(result.status)",
        nil
      )
      return
    }

    resolve(result.value)
  }

  @objc
  func deleteToken(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let status = deleteRawValue(key: tokenKey)

    if status == errSecSuccess || status == errSecItemNotFound {
      resolve(
        resultMap(
          success: true,
          key: tokenKey,
          message: "Token deleted securely"
        )
      )
    } else {
      reject(
        "DELETE_TOKEN_ERROR",
        "Failed to delete token. OSStatus: \(status)",
        nil
      )
    }
  }

  @objc
  func hasValue(
    _ key: String,
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines)

    if let keyError = validateKey(normalizedKey) {
      reject("INVALID_KEY", keyError, nil)
      return
    }

    let result = readRawValue(key: normalizedKey)

    if result.status == errSecSuccess {
      resolve(true)
      return
    }

    if result.status == errSecItemNotFound {
      resolve(false)
      return
    }

    reject(
      "HAS_VALUE_ERROR",
      "Failed to check value. OSStatus: \(result.status)",
      nil
    )
  }

  @objc
  func isBiometricAvailable(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let context = LAContext()
    var error: NSError?

    let available = context.canEvaluatePolicy(
      .deviceOwnerAuthentication,
      error: &error
    )

    resolve(available)
  }

  @objc
  func getTokenWithBiometric(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    let context = LAContext()
    context.localizedCancelTitle = "Cancel"

    var authError: NSError?

    let canAuthenticate = context.canEvaluatePolicy(
      .deviceOwnerAuthentication,
      error: &authError
    )

    if !canAuthenticate {
      reject(
        "BIOMETRIC_NOT_AVAILABLE",
        authError?.localizedDescription ?? "Biometric or device authentication is not available",
        authError
      )
      return
    }

    context.evaluatePolicy(
      .deviceOwnerAuthentication,
      localizedReason: "Authenticate to read your secure token"
    ) { success, error in
      if !success {
        reject(
          "BIOMETRIC_AUTH_FAILED",
          error?.localizedDescription ?? "Biometric authentication failed",
          error
        )
        return
      }

      let result = self.readRawValue(key: self.tokenKey)

      if result.status == errSecItemNotFound {
        resolve(nil)
        return
      }

      if result.status != errSecSuccess {
        reject(
          "GET_TOKEN_ERROR",
          "Failed to read token. OSStatus: \(result.status)",
          nil
        )
        return
      }

      resolve(result.value)
    }
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}