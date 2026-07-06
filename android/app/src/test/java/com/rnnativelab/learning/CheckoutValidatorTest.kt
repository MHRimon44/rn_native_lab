package com.rnnativelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CheckoutValidatorTest {

    @Test
    fun validateSafely_shouldReturnSuccess_whenDataIsValid() {
        val data = mapOf(
            "orderId" to "ORD-1001",
            "amount" to "599.50",
            "paymentMethod" to "bkash"
        )

        val result = CheckoutValidator.validateSafely(data)

        assertTrue(result is ApiResult.Success)

        val success = result as ApiResult.Success
        assertEquals("ORD-1001", success.data.orderId)
        assertEquals(599.50, success.data.amount, 0.0)
        assertEquals("BKASH", success.data.paymentMethod)
    }

    @Test
    fun validateSafely_shouldReturnError_whenOrderIdIsMissing() {
        val data = mapOf(
            "amount" to "599.50",
            "paymentMethod" to "BKASH"
        )

        val result = CheckoutValidator.validateSafely(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("MISSING_ORDER_ID", error.code)
        assertEquals("Order ID is required", error.message)
    }

    @Test
    fun validateSafely_shouldReturnError_whenAmountIsInvalid() {
        val data = mapOf(
            "orderId" to "ORD-1001",
            "amount" to "0",
            "paymentMethod" to "BKASH"
        )

        val result = CheckoutValidator.validateSafely(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("INVALID_AMOUNT", error.code)
    }

    @Test
    fun validateSafely_shouldReturnError_whenPaymentMethodIsMissing() {
        val data = mapOf(
            "orderId" to "ORD-1001",
            "amount" to "599.50"
        )

        val result = CheckoutValidator.validateSafely(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("MISSING_PAYMENT_METHOD", error.code)
    }

    @Test
    fun validateSafely_shouldReturnError_whenPaymentMethodIsUnsupported() {
        val data = mapOf(
            "orderId" to "ORD-1001",
            "amount" to "599.50",
            "paymentMethod" to "PAYPAL"
        )

        val result = CheckoutValidator.validateSafely(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("UNSUPPORTED_PAYMENT_METHOD", error.code)
        assertEquals("Unsupported payment method: PAYPAL", error.message)
    }

    @Test
    fun validateOrThrow_shouldThrowCheckoutValidationException_whenOrderIdIsMissing() {
        val request = CheckoutRequest(
            orderId = "",
            amount = 599.50,
            paymentMethod = "BKASH"
        )

        try {
            CheckoutValidator.validateOrThrow(request)
            fail("Expected CheckoutValidationException")
        } catch (error: CheckoutValidationException) {
            assertEquals("MISSING_ORDER_ID", error.code)
            assertEquals("Order ID is required", error.message)
        }
    }
}