package com.rnnativelab

enum class OrderStatus {
    PENDING,
    PROCESSING,
    DELIVERED,
    CANCELLED,
    UNKNOWN
}

data class OrderItem(
    val orderId: String,
    val status: OrderStatus,
    val amount: Double,
    val customerName: String?
)

object OrderParser {

    fun parse(data: Map<String, String>): OrderItem {
        val orderId = data["orderId"] ?: "UNKNOWN"
        val status = parseStatus(data["status"])
        val amount = data["amount"]?.toDoubleOrNull() ?: 0.0
        val customerName = data["customerName"]

        return OrderItem(
            orderId = orderId,
            status = status,
            amount = amount,
            customerName = customerName
        )
    }

    fun parseStatus(status: String?): OrderStatus {
        return when (status?.uppercase()) {
            "PENDING" -> OrderStatus.PENDING
            "PROCESSING" -> OrderStatus.PROCESSING
            "DELIVERED" -> OrderStatus.DELIVERED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.UNKNOWN
        }
    }

    fun isDelivered(order: OrderItem): Boolean {
        return order.status == OrderStatus.DELIVERED
    }

    fun getDisplayTitle(order: OrderItem): String {
        val customer = order.customerName ?: "Guest Customer"
        return "$customer - ${order.orderId}"
    }

    fun isHighValueOrder(order: OrderItem): Boolean {
        return order.amount >= 5000.0
    }
}