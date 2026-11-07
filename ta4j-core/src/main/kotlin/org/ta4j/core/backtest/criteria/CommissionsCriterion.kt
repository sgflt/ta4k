/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Total transaction costs (commissions) paid for entry and exit trades.
 *
 * Open positions contribute their entry cost. Holding costs are not included.
 */
class CommissionsCriterion : AnalysisCriterion {

    override fun calculate(position: Position): Num {
        val zero = position.numFactory.zero()
        val entryCost = position.entry?.cost ?: zero
        val exitCost = position.exit?.cost ?: zero
        return entryCost + exitCost
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val positions = tradingRecord.positions + listOfNotNull(tradingRecord.currentPosition.takeIf { it.isOpened })
        val zero = tradingRecord.numFactory.zero()
        return positions.fold(zero) { total, position -> total + calculate(position) }
    }
}
