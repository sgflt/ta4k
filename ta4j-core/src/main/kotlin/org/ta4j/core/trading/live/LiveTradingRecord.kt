/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.live

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory

/**
 * Trading record for live trading. Fills reported through [LiveTrading.enter] / [LiveTrading.exit]
 * land here, and it publishes the same runtime values as a backtest (current position, closed
 * positions, current price and time), so position-aware rules such as stops work live too.
 */
class LiveTradingRecord(
    startingType: TradeType = TradeType.BUY,
    numFactory: NumFactory,
    transactionCostModel: CostModel = ZeroCostModel,
    holdingCostModel: CostModel = ZeroCostModel,
    name: String = "live",
    /** Off by default: a live session would grow the curve without bound. */
    recordEquityCurve: Boolean = false,
) : BackTestTradingRecord(
    startingType = startingType,
    name = name,
    transactionCostModel = transactionCostModel,
    holdingCostModel = holdingCostModel,
    numFactory = numFactory,
    recordEquityCurve = recordEquityCurve,
)
