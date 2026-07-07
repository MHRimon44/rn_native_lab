package com.rnnativelab

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NativeNotificationModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val CHANNEL_ID = "rn_native_lab_default_channel"
        private const val CHANNEL_NAME = "RNNativeLab Notifications"
        private const val CHANNEL_DESCRIPTION = "Local notifications from native module"

        private const val REQUEST_NOTIFICATION_PERMISSION = 8801

        private const val DATABASE_NAME = "rn_native_lab_notifications.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NOTIFICATIONS = "notifications"
    }

    private var notificationPermissionPromise: Promise? = null

    private val dbHelper: NotificationDbHelper by lazy {
        NotificationDbHelper(reactContext)
    }

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
                object : PermissionListener {
                    override fun onRequestPermissionsResult(
                        requestCode: Int,
                        permissions: Array<String>,
                        grantResults: IntArray
                    ): Boolean {
                        if (requestCode != REQUEST_NOTIFICATION_PERMISSION) {
                            return false
                        }

                        val pendingPromise = notificationPermissionPromise
                        notificationPermissionPromise = null

                        val granted = grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED

                        pendingPromise?.resolve(granted)

                        return true
                    }
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
            val inboxId = UUID.randomUUID().toString()
            val createdAt = getCurrentIsoTime()

            saveNotificationToDatabase(
                id = inboxId,
                title = title.trim(),
                message = message.trim(),
                isRead = false,
                source = "local",
                createdAt = createdAt
            )

            val launchIntent =
                reactContext.packageManager.getLaunchIntentForPackage(
                    reactContext.packageName
                ) ?: Intent(reactContext, MainActivity::class.java)

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            launchIntent.putExtra("notification_id", inboxId)
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

    @ReactMethod
    fun saveNotification(notification: ReadableMap, promise: Promise) {
        try {
            val title = getStringFromMap(notification, "title")
            val message = getStringFromMap(notification, "message")

            if (title.isBlank()) {
                promise.reject("INVALID_TITLE", "Notification title cannot be blank")
                return
            }

            if (message.isBlank()) {
                promise.reject("INVALID_MESSAGE", "Notification message cannot be blank")
                return
            }

            val id = getOptionalStringFromMap(notification, "id")
                ?: UUID.randomUUID().toString()

            val source = getOptionalStringFromMap(notification, "source")
                ?: "local"

            val createdAt = getOptionalStringFromMap(notification, "createdAt")
                ?: getCurrentIsoTime()

            val isRead = if (
                notification.hasKey("isRead") &&
                !notification.isNull("isRead")
            ) {
                notification.getBoolean("isRead")
            } else {
                false
            }

            saveNotificationToDatabase(
                id = id,
                title = title.trim(),
                message = message.trim(),
                isRead = isRead,
                source = source,
                createdAt = createdAt
            )

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "SAVE_NOTIFICATION_ERROR",
                error.message ?: "Failed to save notification",
                error
            )
        }
    }

    @ReactMethod
    fun getNotifications(promise: Promise) {
        try {
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                TABLE_NOTIFICATIONS,
                arrayOf("id", "title", "message", "is_read", "source", "created_at"),
                null,
                null,
                null,
                null,
                "created_at DESC"
            )

            val notificationsArray = Arguments.createArray()

            cursor.use {
                while (it.moveToNext()) {
                    val notificationMap = Arguments.createMap().apply {
                        putString("id", it.getString(it.getColumnIndexOrThrow("id")))
                        putString("title", it.getString(it.getColumnIndexOrThrow("title")))
                        putString("message", it.getString(it.getColumnIndexOrThrow("message")))
                        putBoolean(
                            "isRead",
                            it.getInt(it.getColumnIndexOrThrow("is_read")) == 1
                        )
                        putString("source", it.getString(it.getColumnIndexOrThrow("source")))
                        putString("createdAt", it.getString(it.getColumnIndexOrThrow("created_at")))
                    }

                    notificationsArray.pushMap(notificationMap)
                }
            }

            promise.resolve(notificationsArray)
        } catch (error: Exception) {
            promise.reject(
                "GET_NOTIFICATIONS_ERROR",
                error.message ?: "Failed to get notifications",
                error
            )
        }
    }

    @ReactMethod
    fun markAsRead(id: String, promise: Promise) {
        try {
            if (id.isBlank()) {
                promise.reject("INVALID_ID", "Notification id cannot be blank")
                return
            }

            val values = ContentValues().apply {
                put("is_read", 1)
            }

            dbHelper.writableDatabase.update(
                TABLE_NOTIFICATIONS,
                values,
                "id = ?",
                arrayOf(id)
            )

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "MARK_NOTIFICATION_READ_ERROR",
                error.message ?: "Failed to mark notification as read",
                error
            )
        }
    }

    @ReactMethod
    fun clearNotifications(promise: Promise) {
        try {
            dbHelper.writableDatabase.delete(
                TABLE_NOTIFICATIONS,
                null,
                null
            )

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "CLEAR_NOTIFICATIONS_ERROR",
                error.message ?: "Failed to clear notifications",
                error
            )
        }
    }

    private fun saveNotificationToDatabase(
        id: String,
        title: String,
        message: String,
        isRead: Boolean,
        source: String,
        createdAt: String
    ) {
        val values = ContentValues().apply {
            put("id", id)
            put("title", title)
            put("message", message)
            put("is_read", if (isRead) 1 else 0)
            put("source", source)
            put("created_at", createdAt)
        }

        dbHelper.writableDatabase.insertWithOnConflict(
            TABLE_NOTIFICATIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    private fun getStringFromMap(
        map: ReadableMap,
        key: String
    ): String {
        if (!map.hasKey(key) || map.isNull(key)) {
            return ""
        }

        return map.getString(key) ?: ""
    }

    private fun getOptionalStringFromMap(
        map: ReadableMap,
        key: String
    ): String? {
        if (!map.hasKey(key) || map.isNull(key)) {
            return null
        }

        return map.getString(key)
    }

    private fun getCurrentIsoTime(): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        )
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(System.currentTimeMillis())
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

    private class NotificationDbHelper(
        context: Context
    ) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_NOTIFICATIONS (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    source TEXT NOT NULL DEFAULT 'local',
                    created_at TEXT NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS idx_notifications_created_at
                ON $TABLE_NOTIFICATIONS(created_at DESC)
                """.trimIndent()
            )
        }

        override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
            onCreate(db)
        }
    }
}