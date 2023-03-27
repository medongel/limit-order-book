package com.mizuho.limitorderbook

import com.mizuho.limitorderbook.exception.LevelNotExistException
import com.mizuho.limitorderbook.exception.OrderAlreadyExistException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

class OrderBookTest {

    @Test
    fun addOrder() {
        val orderBook = OrderBook()

        orderBook.addOrder(Order(1, 'O', 100.0, 100))
        orderBook.addOrder(Order(2, 'O', 101.0, 100))
        orderBook.addOrder(Order(3, 'O', 102.0, 100))
        orderBook.addOrder(Order(4, 'O', 103.0, 100))
        orderBook.addOrder(Order(5, 'O', 50.0, 100))

        assertEquals(5, orderBook.getOrders('O').size)
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given an Order, add it to the OrderBook`(side: Char) {
        val orderBook = OrderBook()

        // Given an Order
        val order = Order(1, side, 50.0, 10)

        // When an Order is added to the OrderBook
        orderBook.addOrder(order)


        // Then the OrderBook contains the Order
        assertEquals(1, orderBook.allOrders.size)
        assertEquals(1, orderBook.allOrders.toList().first().second.id)
        assertEquals(side, orderBook.allOrders.toList().first().second.side)
        assertEquals(10, orderBook.allOrders.toList().first().second.size)
        assertEquals(50.0, orderBook.allOrders.toList().first().second.price)
    }

    @Test
    fun `Given an Order which is already added, add it again to the OrderBook, then throw OrderAlreadyExistException`() {
        val orderBook = OrderBook()

        // Given an already added Order
        val order = Order(1, 'O', 50.0, 10)
        orderBook.addOrder(order)

        // When an Order is added to the OrderBook again, Then the OrderBook throws an exception
        assertThrows(OrderAlreadyExistException::class.java) {
            orderBook.addOrder(order)
        }

    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given an order id, remove an Order from the OrderBook`(side: Char) {
        val orderBook = OrderBook()

        // Given an Order id for an already added order
        val orderId = 1L
        orderBook.addOrder(Order(orderId, side, 50.0, 10))

        // When an Order is removed from the OrderBook
        orderBook.removeOrder(orderId)

        // Then the OrderBook does not contain the Order
        assertEquals(0, orderBook.allOrders.size)
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given multiple orders with same price, remove an Order from the OrderBook`(side: Char) {
        val orderBook = OrderBook()

        // Given an Order id for an already added order
        val order1Id = 1L
        val order2Id = 2L
        orderBook.addOrder(Order(order1Id, side, 50.0, 10))
        orderBook.addOrder(Order(order2Id, side, 50.0, 5))

        // When an Order is removed from the OrderBook
        orderBook.removeOrder(order1Id)

        // Then the OrderBook does not contain the Order
        assertEquals(1, orderBook.allOrders.size)


    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given an order id and a new size, modify an existing order in the book to use the new size without changing time priority`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        // Given an Order id for an already added order and a new size
        val orderId = 1L
        val newSize = 100L
        val order = Order(orderId, side, 50.0, 10)
        orderBook.addOrder(order) // First added order
        orderBook.addOrder(Order(2, side, 50.0, 10))
        orderBook.addOrder(Order(3, side, 50.0, 10))


        // When an Order is modified with the new size
        orderBook.modifyOrder(orderId, newSize)

        // Then the OrderBook contains the Order with the new size
        assertEquals(newSize, orderBook.allOrders.toList().first().second.size)
        if (side == 'O')
            assertEquals(order, orderBook.offers[order]?.get(0)) // check if the order is first priority
        else if (side == 'B')
            assertEquals(order, orderBook.bids[order]?.get(0)) // check if the order is first priority

    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given a side and a level (an integer value gt then 0) return the price for that level (where level 1 represents the best price for a given side)`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        // Given a side and a level
        val randomPrices = List(100) { Random.nextLong(20, 70).toDouble() }
        val randomSizes = List(100) { Random.nextLong(1, 10) }


        val priceLevels =
            if (side == 'B')
                randomPrices.toSortedSet().sortedDescending()
            else if (side == 'O')
                randomPrices.toSortedSet()
            else
                throw IllegalArgumentException("Side must be either 'O' or 'B'")

        (1..100).forEach {
            orderBook.addOrder(Order(it.toLong(), side, randomPrices[it - 1], randomSizes[it - 1]))
        }

        priceLevels.forEachIndexed { index, value ->
            val level = index + 1
            // When a price is requested for a given side and level
            val price = orderBook.getPrice(side, level)
            assertEquals(value, price)
        }
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given a side and a level (an integer value gt then 0) when getPrice called and there is no such order at that level throw LevelNotExistException`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        val levelNotExits = 10

        // Given an Order at level 1
        orderBook.addOrder(Order(1, side, 50.0, 10))

        // When a price is requested for a given side and level which does not exist, then throw LevelNotExistException
        assertThrows(LevelNotExistException::class.java) {
            orderBook.getPrice(side, levelNotExits)
        }
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given a side and a level return the total size available for that level)`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        // Given a side and a level
        val randomPrices = List(100) { Random.nextLong(20, 70).toDouble() }
        val randomSizes = List(100) { Random.nextLong(1, 10) }


        val priceLevels =
            if (side == 'B')
                randomPrices.toSortedSet().sortedDescending()
            else if (side == 'O')
                randomPrices.toSortedSet()
            else
                throw IllegalArgumentException("Side must be either 'O' or 'B'")

        val orderList = (1..100).map { Order(it.toLong(), side, randomPrices[it - 1], randomSizes[it - 1]) }.toList()

        orderList.forEach { orderBook.addOrder(it) }


        val levelSizeMap = orderList.groupBy { it.price }.mapValues { entry ->
            entry.value.sumOf { it.size ?: 0 }
        }.toSortedMap()

        val expectedTotalSizeByLevel = if (side == 'B')
            levelSizeMap.values.reversed().toList()
        else
            levelSizeMap.values.toList()


        // When size is requested for a given side and level
        priceLevels.forEachIndexed { index, value ->
            val level = index + 1
            val totalSize = orderBook.getSize(side, level)
            assertEquals(expectedTotalSizeByLevel[index], totalSize)
        }
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given a side and a level (an integer value gt then 0) when getSize called and there is no such order at that level throw LevelNotExistException`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        val levelNotExits = 10

        // Given an Order at level 1
        orderBook.addOrder(Order(1, side, 50.0, 10))

        // When a price is requested for a given side and level which does not exist, then throw LevelNotExistException
        assertThrows(LevelNotExistException::class.java) {
            orderBook.getSize(side, levelNotExits)
        }
    }

    @ParameterizedTest(name = "This test should use sides {0}")
    @ValueSource(strings = ["O", "B"])
    fun `Given a side return all the orders from that side of the book, in level- and time-order`(
        side: Char,
    ) {
        val orderBook = OrderBook()

        // Given a side and a level
        val randomPrices = List(100) { Random.nextLong(20, 70).toDouble() }
        val randomSizes = List(100) { Random.nextLong(1, 10) }


        val priceLevels =
            if (side == 'B')
                randomPrices.toSortedSet().sortedDescending()
            else if (side == 'O')
                randomPrices.toSortedSet()
            else
                throw IllegalArgumentException("Side must be either 'O' or 'B'")

        val orderList = (1..100).map { Order(it.toLong(), side, randomPrices[it - 1], randomSizes[it - 1]) }.toList()

        orderList.forEach { orderBook.addOrder(it) }


        val expectedOrders = if (side == 'B')
            orderList.sortedWith(compareByDescending<Order> { it.price }
                .thenBy { it.createdAt })
        else
            orderList.sortedWith(compareBy({ it.price }, { it.createdAt }))

        // When size is requested for a given side and level
        val orders = orderBook.getOrders(side)

        priceLevels.forEachIndexed { index, value ->
            assertEquals(expectedOrders[index], orders[index])
        }
    }

}