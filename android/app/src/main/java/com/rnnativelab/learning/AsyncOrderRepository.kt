package com.rnnativelab

import kotlinx.coroutines.delay

data class OrderDetails(
    val orderId: String,
    val customerName: String,
    val amount: Double,
    val status: OrderStatus
)

object AsyncOrderRepository {

    suspend fun fetchOrderDetails(orderId: String): ApiResult<OrderDetails> {
        delay(500)

        if (orderId.isBlank()) {
            return ApiResult.Error(
                code = "MISSING_ORDER_ID",
                message = "Order ID is required"
            )
        }

        if (orderId == "ORD-404") {
            return ApiResult.Error(
                code = "ORDER_NOT_FOUND",
                message = "Order was not found"
            )
        }

        val orderDetails = OrderDetails(
            orderId = orderId,
            customerName = "SaRa Customer",
            amount = 599.50,
            status = OrderStatus.DELIVERED
        )

        return ApiResult.Success(orderDetails)
    }

    suspend fun syncOrder(orderId: String): ApiResult<String> {
        delay(300)

        val orderResult = fetchOrderDetails(orderId)

        return when (orderResult) {
            is ApiResult.Success -> {
                ApiResult.Success("Order ${orderResult.data.orderId} synced successfully")
            }

            is ApiResult.Error -> {
                ApiResult.Error(
                    code = orderResult.code,
                    message = orderResult.message
                )
            }

            ApiResult.Loading -> {
                ApiResult.Error(
                    code = "INVALID_STATE",
                    message = "Order sync cannot start from loading state"
                )
            }
        }
    }
}