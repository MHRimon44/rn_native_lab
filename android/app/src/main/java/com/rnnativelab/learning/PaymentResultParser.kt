package com.rnnativelab

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()

    data class Error(
        val code: String,
        val message: String
    ) : ApiResult<Nothing>()

    object Loading : ApiResult<Nothing>()
}

data class PaymentInfo(
    val transactionId: String,
    val orderId: String,
    val amount: Double,
    val status: String
) {
    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val MIN_VALID_AMOUNT = 1.0
    }
}

object PaymentResultParser {

    fun parse(data: Map<String, String>): ApiResult<PaymentInfo> {
        val status = data["status"]?.uppercase() ?: "UNKNOWN"

        return when (status) {
            "SUCCESS" -> parseSuccessPayment(data)

            "FAILED" -> ApiResult.Error(
                code = "PAYMENT_FAILED",
                message = data["message"] ?: "Payment failed"
            )

            "CANCELLED" -> ApiResult.Error(
                code = "PAYMENT_CANCELLED",
                message = "User cancelled the payment"
            )

            else -> ApiResult.Error(
                code = "UNKNOWN_PAYMENT_STATUS",
                message = "Unknown payment status: $status"
            )
        }
    }

    private fun parseSuccessPayment(data: Map<String, String>): ApiResult<PaymentInfo> {
        val transactionId = data["transactionId"]
        val orderId = data["orderId"]
        val amount = data["amount"]?.toDoubleOrNull()

        if (transactionId.isNullOrBlank()) {
            return ApiResult.Error(
                code = "MISSING_TRANSACTION_ID",
                message = "Transaction ID is required for successful payment"
            )
        }

        if (orderId.isNullOrBlank()) {
            return ApiResult.Error(
                code = "MISSING_ORDER_ID",
                message = "Order ID is required for successful payment"
            )
        }

        if (amount == null || amount <= 0.0) {
            return ApiResult.Error(
                code = "INVALID_AMOUNT",
                message = "Valid amount is required for successful payment"
            )
        }

        return ApiResult.Success(
            PaymentInfo(
                transactionId = transactionId,
                orderId = orderId,
                amount = amount,
                status = "SUCCESS"
            )
        )
    }

    fun getDisplayMessage(result: ApiResult<PaymentInfo>): String {
        return when (result) {
            is ApiResult.Success -> {
                "Payment successful for order ${result.data.orderId}"
            }

            is ApiResult.Error -> {
                result.message
            }

            ApiResult.Loading -> {
                "Processing payment..."
            }
        }
    }
}