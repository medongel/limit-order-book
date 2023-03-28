# limit-order-book

In order to Run the tests please type: 
./gradlew test

# Future Work

Here are some suggestions for the implementation of the Order and/or OrderBook classes 
to make them more performant to support real-life, latency-sensitive trading operations:

We can use more efficient data structures. Instead of using a simple ArrayList to keep orders at the same level, it would be better to use PriorityQueue with the creation time-based comparator.

We can use multithreading and asynchronous processing of high volume of orders. This would reduce the latency.

In real-life trading applications, it is more potential to receive multiple orders simultaneously. So, We can have a mechanism to handle and process multiple orders at once.

We can implement diversification orders by adding order types to the order entity. It is already done in this project that orders are separated into two types: bids and offers

Separation of concerns can be implemented where adding orders and getting/querying orders parts can be loosely coupled to maximize the throughput and reduce latency for adding orders and querying orders. There can be separate implementations/data structures for handling adding orders and querying orders which is more efficient for the specific purpose (read or write), and of course, there should be a background processing to transform data between these two data structures.
