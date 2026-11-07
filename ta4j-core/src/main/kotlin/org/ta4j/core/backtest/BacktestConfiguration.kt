/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.execution.OrderTriggerFactory
import org.ta4j.core.backtest.execution.PositionSizer
import org.ta4j.core.backtest.execution.SlippageModel
import org.ta4j.core.num.NumFactory

@JvmRecord
data class BacktestConfiguration(
    val numFactory: NumFactory,
    val transactionCostModel: CostModel = ZeroCostModel,
    val holdingCostModel: CostModel = ZeroCostModel,
    val executionMode: ExecutionMode = ExecutionMode.CURRENT_CLOSE,
    /** When and at what price signals fill; defaults to [executionMode]. */
    val orderTriggerFactory: OrderTriggerFactory = OrderTriggerFactory.of(executionMode),
    val slippageModel: SlippageModel = SlippageModel.NONE,
    /** Sizes entries; null uses the amount passed to [BacktestExecutor.execute]. */
    val positionSizer: PositionSizer? = null,
    /** Closes a position still open after the last event at the last candle's close. */
    val closeOpenPositionAtEnd: Boolean = false,
)
