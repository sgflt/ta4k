/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.execution.OrderTriggerFactory
import org.ta4j.core.backtest.execution.PositionSizer
import org.ta4j.core.backtest.execution.SlippageModel
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Turns strategy signals into fills.
 *
 * Each signal becomes a pending order whose [ExecutionTrigger] (from [orderTriggerFactory]) decides
 * when and at what price it fills. Fills get [slippageModel] applied; entries are sized by
 * [positionSizer] when set, exits close the whole open amount.
 */
class PendingOrderManager(
    private val tradingRecord: TradingRecord,
    private val numFactory: NumFactory,
    private val orderTriggerFactory: OrderTriggerFactory,
    private val slippageModel: SlippageModel = SlippageModel.NONE,
    private val positionSizer: PositionSizer? = null,
) {
    constructor(tradingRecord: TradingRecord, executionMode: ExecutionMode, numFactory: NumFactory) :
        this(tradingRecord, numFactory, OrderTriggerFactory.of(executionMode))

    private val pendingOrders = mutableListOf<PendingOrder>()


    /**
     * Queues an order for [operation], unless one of the same type is already pending.
     *
     * @param amount amount used when no position sizer is configured
     */
    fun createOrder(operation: OperationType, event: MarketEvent, amount: Num) {
        if (operation == OperationType.NOOP || pendingOrders.any { it.type == operation }) {
            return
        }
        pendingOrders.add(
            PendingOrder(
                type = operation,
                amount = amount,
                trigger = orderTriggerFactory.create(sideOf(operation), event, numFactory),
                createdAt = Instant.now()
            )
        )
    }

    /**
     * Fills pending orders whose trigger fires on [event] and drops expired ones.
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
                    continue
                }
            }
            if (order.trigger.isExpired(event)) {
                iterator.remove()
            }
        }
    }

    /** Number of orders waiting for execution. */
    val pendingOrderCount: Int
        get() = pendingOrders.size

    private fun sideOf(operation: OperationType): TradeType =
        if (operation == OperationType.ENTER) tradingRecord.startingType else tradingRecord.startingType.complementType()

    private fun executePendingOrder(order: PendingOrder, event: MarketEvent, executionPrice: Num) {
        val fillPrice = slippageModel.apply(sideOf(order.type), executionPrice)
        when (order.type) {
            OperationType.ENTER -> tradingRecord.enter(
                event.beginTime,
                fillPrice,
                positionSizer?.size(fillPrice, tradingRecord) ?: order.amount
            )

            OperationType.EXIT -> tradingRecord.exit(
                event.beginTime,
                fillPrice,
                tradingRecord.currentPosition.entry?.amount ?: order.amount
            )

            OperationType.NOOP -> {}
        }
    }
}
