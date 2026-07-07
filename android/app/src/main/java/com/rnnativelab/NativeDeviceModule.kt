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
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
            promise.resolve(deviceModel)
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
            val batteryIntent = getBatteryIntent()

            if (batteryIntent == null) {
                promise.reject(
                    "BATTERY_INFO_NOT_AVAILABLE",
                    "Battery information is not available"
                )
                return
            }

            val batteryLevel = calculateBatteryLevel(batteryIntent)

            if (batteryLevel < 0.0) {
                promise.reject(
                    "INVALID_BATTERY_INFO",
                    "Invalid battery level or scale"
                )
                return
            }

            promise.resolve(batteryLevel)
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
            val batteryIntent = getBatteryIntent()

            if (batteryIntent == null) {
                promise.reject(
                    "BATTERY_INFO_NOT_AVAILABLE",
                    "Battery information is not available"
                )
                return
            }

            promise.resolve(checkBatteryCharging(batteryIntent))
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

            val batteryIntent = getBatteryIntent()

            val batteryLevel = if (batteryIntent != null) {
                calculateBatteryLevel(batteryIntent)
            } else {
                -1.0
            }

            val isCharging = if (batteryIntent != null) {
                checkBatteryCharging(batteryIntent)
            } else {
                false
            }

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
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun getPlatformNameSync(): String {
        return "android"
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun getAppVersionSync(): String {
        return try {
            val packageInfo = reactContext.packageManager.getPackageInfo(
                reactContext.packageName,
                0
            )

            packageInfo.versionName ?: "Unknown"
        } catch (error: Exception) {
            "Unknown"
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun getBuildNumberSync(): String {
        return try {
            val packageInfo = reactContext.packageManager.getPackageInfo(
                reactContext.packageName,
                0
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (error: Exception) {
            "Unknown"
        }
    }
    }

    private fun getBatteryIntent(): Intent? {
        return reactContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private fun calculateBatteryLevel(batteryIntent: Intent): Double {
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        if (level < 0 || scale <= 0) {
            return -1.0
        }

        return level * 100.0 / scale
    }

    private fun checkBatteryCharging(batteryIntent: Intent): Boolean {
        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }
}