package com.mizuho.limitorderbook

import com.mizuho.limitorderbook.exception.InvalidLevelException
import com.mizuho.limitorderbook.exception.LevelNotExistException
import com.mizuho.limitorderbook.exception.OrderAlreadyExistException
import java.util.SortedMap

class OrderBook(
    val bids: SortedMap<Order, List<Order>> = sortedMapOf(),
    val offers: SortedMap<Order, List<Order>> = sortedMapOf(),
    val allOrders: SortedMap<Long, Order> = sortedMapOf(),
) {

    fun addOrder(order: Order) {
        if (allOrders.containsKey(order.id))
            throw OrderAlreadyExistException("Order with id: ${order.id} already exists")

        allOrders[order.id] = order
        when (order.side) {
            'B' -> {
                if (bids.containsKey(order)) {
                    bids[order] = bids[order]!!.plus(order)
                } else {
                    bids[order] = listOf(order)
                }
            }

            'O' -> {
                if (offers.containsKey(order)) {
                    offers[order] = offers[order]!!.plus(order)
                } else {
                    offers[order] = listOf(order)
                }
            }

            else -> throw IllegalArgumentException("Invalid order side: ${order.side}")
        }
    }

    fun removeOrder(id: Long) {
        allOrders.remove(id)?.let { order ->
            when (order.side) {
                'B' -> {
                    if (bids[order]!!.size > 1) {
                        bids[order] = bids[order]!!.minus(order)
                    } else {
                        bids.remove(order)
                    }
                }

                'O' -> {
                    if (offers[order]!!.size > 1) {
                        offers[order] = offers[order]!!.minus(order)
                    } else {
                        offers.remove(order)
                    }
                }

                else -> throw IllegalArgumentException("Invalid order side: ${order.side}")
            }

        }
    }

    fun modifyOrder(id: Long, size: Long) {
        // Since bids and offers maps contains same references for the orders in allOrders map,
        // we don't need to update the bids and offers maps
        allOrders[id]?.size = size
    }

    fun getPrice(side: Char, level: Int): Double {
        if (level < 1) throw InvalidLevelException("Invalid level: ${level}")
        return when (side) {
            'B' -> {
                if (bids.size < level)
                    throw LevelNotExistException("No bids at level: $level")
                bids.toList()[level - 1].first.price
            }

            'O' -> {
                if (offers.size < level)
                    throw LevelNotExistException("No offers at level: $level")
                offers.toList()[level - 1].first.price
            }

            else -> throw IllegalArgumentException("Invalid order side: $side")
        }
    }

    fun getSize(side: Char, level: Int): Long {
        if (level < 1) throw InvalidLevelException("Invalid level: $level")
        return when (side) {
            'B' -> {
                if (bids.size < level)
                    throw LevelNotExistException("No bids at level: $level")
                bids.toList()[level - 1].second.sumOf { it.size?.toInt() ?: 0 }.toLong()
            }

            'O' -> {
                if (offers.size < level)
                    throw LevelNotExistException("No offers at level: $level")
                offers.toList()[level - 1].second.sumOf { it.size?.toInt() ?: 0 }.toLong()
            }

            else -> throw IllegalArgumentException("Invalid order side: $side")
        }
    }

    fun getOrders(side: Char): List<Order> {
        return when (side) {
            'B' -> bids.toList().flatMap { it.second.sortedBy { order -> order.createdAt } }
            'O' -> offers.toList().flatMap { it.second.sortedBy { order -> order.createdAt } }
            else -> throw IllegalArgumentException("Invalid order side: ${side}")
        }
    }

}