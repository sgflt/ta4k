/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Number of closed winning positions criterion.
 */
class NumberOfWinningPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (position.hasProfit)
            position.numFactory.one()
        else
            position.numFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfWinningPositions =
            tradingRecord.positions.count { it.hasProfit }
        return tradingRecord.numFactory.numOf(numberOfWinningPositions)
    }
}
