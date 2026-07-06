package com.rnnativelab

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class NativeDebugModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "NativeDebugModule"
    }

    @ReactMethod
    fun getNativeGreeting(name: String, promise: Promise) {
        try {
            val message = "Hello $name from Kotlin NativeModule"
            promise.resolve(message)
        } catch (error: Exception) {
            promise.reject(
                "GREETING_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun openDebugScreen(
        orderId: String,
        amount: String,
        promise: Promise
    ) {
        try {
            val activity = getCurrentActivity()
            val context = activity ?: reactContext

            NativeActivityLauncher.openDebugScreen(
                context = context,
                orderId = orderId,
                source = "React Native Button",
                amount = amount
            )

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "OPEN_DEBUG_SCREEN_ERROR",
                error.message,
                error
            )
        }
    }
}