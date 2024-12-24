package org.ta4j.core.backtest;

import org.ta4j.core.backtest.analysis.cost.CostModel;
import org.ta4j.core.num.NumFactory;

public record BacktestConfiguration(
    NumFactory numFactory,
    CostModel transactionCostModel,
    CostModel holdingCostModel,
    TradeExecutionModel tradeExecutionModel
) {
}
