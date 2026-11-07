/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Net Profit and loss criterion (absolute PnL, includes trading costs).
 */
class ProfitLossCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return position.profit
    }


    override fun calculate(tradingRecord: TradingRecord): Num =
        tradingRecord.positions
            .filter { it.isClosed }
            .map { calculate(it) }
            .fold(defaultNumFactory.zero()) { acc, num -> acc + num }
}
