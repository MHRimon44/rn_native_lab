package com.rnnativelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationParserTest {

    @Test
    fun parse_shouldReturnNotification_whenDataIsValid() {
        val data = mapOf(
            "notificationId" to "N-1001",
            "title" to "Order Delivered",
            "body" to "Your order has been delivered",
            "type" to "ORDER",
            "orderId" to "ORD-1001",
            "screen" to "OrderDetails",
            "createdAt" to "2026-07-05T10:00:00Z"
        )

        val notification = NotificationParser.parse(data)

        assertEquals("N-1001", notification.id)
        assertEquals("Order Delivered", notification.title)
        assertEquals("Your order has been delivered", notification.body)
        assertEquals(NotificationType.ORDER, notification.type)
        assertEquals("ORD-1001", notification.refId)
        assertEquals("OrderDetails", notification.screen)
        assertEquals("2026-07-05T10:00:00Z", notification.createdAt)
        assertFalse(notification.isRead)
    }

    @Test
    fun parse_shouldUseDefaultValues_whenDataIsMissing() {
        val data = emptyMap<String, String>()

        val notification = NotificationParser.parse(data)

        assertEquals("New Notification", notification.title)
        assertEquals("", notification.body)
        assertEquals(NotificationType.UNKNOWN, notification.type)
        assertEquals(null, notification.refId)
        assertEquals(null, notification.screen)
        assertFalse(notification.isRead)
    }

    @Test
    fun parseType_shouldReturnPayment_whenTypeIsLowercase() {
        val type = NotificationParser.parseType("payment")

        assertEquals(NotificationType.PAYMENT, type)
    }

    @Test
    fun markAsRead_shouldReturnNotificationWithIsReadTrue() {
        val notification = NotificationItem(
            id = "N-1001",
            title = "Order Delivered",
            body = "Your order has been delivered",
            type = NotificationType.ORDER,
            refId = "ORD-1001",
            screen = "OrderDetails",
            createdAt = "2026-07-05T10:00:00Z",
            isRead = false
        )

        val updated = NotificationParser.markAsRead(notification)

        assertTrue(updated.isRead)
        assertFalse(notification.isRead)
    }

    @Test
    fun countUnread_shouldReturnUnreadCount() {
        val notifications = listOf(
            NotificationItem(
                id = "N-1",
                title = "Title 1",
                body = "Body 1",
                type = NotificationType.ORDER,
                refId = "ORD-1",
                screen = "OrderDetails",
                createdAt = "2026-07-05T10:00:00Z",
                isRead = false
            ),
            NotificationItem(
                id = "N-2",
                title = "Title 2",
                body = "Body 2",
                type = NotificationType.PAYMENT,
                refId = "PAY-1",
                screen = "PaymentDetails",
                createdAt = "2026-07-05T11:00:00Z",
                isRead = true
            ),
            NotificationItem(
                id = "N-3",
                title = "Title 3",
                body = "Body 3",
                type = NotificationType.SYSTEM,
                refId = null,
                screen = null,
                createdAt = "2026-07-05T12:00:00Z",
                isRead = false
            )
        )

        val unreadCount = NotificationParser.countUnread(notifications)

        assertEquals(2, unreadCount)
    }

    @Test
    fun getOrderNotifications_shouldReturnOnlyOrderNotifications() {
        val notifications = listOf(
            NotificationItem(
                id = "N-1",
                title = "Order Delivered",
                body = "Body",
                type = NotificationType.ORDER,
                refId = "ORD-1",
                screen = "OrderDetails",
                createdAt = "2026-07-05T10:00:00Z"
            ),
            NotificationItem(
                id = "N-2",
                title = "Payment Success",
                body = "Body",
                type = NotificationType.PAYMENT,
                refId = "PAY-1",
                screen = "PaymentDetails",
                createdAt = "2026-07-05T11:00:00Z"
            )
        )

        val result = NotificationParser.getOrderNotifications(notifications)

        assertEquals(1, result.size)
        assertEquals(NotificationType.ORDER, result[0].type)
    }

    @Test
    fun sortLatestFirst_shouldReturnLatestNotificationFirst() {
        val notifications = listOf(
            NotificationItem(
                id = "N-1",
                title = "Old",
                body = "Body",
                type = NotificationType.SYSTEM,
                refId = null,
                screen = null,
                createdAt = "2026-07-05T10:00:00Z"
            ),
            NotificationItem(
                id = "N-2",
                title = "Latest",
                body = "Body",
                type = NotificationType.SYSTEM,
                refId = null,
                screen = null,
                createdAt = "2026-07-05T12:00:00Z"
            )
        )

        val result = NotificationParser.sortLatestFirst(notifications)

        assertEquals("N-2", result[0].id)
        assertEquals("N-1", result[1].id)
    }
}