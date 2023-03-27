package com.mizuho.limitorderbook

import java.time.LocalDateTime

data class Order(
    val id: Long,
    val side: Char='c',
    val price: Double=0.0,
    var size: Long?=0L,
    val createdAt:LocalDateTime=LocalDateTime.now(),
) : Comparable<Order> {
    override fun compareTo(other: Order): Int {
        return when (side) {
            'O' -> {
                price.compareTo(other.price)
            }
            'B' -> {
                other.price.compareTo(price)
            }
            else -> throw IllegalArgumentException("Invalid order side: $side")
        }
    }
}