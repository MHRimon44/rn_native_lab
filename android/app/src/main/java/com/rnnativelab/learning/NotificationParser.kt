package com.rnnativelab

enum class NotificationType {
    ORDER,
    PAYMENT,
    PROMOTION,
    SYSTEM,
    UNKNOWN
}

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val refId: String?,
    val screen: String?,
    val createdAt: String,
    val isRead: Boolean = false
)

object NotificationParser {

    fun parse(data: Map<String, String>): NotificationItem {
        val id = data["notificationId"] ?: System.currentTimeMillis().toString()
        val title = data["title"] ?: "New Notification"
        val body = data["body"] ?: ""
        val type = parseType(data["type"])
        val refId = data["orderId"] ?: data["paymentId"] ?: data["refId"]
        val screen = data["screen"]
        val createdAt = data["createdAt"] ?: ""

        return NotificationItem(
            id = id,
            title = title,
            body = body,
            type = type,
            refId = refId,
            screen = screen,
            createdAt = createdAt,
            isRead = false
        )
    }

    fun parseType(type: String?): NotificationType {
        return when (type?.uppercase()) {
            "ORDER" -> NotificationType.ORDER
            "PAYMENT" -> NotificationType.PAYMENT
            "PROMOTION" -> NotificationType.PROMOTION
            "SYSTEM" -> NotificationType.SYSTEM
            else -> NotificationType.UNKNOWN
        }
    }

    fun markAsRead(notification: NotificationItem): NotificationItem {
        return notification.copy(isRead = true)
    }

    fun getUnreadNotifications(
        notifications: List<NotificationItem>
    ): List<NotificationItem> {
        return notifications.filter { !it.isRead }
    }

    fun countUnread(
        notifications: List<NotificationItem>
    ): Int {
        return notifications.count { !it.isRead }
    }

    fun getOrderNotifications(
        notifications: List<NotificationItem>
    ): List<NotificationItem> {
        return notifications.filter { it.type == NotificationType.ORDER }
    }

    fun sortLatestFirst(
        notifications: List<NotificationItem>
    ): List<NotificationItem> {
        return notifications.sortedByDescending { it.createdAt }
    }

    fun getDisplayTitle(notification: NotificationItem): String {
        return when (notification.type) {
            NotificationType.ORDER -> "Order: ${notification.title}"
            NotificationType.PAYMENT -> "Payment: ${notification.title}"
            NotificationType.PROMOTION -> "Offer: ${notification.title}"
            NotificationType.SYSTEM -> "System: ${notification.title}"
            NotificationType.UNKNOWN -> notification.title
        }
    }
}