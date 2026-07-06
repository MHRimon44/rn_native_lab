package com.rnnativelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderExtensionsTest {

    @Test
    fun toOrderStatus_shouldReturnDelivered_whenStringIsLowercase() {
        val result = "delivered".toOrderStatus()

        assertEquals(OrderStatus.DELIVERED, result)
    }

    @Test
    fun toOrderStatus_shouldReturnUnknown_whenStringIsNull() {
        val status: String? = null

        val result = status.toOrderStatus()

        assertEquals(OrderStatus.UNKNOWN, result)
    }

    @Test
    fun toSafeDouble_shouldReturnDouble_whenStringIsValid() {
        val result = "599.50".toSafeDouble()

        assertEquals(599.50, result, 0.0)
    }

    @Test
    fun toSafeDouble_shouldReturnDefault_whenStringIsInvalid() {
        val result = "abc".toSafeDouble()

        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun toSafeDouble_shouldReturnCustomDefault_whenStringIsNull() {
        val amount: String? = null

        val result = amount.toSafeDouble(defaultValue = 100.0)

        assertEquals(100.0, result, 0.0)
    }

    @Test
    fun isCompleted_shouldReturnTrue_whenOrderIsDelivered() {
        val order = OrderItem(
            orderId = "ORD-1001",
            status = OrderStatus.DELIVERED,
            amount = 599.50,
            customerName = "Mehedi"
        )

        val result = order.isCompleted()

        assertTrue(result)
    }

    @Test
    fun isCompleted_shouldReturnFalse_whenOrderIsPending() {
        val order = OrderItem(
            orderId = "ORD-1002",
            status = OrderStatus.PENDING,
            amount = 599.50,
            customerName = "Mehedi"
        )

        val result = order.isCompleted()

        assertFalse(result)
    }

    @Test
    fun toDisplayText_shouldReturnFormattedText() {
        val order = OrderItem(
            orderId = "ORD-1003",
            status = OrderStatus.PROCESSING,
            amount = 1000.0,
            customerName = null
        )

        val result = order.toDisplayText()

        assertEquals("Guest Customer - ORD-1003 - PROCESSING", result)
    }
}