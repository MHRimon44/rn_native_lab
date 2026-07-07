package com.rnnativelab

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener

class NativeNotificationModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val CHANNEL_ID = "rn_native_lab_default_channel"
        private const val CHANNEL_NAME = "RNNativeLab Notifications"
        private const val CHANNEL_DESCRIPTION = "Local notifications from native module"
        private const val REQUEST_NOTIFICATION_PERMISSION = 8801
    }

    private var notificationPermissionPromise: Promise? = null

    override fun getName(): String {
        return "NativeNotificationModule"
    }

    @ReactMethod
    fun requestNotificationPermission(promise: Promise) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                promise.resolve(true)
                return
            }

            val permissionStatus = ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                promise.resolve(true)
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
                    "INVALID_ACTIVITY",
                    "Current activity does not support permission request"
                )
                return
            }

            if (notificationPermissionPromise != null) {
                promise.reject(
                    "PERMISSION_REQUEST_IN_PROGRESS",
                    "Another notification permission request is already running"
                )
                return
            }

            notificationPermissionPromise = promise

            activity.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION,
                PermissionListener { requestCode, _, grantResults ->
                    if (requestCode != REQUEST_NOTIFICATION_PERMISSION) {
                        return@PermissionListener false
                    }

                    val pendingPromise = notificationPermissionPromise
                    notificationPermissionPromise = null

                    val granted = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED

                    pendingPromise?.resolve(granted)

                    true
                }
            )
        } catch (error: Exception) {
            notificationPermissionPromise = null

            promise.reject(
                "REQUEST_NOTIFICATION_PERMISSION_ERROR",
                error.message ?: "Failed to request notification permission",
                error
            )
        }
    }

    @ReactMethod
    fun showLocalNotification(
        title: String,
        message: String,
        promise: Promise
    ) {
        try {
            if (title.isBlank()) {
                promise.reject(
                    "INVALID_TITLE",
                    "Notification title cannot be blank"
                )
                return
            }

            if (message.isBlank()) {
                promise.reject(
                    "INVALID_MESSAGE",
                    "Notification message cannot be blank"
                )
                return
            }

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    reactContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                promise.reject(
                    "NOTIFICATION_PERMISSION_DENIED",
                    "Notification permission is not granted"
                )
                return
            }

            createNotificationChannel()

            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

            val launchIntent =
                reactContext.packageManager.getLaunchIntentForPackage(
                    reactContext.packageName
                ) ?: Intent(reactContext, MainActivity::class.java)

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            launchIntent.putExtra("notification_title", title)
            launchIntent.putExtra("notification_message", message)

            val pendingIntentFlags =
                PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }

            val pendingIntent = PendingIntent.getActivity(
                reactContext,
                notificationId,
                launchIntent,
                pendingIntentFlags
            )

            val notification = NotificationCompat.Builder(reactContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(reactContext).notify(
                notificationId,
                notification
            )

            promise.resolve(true)
        } catch (error: SecurityException) {
            promise.reject(
                "NOTIFICATION_SECURITY_ERROR",
                error.message ?: "Notification permission/security error",
                error
            )
        } catch (error: Exception) {
            promise.reject(
                "SHOW_LOCAL_NOTIFICATION_ERROR",
                error.message ?: "Failed to show local notification",
                error
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            reactContext.getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannel(channel)
    }
}