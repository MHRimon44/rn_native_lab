import Foundation
import UIKit

@objc(NativeDeviceModule)
class NativeDeviceModule: NSObject {

  @objc
  func getAppVersion(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
    resolve(version)
  }

  @objc
  func getBuildNumber(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown"
    resolve(buildNumber)
  }

  @objc
  func getDeviceModel(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let deviceModel = getExactDeviceModel()
    resolve(deviceModel)
  }

  @objc
  func getOSVersion(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    let osVersion = "\(UIDevice.current.systemName) \(UIDevice.current.systemVersion)"
    resolve(osVersion)
  }

  @objc
  func getBatteryLevel(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    UIDevice.current.isBatteryMonitoringEnabled = true

    let batteryLevel = UIDevice.current.batteryLevel

    if batteryLevel < 0 {
      reject(
        "BATTERY_INFO_NOT_AVAILABLE",
        "Battery information is not available",
        nil
      )
      return
    }

    resolve(Double(batteryLevel * 100))
  }

  @objc
  func isBatteryCharging(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    UIDevice.current.isBatteryMonitoringEnabled = true

    let batteryState = UIDevice.current.batteryState
    let isCharging = batteryState == .charging || batteryState == .full

    resolve(isCharging)
  }

  @objc
  func getDeviceSummary(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    UIDevice.current.isBatteryMonitoringEnabled = true

    let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
    let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown"
    let deviceModel = getExactDeviceModel()
    let osVersion = "\(UIDevice.current.systemName) \(UIDevice.current.systemVersion)"

    let batteryLevelValue = UIDevice.current.batteryLevel
    let batteryLevel = batteryLevelValue >= 0 ? Double(batteryLevelValue * 100) : -1.0

    let batteryState = UIDevice.current.batteryState
    let isCharging = batteryState == .charging || batteryState == .full

    let summary: [String: Any] = [
      "appVersion": appVersion,
      "buildNumber": buildNumber,
      "deviceModel": deviceModel,
      "osVersion": osVersion,
      "batteryLevel": batteryLevel,
      "isBatteryCharging": isCharging,
      "source": "Swift NativeDeviceModule"
    ]

    resolve(summary)
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
      return false
    }

  private func getExactDeviceModel() -> String {
  #if targetEnvironment(simulator)
    let simulatorModelIdentifier =
      ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] ?? "Unknown Simulator"

    let simulatorName =
      ProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] ?? "iOS Simulator"

    return "\(simulatorName) (\(simulatorModelIdentifier))"
    #else
    var systemInfo = utsname()
    uname(&systemInfo)

    let machineMirror = Mirror(reflecting: systemInfo.machine)

    let identifier = machineMirror.children.reduce("") { result, element in
      guard let value = element.value as? Int8, value != 0 else {
        return result
      }

      return result + String(UnicodeScalar(UInt8(value)))
    }

    return identifier
    #endif
  }
}