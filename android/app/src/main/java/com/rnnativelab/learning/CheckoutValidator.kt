package com.rnnativelab

class CheckoutValidationException(
    val code: String,
    override val message: String
) : Exception(message)

data class CheckoutRequest(
    val orderId: String,
    val amount: Double,
    val paymentMethod: String?
)

object CheckoutValidator {

    private const val MIN_VALID_AMOUNT = 1.0

    private val supportedPaymentMethods = listOf(
        "CASH",
        "CARD",
        "BKASH",
        "NAGAD",
        "SSL_COMMERZ"
    )

    fun parse(data: Map<String, String>): CheckoutRequest {
        val orderId = data["orderId"] ?: ""
        val amount = data["amount"]?.toDoubleOrNull() ?: 0.0
        val paymentMethod = data["paymentMethod"]

        return CheckoutRequest(
            orderId = orderId,
            amount = amount,
            paymentMethod = paymentMethod
        )
    }

    fun validateOrThrow(request: CheckoutRequest): CheckoutRequest {
        if (request.orderId.isBlank()) {
            throw CheckoutValidationException(
                code = "MISSING_ORDER_ID",
                message = "Order ID is required"
            )
        }

        if (request.amount < MIN_VALID_AMOUNT) {
            throw CheckoutValidationException(
                code = "INVALID_AMOUNT",
                message = "Amount must be greater than or equal to $MIN_VALID_AMOUNT"
            )
        }

        if (request.paymentMethod.isNullOrBlank()) {
            throw CheckoutValidationException(
                code = "MISSING_PAYMENT_METHOD",
                message = "Payment method is required"
            )
        }

        val normalizedPaymentMethod = request.paymentMethod.uppercase()

        if (!supportedPaymentMethods.contains(normalizedPaymentMethod)) {
            throw CheckoutValidationException(
                code = "UNSUPPORTED_PAYMENT_METHOD",
                message = "Unsupported payment method: ${request.paymentMethod}"
            )
        }

        return request.copy(
            paymentMethod = normalizedPaymentMethod
        )
    }

    fun validateSafely(data: Map<String, String>): ApiResult<CheckoutRequest> {
        return try {
            val request = parse(data)
            val validatedRequest = validateOrThrow(request)

            ApiResult.Success(validatedRequest)
        } catch (error: CheckoutValidationException) {
            ApiResult.Error(
                code = error.code,
                message = error.message
            )
        } catch (error: Exception) {
            ApiResult.Error(
                code = "UNKNOWN_CHECKOUT_ERROR",
                message = error.message ?: "Unknown checkout error"
            )
        }
    }
}