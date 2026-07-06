package com.rnnativelab

import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
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
import com.facebook.react.bridge.ActivityEventListener

import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.modules.core.DeviceEventManagerModule

class NativeDebugModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), PermissionListener, ActivityEventListener {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2001
        private const val CAMERA_CAPTURE_REQUEST_CODE = 3001
        private const val FULL_SIZE_CAMERA_REQUEST_CODE = 3002
    }

    private var cameraPermissionPromise: Promise? = null
    private var cameraCapturePromise: Promise? = null
    private var fullSizeCameraPromise: Promise? = null
    private var currentPhotoPath: String? = null
    private var currentPhotoUri: Uri? = null
    private var currentPhotoFileName: String? = null
    
    init {
        reactContext.addActivityEventListener(this)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date())

        val storageDir = reactContext.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )

        val imageFile = File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        )

        currentPhotoPath = imageFile.absolutePath
        currentPhotoFileName = imageFile.name

        return imageFile
    }

    private fun createFullSizeCameraResultMap(
        success: Boolean,
        message: String,
        filePath: String? = null,
        fileUri: String? = null,
        fileName: String? = null
    ): WritableMap {
        return Arguments.createMap().apply {
            putBoolean("success", success)
            putString("message", message)
            putString("filePath", filePath)
            putString("fileUri", fileUri)
            putString("fileName", fileName)
            putString("source", "Kotlin FileProvider Camera")
        }
    }

    @ReactMethod
    fun openCameraFullSize(promise: Promise) {
        try {
            val permissionGranted = ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                promise.reject(
                    "CAMERA_PERMISSION_NOT_GRANTED",
                    "Camera permission is required before opening camera"
                )
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

            if (fullSizeCameraPromise != null) {
                promise.reject(
                    "FULL_SIZE_CAMERA_IN_PROGRESS",
                    "Another full-size camera capture request is already running"
                )
                return
            }

            val photoFile = createImageFile()

            val photoUri = FileProvider.getUriForFile(
                reactContext,
                "${reactContext.packageName}.fileprovider",
                photoFile
            )

            currentPhotoUri = photoUri

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            fullSizeCameraPromise = promise

            try {
                activity.startActivityForResult(
                    cameraIntent,
                    FULL_SIZE_CAMERA_REQUEST_CODE
                )
            } catch (error: Exception) {
                fullSizeCameraPromise = null
                currentPhotoPath = null
                currentPhotoUri = null
                currentPhotoFileName = null

                promise.reject(
                    "OPEN_FULL_SIZE_CAMERA_ERROR",
                    error.message ?: "Failed to open full-size camera",
                    error
                )
            }
        } catch (error: Exception) {
            fullSizeCameraPromise = null
            currentPhotoPath = null
            currentPhotoUri = null
            currentPhotoFileName = null

            promise.reject(
                "CREATE_IMAGE_FILE_ERROR",
                error.message ?: "Failed to create image file",
                error
            )
        }
    }

    private fun createCameraResultMap(
        success: Boolean,
        message: String,
        width: Int = 0,
        height: Int = 0
    ): WritableMap {
        return Arguments.createMap().apply {
            putBoolean("success", success)
            putString("message", message)
            putInt("width", width)
            putInt("height", height)
            putString("source", "Kotlin Camera Intent")
        }
    }

   @ReactMethod
    fun openCamera(promise: Promise) {
        try {
            val permissionGranted = ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                promise.reject(
                    "CAMERA_PERMISSION_NOT_GRANTED",
                    "Camera permission is required before opening camera"
                )
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

            if (cameraCapturePromise != null) {
                promise.reject(
                    "CAMERA_CAPTURE_IN_PROGRESS",
                    "Another camera capture request is already running"
                )
                return
            }

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            cameraCapturePromise = promise

            try {
                activity.startActivityForResult(
                    cameraIntent,
                    CAMERA_CAPTURE_REQUEST_CODE
                )
            } catch (error: Exception) {
                cameraCapturePromise = null

                promise.reject(
                    "NO_CAMERA_APP",
                    "No camera app is available on this emulator/device",
                    error
                )
            }
        } catch (error: Exception) {
            cameraCapturePromise = null

            promise.reject(
                "OPEN_CAMERA_ERROR",
                error.message ?: "Failed to open camera",
                error
            )
        }
    }
        
    override fun onNewIntent(intent: Intent) {
        // Not needed for camera capture in this lesson.
    }

   override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE) {
            val promise = cameraCapturePromise
            cameraCapturePromise = null

            if (promise == null) {
                return
            }

            if (resultCode != Activity.RESULT_OK) {
                val resultMap = createCameraResultMap(
                    success = false,
                    message = "Camera capture cancelled"
                )

                promise.resolve(resultMap)
                return
            }

            val bitmap = data?.extras?.get("data") as? Bitmap

            if (bitmap == null) {
                promise.reject(
                    "NO_IMAGE_DATA",
                    "Camera did not return image data"
                )
                return
            }

            val resultMap = createCameraResultMap(
                success = true,
                message = "Photo captured successfully",
                width = bitmap.width,
                height = bitmap.height
            )

            promise.resolve(resultMap)
            return
        }

        if (requestCode == FULL_SIZE_CAMERA_REQUEST_CODE) {
            val promise = fullSizeCameraPromise
            fullSizeCameraPromise = null

            if (promise == null) {
                return
            }

            if (resultCode != Activity.RESULT_OK) {
                val resultMap = createFullSizeCameraResultMap(
                    success = false,
                    message = "Full-size camera capture cancelled",
                    filePath = currentPhotoPath,
                    fileUri = currentPhotoUri?.toString(),
                    fileName = currentPhotoFileName
                )

                currentPhotoPath = null
                currentPhotoUri = null
                currentPhotoFileName = null

                promise.resolve(resultMap)
                return
            }

            val resultMap = createFullSizeCameraResultMap(
                success = true,
                message = "Full-size photo captured successfully",
                filePath = currentPhotoPath,
                fileUri = currentPhotoUri?.toString(),
                fileName = currentPhotoFileName
            )

            currentPhotoPath = null
            currentPhotoUri = null
            currentPhotoFileName = null

            promise.resolve(resultMap)
            return
        }
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