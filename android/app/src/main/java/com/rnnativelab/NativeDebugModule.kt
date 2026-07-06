package com.rnnativelab
import com.facebook.react.bridge.Arguments
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
}