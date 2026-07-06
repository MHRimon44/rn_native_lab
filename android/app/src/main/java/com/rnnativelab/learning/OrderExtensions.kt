package com.rnnativelab

fun String?.toOrderStatus(): OrderStatus {
    return when (this?.uppercase()) {
        "PENDING" -> OrderStatus.PENDING
        "PROCESSING" -> OrderStatus.PROCESSING
        "DELIVERED" -> OrderStatus.DELIVERED
        "CANCELLED" -> OrderStatus.CANCELLED
        else -> OrderStatus.UNKNOWN
    }
}

fun String?.toSafeDouble(defaultValue: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: defaultValue
}

fun OrderItem.isCompleted(): Boolean {
    return this.status == OrderStatus.DELIVERED
}

fun OrderItem.toDisplayText(): String {
    val customer = this.customerName ?: "Guest Customer"
    return "$customer - ${this.orderId} - ${this.status}"
}