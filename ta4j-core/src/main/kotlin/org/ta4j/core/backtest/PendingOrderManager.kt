/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Manages the execution of trade orders based on the configured execution mode.
 *
 * Handles both immediate execution (CURRENT_CLOSE) and delayed execution (NEXT_OPEN)
 * through a pending order system.
 */
class PendingOrderManager(
    private val tradingRecord: TradingRecord,
    private val executionMode: ExecutionMode,
    private val numFactory: NumFactory,
) {
    private val pendingOrders = mutableListOf<PendingOrder>()


    /**
     * Creates and queues a trade order based on the execution mode.
     * All orders go through the unified queuing system.
     *
     * @param operation the type of operation (ENTER or EXIT)
     * @param amount the trade amount
     */
    fun createOrder(operation: OperationType, event: MarketEvent, amount: Num) {

        val trigger = when (executionMode) {
            ExecutionMode.CURRENT_CLOSE -> CurrentBarTrigger()
            ExecutionMode.NEXT_OPEN -> NextBarOpenTrigger(event.beginTime)
        }

        pendingOrders.add(
            PendingOrder(
                type = operation,
                amount = amount,
                trigger = trigger,
                createdAt = Instant.now()
            )
        )
    }

    /**
     * Processes all pending orders against the given market event.
     * Orders that should be executed are executed and removed from the pending list.
     *
     * @param event the market event to check against pending orders
     */
    fun processPendingOrders(event: MarketEvent) {
        val iterator = pendingOrders.iterator()
        while (iterator.hasNext()) {
            val order = iterator.next()
            if (order.trigger.shouldExecute(event)) {
                val executionPrice = order.trigger.getExecutionPrice(event, numFactory)
                if (executionPrice != null) {
                    executePendingOrder(order, event, executionPrice)
                    iterator.remove()
                }
            }
        }
    }

    /**
     * Gets the current number of pending orders.
     *
     * @return the number of orders waiting for execution
     */
    fun getPendingOrderCount(): Int = pendingOrders.size

    private fun executePendingOrder(order: PendingOrder, event: MarketEvent, executionPrice: Num) {
        when (order.type) {
            OperationType.ENTER -> tradingRecord.enter(
                event.beginTime,
                executionPrice,
                order.amount
            )

            OperationType.EXIT -> tradingRecord.exit(
                event.beginTime,
                executionPrice,
                order.amount
            )

            OperationType.NOOP -> {}
        }
    }
}
