package com.rnnativelab

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class NativeDeviceModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "NativeDeviceModule"
    }

    @ReactMethod
    fun getAppVersion(promise: Promise) {
        try {
            val packageInfo = reactContext.packageManager.getPackageInfo(
                reactContext.packageName,
                0
            )

            promise.resolve(packageInfo.versionName ?: "Unknown")
        } catch (error: Exception) {
            promise.reject(
                "GET_APP_VERSION_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getBuildNumber(promise: Promise) {
        try {
            val packageInfo = reactContext.packageManager.getPackageInfo(
                reactContext.packageName,
                0
            )

            val buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            promise.resolve(buildNumber)
        } catch (error: Exception) {
            promise.reject(
                "GET_BUILD_NUMBER_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getDeviceModel(promise: Promise) {
        try {
            val model = "${Build.MANUFACTURER} ${Build.MODEL}"
            promise.resolve(model)
        } catch (error: Exception) {
            promise.reject(
                "GET_DEVICE_MODEL_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getOSVersion(promise: Promise) {
        try {
            val osVersion = "Android ${Build.VERSION.RELEASE} API ${Build.VERSION.SDK_INT}"
            promise.resolve(osVersion)
        } catch (error: Exception) {
            promise.reject(
                "GET_OS_VERSION_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getBatteryLevel(promise: Promise) {
        try {
            val batteryIntent = reactContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            if (batteryIntent == null) {
                promise.reject(
                    "BATTERY_INFO_NOT_AVAILABLE",
                    "Battery information is not available"
                )
                return
            }

            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level < 0 || scale <= 0) {
                promise.reject(
                    "INVALID_BATTERY_INFO",
                    "Invalid battery level or scale"
                )
                return
            }

            val batteryPercent = level * 100.0 / scale
            promise.resolve(batteryPercent)
        } catch (error: Exception) {
            promise.reject(
                "GET_BATTERY_LEVEL_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun isBatteryCharging(promise: Promise) {
        try {
            val batteryIntent = reactContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            if (batteryIntent == null) {
                promise.reject(
                    "BATTERY_INFO_NOT_AVAILABLE",
                    "Battery information is not available"
                )
                return
            }

            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            promise.resolve(isCharging)
        } catch (error: Exception) {
            promise.reject(
                "IS_BATTERY_CHARGING_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getDeviceSummary(promise: Promise) {
        try {
            val packageInfo = reactContext.packageManager.getPackageInfo(
                reactContext.packageName,
                0
            )

            val buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            val batteryIntent = reactContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val batteryLevel = if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

                if (level >= 0 && scale > 0) {
                    level * 100.0 / scale
                } else {
                    -1.0
                }
            } else {
                -1.0
            }

            val batteryStatus = batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) ?: -1

            val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                batteryStatus == BatteryManager.BATTERY_STATUS_FULL

            val summaryMap = Arguments.createMap().apply {
                putString("appVersion", packageInfo.versionName ?: "Unknown")
                putString("buildNumber", buildNumber)
                putString("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
                putString("osVersion", "Android ${Build.VERSION.RELEASE} API ${Build.VERSION.SDK_INT}")
                putDouble("batteryLevel", batteryLevel)
                putBoolean("isBatteryCharging", isCharging)
                putString("source", "Kotlin NativeDeviceModule")
            }

            promise.resolve(summaryMap)
        } catch (error: Exception) {
            promise.reject(
                "GET_DEVICE_SUMMARY_ERROR",
                error.message,
                error
            )
        }
    }
}