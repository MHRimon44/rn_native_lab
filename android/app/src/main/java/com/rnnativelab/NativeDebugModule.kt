package com.rnnativelab

import android.os.Handler
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Callback

import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.modules.core.DeviceEventManagerModule

class NativeDebugModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), PermissionListener {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2001
    }

    private fun createPermissionResultMap(
        permission: String,
        granted: Boolean,
        message: String
    ): WritableMap {
        return Arguments.createMap().apply {
            putString("permission", permission)
            putBoolean("granted", granted)
            putString("status", if (granted) "GRANTED" else "DENIED")
            putString("message", message)
            putString("source", "Kotlin Permission API")
        }
    }

    private var cameraPermissionPromise: Promise? = null

    @ReactMethod
    fun checkCameraPermission(promise: Promise) {
        try {
            val granted = ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            val resultMap = createPermissionResultMap(
                permission = Manifest.permission.CAMERA,
                granted = granted,
                message = if (granted) {
                    "Camera permission already granted"
                } else {
                    "Camera permission not granted"
                }
            )

            promise.resolve(resultMap)
        } catch (error: Exception) {
            promise.reject(
                "CHECK_CAMERA_PERMISSION_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun requestCameraPermission(promise: Promise) {
        try {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                val resultMap = createPermissionResultMap(
                    permission = Manifest.permission.CAMERA,
                    granted = true,
                    message = "Camera permission already granted"
                )

                promise.resolve(resultMap)
                return
            }

            val activity = getCurrentActivity()

            if (activity == null) {
                promise.reject(
                    "NO_ACTIVITY",
                    "Current Android activity is not available"
                )
                return
            }

            if (activity !is PermissionAwareActivity) {
                promise.reject(
                    "ACTIVITY_NOT_PERMISSION_AWARE",
                    "Current activity cannot request permissions"
                )
                return
            }

            if (cameraPermissionPromise != null) {
                promise.reject(
                    "PERMISSION_REQUEST_IN_PROGRESS",
                    "Another camera permission request is already running"
                )
                return
            }

            cameraPermissionPromise = promise

            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE,
                this
            )
        } catch (error: Exception) {
            cameraPermissionPromise = null

            promise.reject(
                "REQUEST_CAMERA_PERMISSION_ERROR",
                error.message,
                error
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode != CAMERA_PERMISSION_REQUEST_CODE) {
            return false
        }

        val granted = grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED

        val resultMap = createPermissionResultMap(
            permission = Manifest.permission.CAMERA,
            granted = granted,
            message = if (granted) {
                "Camera permission granted"
            } else {
                "Camera permission denied"
            }
        )

        cameraPermissionPromise?.resolve(resultMap)
        cameraPermissionPromise = null

        return true
    }

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

    @ReactMethod
    fun getOrderSummary(
        orderId: String,
        amount: Double,
        promise: Promise
    ) {
        try {
            val orderMap = Arguments.createMap().apply {
                putString("orderId", orderId)
                putString("customerName", "SaRa Customer")
                putDouble("amount", amount)
                putString("status", "DELIVERED")
                putBoolean("isHighValue", amount >= 5000.0)
                putString("source", "Kotlin NativeModule")
            }

            promise.resolve(orderMap)
        } catch (error: Exception) {
            promise.reject(
                "ORDER_SUMMARY_ERROR",
                error.message,
                error
            )
        }
    }
    @ReactMethod
    fun getRecentOrders(
        maxCount: Double,
        promise: Promise
    ) {
        try {
            val safeCount = maxCount.toInt().coerceIn(1, 10)

            val orders = listOf(
                mapOf(
                    "orderId" to "ORD-5001",
                    "customerName" to "Customer 1",
                    "amount" to 1200.0,
                    "status" to "DELIVERED"
                ),
                mapOf(
                    "orderId" to "ORD-5002",
                    "customerName" to "Customer 2",
                    "amount" to 6500.0,
                    "status" to "PROCESSING"
                ),
                mapOf(
                    "orderId" to "ORD-5003",
                    "customerName" to "Customer 3",
                    "amount" to 950.0,
                    "status" to "PENDING"
                ),
                mapOf(
                    "orderId" to "ORD-5004",
                    "customerName" to "Customer 4",
                    "amount" to 8000.0,
                    "status" to "DELIVERED"
                )
            )

            val orderArray = Arguments.createArray()

            orders.take(safeCount).forEach { order ->
                val amount = order["amount"] as Double

                val orderMap = Arguments.createMap().apply {
                    putString("orderId", order["orderId"] as String)
                    putString("customerName", order["customerName"] as String)
                    putDouble("amount", amount)
                    putString("status", order["status"] as String)
                    putBoolean("isHighValue", amount >= 5000.0)
                    putString("source", "Kotlin WritableArray")
                }

                orderArray.pushMap(orderMap)
            }

            promise.resolve(orderArray)
        } catch (error: Exception) {
            promise.reject(
                "RECENT_ORDERS_ERROR",
                error.message,
                error
            )
        }
    }
    @ReactMethod
    fun createOrderFromMap(
        orderInput: ReadableMap,
        promise: Promise
    ) {
        try {
            val orderId = orderInput.getOptionalString("orderId") ?: ""
            val customerName = orderInput.getOptionalString("customerName") ?: "Guest Customer"
            val amount = orderInput.getOptionalDouble("amount") ?: 0.0
            val status = orderInput.getOptionalString("status") ?: "PENDING"

            if (orderId.isBlank()) {
                promise.reject(
                    "MISSING_ORDER_ID",
                    "Order ID is required"
                )
                return
            }

            if (amount <= 0.0) {
                promise.reject(
                    "INVALID_AMOUNT",
                    "Amount must be greater than 0"
                )
                return
            }

            val responseMap = Arguments.createMap().apply {
                putString("orderId", orderId)
                putString("customerName", customerName)
                putDouble("amount", amount)
                putString("status", status.uppercase())
                putBoolean("isHighValue", amount >= 5000.0)
                putString("source", "React Native object parsed by Kotlin")
                putString("message", "Order received successfully in Kotlin")
            }

            promise.resolve(responseMap)
        } catch (error: Exception) {
            promise.reject(
                "CREATE_ORDER_FROM_MAP_ERROR",
                error.message,
                error
            )
        }
    }
    @ReactMethod
    fun summarizeOrdersFromArray(
        ordersInput: ReadableArray,
        promise: Promise
    ) {
        try {
            var totalAmount = 0.0
            var highValueOrders = 0
            var deliveredOrders = 0

            for (index in 0 until ordersInput.size()) {
                val orderMap = ordersInput.getMap(index)

                val amount = orderMap?.getOptionalDouble("amount") ?: 0.0
                val status = orderMap?.getOptionalString("status") ?: "UNKNOWN"

                totalAmount += amount

                if (amount >= 5000.0) {
                    highValueOrders++
                }

                if (status.uppercase() == "DELIVERED") {
                    deliveredOrders++
                }
            }

            val responseMap = Arguments.createMap().apply {
                putInt("totalOrders", ordersInput.size())
                putDouble("totalAmount", totalAmount)
                putInt("highValueOrders", highValueOrders)
                putInt("deliveredOrders", deliveredOrders)
                putString("source", "Kotlin ReadableArray")
                putString("message", "Order array summarized successfully in Kotlin")
            }

            promise.resolve(responseMap)
        } catch (error: Exception) {
            promise.reject(
                "SUMMARIZE_ORDERS_ERROR",
                error.message,
                error
            )
        }
    }
        private fun sendEvent(eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
    @ReactMethod
    fun emitTestEvent(message: String, promise: Promise) {
        try {
            val eventMap = Arguments.createMap().apply {
                putString("message", message)
                putString("source", "Kotlin EventEmitter")
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            }

            sendEvent("NativeDebugEvent", eventMap)

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "EMIT_TEST_EVENT_ERROR",
                error.message,
                error
            )
        }
    }
    @ReactMethod
    fun startFakeOrderSync(orderId: String, promise: Promise) {
        try {
            if (orderId.isBlank()) {
                promise.reject(
                    "MISSING_ORDER_ID",
                    "Order ID is required"
                )
                return
            }

            val handler = Handler(Looper.getMainLooper())
            val progressList = listOf(0, 25, 50, 75, 100)

            progressList.forEachIndexed { index, progress ->
                handler.postDelayed({
                    val eventMap = Arguments.createMap().apply {
                        putString("orderId", orderId)
                        putInt("progress", progress)
                        putBoolean("isCompleted", progress == 100)
                        putString(
                            "message",
                            if (progress == 100) {
                                "Order sync completed"
                            } else {
                                "Order sync in progress"
                            }
                        )
                    }

                    sendEvent("OrderSyncProgress", eventMap)
                }, index * 700L)
            }

            promise.resolve("Order sync started for $orderId")
        } catch (error: Exception) {
            promise.reject(
                "START_ORDER_SYNC_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun validateCouponWithCallback(
        couponCode: String,
        amount: Double,
        successCallback: Callback,
        errorCallback: Callback
    ) {
        try {
            if (couponCode.isBlank()) {
                errorCallback.invoke(
                    "MISSING_COUPON_CODE",
                    "Coupon code is required"
                )
                return
            }

            if (amount <= 0.0) {
                errorCallback.invoke(
                    "INVALID_AMOUNT",
                    "Amount must be greater than 0"
                )
                return
            }

            val handler = Handler(Looper.getMainLooper())

            handler.postDelayed({
                val normalizedCouponCode = couponCode.uppercase()

                val discountPercent = when (normalizedCouponCode) {
                    "SARA10" -> 10.0
                    "VIP20" -> 20.0
                    "FREESHIP" -> 5.0
                    else -> null
                }

                if (discountPercent == null) {
                    errorCallback.invoke(
                        "INVALID_COUPON_CODE",
                        "Coupon code is invalid"
                    )
                    return@postDelayed
                }

                val discountAmount = amount * discountPercent / 100
                val finalAmount = amount - discountAmount

                val resultMap = Arguments.createMap().apply {
                    putString("couponCode", normalizedCouponCode)
                    putDouble("amount", amount)
                    putDouble("discountPercent", discountPercent)
                    putDouble("discountAmount", discountAmount)
                    putDouble("finalAmount", finalAmount)
                    putString("message", "Coupon applied successfully")
                    putString("source", "Kotlin Callback")
                }

                successCallback.invoke(resultMap)
            }, 700L)
        } catch (error: Exception) {
            errorCallback.invoke(
                "COUPON_VALIDATION_ERROR",
                error.message ?: "Failed to validate coupon"
            )
        }
    }
}
private fun ReadableMap.getOptionalString(key: String): String? {
    return if (hasKey(key) && !isNull(key)) {
        getString(key)
    } else {
        null
    }
}

private fun ReadableMap.getOptionalDouble(key: String): Double? {
    return if (hasKey(key) && !isNull(key)) {
        getDouble(key)
    } else {
        null
    }
}