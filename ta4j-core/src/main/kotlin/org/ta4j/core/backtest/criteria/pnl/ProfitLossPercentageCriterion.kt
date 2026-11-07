/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

/**
 * Net profit and loss in percentage criterion (relative PnL, excludes trading
 * costs), returned in percentage format (e.g. 1 = 1%).
 *
 * For individual positions: Calculated as (profit / entry_value) * 100
 * For trading records: Calculated as (total_profit / total_invested) * 100
 *
 * @see <a href="https://www.investopedia.com/ask/answers/how-do-you-calculate-percentage-gain-or-loss-investment/">
 *      How to Calculate Percentage Gain or Loss on Investment</a>
 */
class ProfitLossPercentageCriterion : AnalysisCriterion {

    override fun calculate(position: Position): Num {
        if (!position.isClosed) {
            return position.numFactory.zero()
        }

        val entry = position.entry ?: return position.numFactory.zero()
        val entryValue = entry.value * entry.amount

        val profit = position.profit

        return profit / entryValue * position.numFactory.hundred()
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val closedPositions = tradingRecord.positions.filter { it.isClosed }

        if (closedPositions.isEmpty()) {
            return tradingRecord.numFactory.zero()
        }

        // Calculate total invested capital and total profit
        var totalInvested = tradingRecord.numFactory.zero()
        var totalProfit = tradingRecord.numFactory.zero()

        for (position in closedPositions) {
            val entry = position.entry!!
            val entryValue = entry.value * entry.amount

            totalInvested += entryValue
            totalProfit += position.profit
        }

        // Avoid division by zero
        if (totalInvested.isZero) {
            return tradingRecord.numFactory.zero()
        }

        // Calculate overall percentage: (total_profit / total_invested) * 100
        return totalProfit / totalInvested * tradingRecord.numFactory.hundred()
    }
}
