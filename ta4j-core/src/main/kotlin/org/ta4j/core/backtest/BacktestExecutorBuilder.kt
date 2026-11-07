/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.execution.OrderTriggerFactory
import org.ta4j.core.backtest.execution.PositionSizer
import org.ta4j.core.backtest.execution.SlippageModel
import org.ta4j.core.backtest.execution.StopLimitOrders
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * @author Lukáš Kvídera
 */
class BacktestExecutorBuilder {
    private var numFactory: NumFactory = DoubleNumFactory

    /** The trading cost models  */
    private var transactionCostModel: CostModel = ZeroCostModel
    private var holdingCostModel: CostModel = ZeroCostModel

    /** The execution mode to use  */
    private var executionMode: ExecutionMode = ExecutionMode.CURRENT_CLOSE
    private var orderTriggerFactory: OrderTriggerFactory? = null
    private var slippageModel: SlippageModel = SlippageModel.NONE
    private var positionSizer: PositionSizer? = null
    private var closeOpenPositionAtEnd = false


    fun numFactory(numFactory: NumFactory): BacktestExecutorBuilder = apply {
        this.numFactory = numFactory
        defaultNumFactory = numFactory
    }


    fun transactionCostModel(transactionCostModel: CostModel): BacktestExecutorBuilder = apply {
        this.transactionCostModel = transactionCostModel
    }


    fun holdingCostModel(holdingCostModel: CostModel): BacktestExecutorBuilder = apply {
        this.holdingCostModel = holdingCostModel
    }


    fun executionMode(executionMode: ExecutionMode): BacktestExecutorBuilder = apply {
        this.executionMode = executionMode
    }


    /** Replaces [executionMode] with custom order handling, e.g. [StopLimitOrders]. */
    fun orderTriggerFactory(orderTriggerFactory: OrderTriggerFactory): BacktestExecutorBuilder = apply {
        this.orderTriggerFactory = orderTriggerFactory
    }


    fun stopLimitOrders(stopTriggerRatio: Number, limitOffsetRatio: Number, maxBarsToFill: Int = 1) =
        orderTriggerFactory(StopLimitOrders(stopTriggerRatio, limitOffsetRatio, maxBarsToFill))


    fun slippage(slippageModel: SlippageModel): BacktestExecutorBuilder = apply {
        this.slippageModel = slippageModel
    }


    fun positionSizer(positionSizer: PositionSizer): BacktestExecutorBuilder = apply {
        this.positionSizer = positionSizer
    }


    /** Ends every run flat: a position still open is exited at the last candle's close. */
    fun closeOpenPositionAtEnd(close: Boolean = true): BacktestExecutorBuilder = apply {
        this.closeOpenPositionAtEnd = close
    }


    fun build(): BacktestExecutor = BacktestExecutor(
        BacktestConfiguration(
            numFactory = this.numFactory,
            transactionCostModel = this.transactionCostModel,
            holdingCostModel = this.holdingCostModel,
            executionMode = this.executionMode,
            orderTriggerFactory = this.orderTriggerFactory ?: OrderTriggerFactory.of(this.executionMode),
            slippageModel = this.slippageModel,
            positionSizer = this.positionSizer,
            closeOpenPositionAtEnd = this.closeOpenPositionAtEnd,
        )
    )
}
