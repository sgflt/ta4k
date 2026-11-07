/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Number of closed winning positions criterion.
 */
class NumberOfWinningPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (position.hasProfit())
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfWinningPositions =
            tradingRecord.positions.count { it.hasProfit() }
        return defaultNumFactory.numOf(numberOfWinningPositions)
    }
}
