package com.rnnativelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentResultParserTest {

    @Test
    fun parse_shouldReturnSuccess_whenPaymentDataIsValid() {
        val data = mapOf(
            "status" to "SUCCESS",
            "transactionId" to "TRX-1001",
            "orderId" to "ORD-1001",
            "amount" to "1250.50"
        )

        val result = PaymentResultParser.parse(data)

        assertTrue(result is ApiResult.Success)

        val success = result as ApiResult.Success
        assertEquals("TRX-1001", success.data.transactionId)
        assertEquals("ORD-1001", success.data.orderId)
        assertEquals(1250.50, success.data.amount, 0.0)
        assertEquals("SUCCESS", success.data.status)
    }

    @Test
    fun parse_shouldReturnError_whenTransactionIdIsMissing() {
        val data = mapOf(
            "status" to "SUCCESS",
            "orderId" to "ORD-1001",
            "amount" to "1250.50"
        )

        val result = PaymentResultParser.parse(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("MISSING_TRANSACTION_ID", error.code)
        assertEquals("Transaction ID is required for successful payment", error.message)
    }

    @Test
    fun parse_shouldReturnError_whenAmountIsInvalid() {
        val data = mapOf(
            "status" to "SUCCESS",
            "transactionId" to "TRX-1001",
            "orderId" to "ORD-1001",
            "amount" to "abc"
        )

        val result = PaymentResultParser.parse(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("INVALID_AMOUNT", error.code)
    }

    @Test
    fun parse_shouldReturnFailedError_whenPaymentFailed() {
        val data = mapOf(
            "status" to "FAILED",
            "message" to "Insufficient balance"
        )

        val result = PaymentResultParser.parse(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("PAYMENT_FAILED", error.code)
        assertEquals("Insufficient balance", error.message)
    }

    @Test
    fun parse_shouldReturnCancelledError_whenPaymentCancelled() {
        val data = mapOf(
            "status" to "CANCELLED"
        )

        val result = PaymentResultParser.parse(data)

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error
        assertEquals("PAYMENT_CANCELLED", error.code)
        assertEquals("User cancelled the payment", error.message)
    }

    @Test
    fun getDisplayMessage_shouldReturnSuccessMessage_whenResultIsSuccess() {
        val result = ApiResult.Success(
            PaymentInfo(
                transactionId = "TRX-1001",
                orderId = "ORD-1001",
                amount = 1250.50,
                status = "SUCCESS"
            )
        )

        val message = PaymentResultParser.getDisplayMessage(result)

        assertEquals("Payment successful for order ORD-1001", message)
    }

    @Test
    fun getDisplayMessage_shouldReturnLoadingMessage_whenResultIsLoading() {
        val message = PaymentResultParser.getDisplayMessage(ApiResult.Loading)

        assertEquals("Processing payment...", message)
    }
}