package org.ta4j.core.backtest

import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.NumFactory

@JvmRecord
data class BacktestConfiguration(
    val numFactory: NumFactory,
    val transactionCostModel: CostModel = ZeroCostModel,
    val holdingCostModel: CostModel = ZeroCostModel,
    val tradeExecutionModel: TradeExecutionModel = TradeOnCurrentCloseModel(),
)
