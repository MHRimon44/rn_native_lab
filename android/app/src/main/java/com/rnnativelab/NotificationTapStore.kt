package com.rnnativelab

import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object NotificationTapStore {

    data class NotificationTap(
        val id: String,
        val title: String,
        val message: String,
        val source: String,
        val openedAt: String
    )

    private var pendingTap: NotificationTap? = null
    private var eventEmitter: ((NotificationTap) -> Unit)? = null

    fun setEventEmitter(emitter: ((NotificationTap) -> Unit)?) {
        eventEmitter = emitter
    }

    fun saveFromIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        val id = intent.getStringExtra("notification_id") ?: return
        val title = intent.getStringExtra("notification_title") ?: ""
        val message = intent.getStringExtra("notification_message") ?: ""
        val source = intent.getStringExtra("notification_source") ?: "local"

        val tap = NotificationTap(
            id = id,
            title = title,
            message = message,
            source = source,
            openedAt = getCurrentIsoTime()
        )

        pendingTap = tap
        eventEmitter?.invoke(tap)
    }

    fun getPendingTap(): NotificationTap? {
        return pendingTap
    }

    fun clear() {
        pendingTap = null
    }

    private fun getCurrentIsoTime(): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        )
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(System.currentTimeMillis())
    }
}