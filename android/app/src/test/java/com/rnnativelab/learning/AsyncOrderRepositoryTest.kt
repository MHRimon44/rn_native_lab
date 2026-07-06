package com.rnnativelab

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AsyncOrderRepositoryTest {

    @Test
    fun fetchOrderDetails_shouldReturnSuccess_whenOrderIdIsValid() = runTest {
        val result = AsyncOrderRepository.fetchOrderDetails("ORD-1001")

        println("Fetch order result: $result")

        assertTrue(result is ApiResult.Success)

        val success = result as ApiResult.Success
        assertEquals("ORD-1001", success.data.orderId)
        assertEquals("SaRa Customer", success.data.customerName)
        assertEquals(599.50, success.data.amount, 0.0)
        assertEquals(OrderStatus.DELIVERED, success.data.status)
    }

    @Test
    fun fetchOrderDetails_shouldReturnError_whenOrderIdIsBlank() = runTest {
        val result = AsyncOrderRepository.fetchOrderDetails("")

        println("Blank order result: $result")

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("MISSING_ORDER_ID", error.code)
        assertEquals("Order ID is required", error.message)
    }

    @Test
    fun fetchOrderDetails_shouldReturnError_whenOrderNotFound() = runTest {
        val result = AsyncOrderRepository.fetchOrderDetails("ORD-404")

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("ORDER_NOT_FOUND", error.code)
        assertEquals("Order was not found", error.message)
    }

    @Test
    fun syncOrder_shouldReturnSuccessMessage_whenOrderIsValid() = runTest {
        val result = AsyncOrderRepository.syncOrder("ORD-1001")

        println("Sync order result: $result")

        assertTrue(result is ApiResult.Success)

        val success = result as ApiResult.Success
        assertEquals("Order ORD-1001 synced successfully", success.data)
    }

    @Test
    fun syncOrder_shouldReturnError_whenOrderIsNotFound() = runTest {
        val result = AsyncOrderRepository.syncOrder("ORD-404")

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("ORDER_NOT_FOUND", error.code)
    }
}