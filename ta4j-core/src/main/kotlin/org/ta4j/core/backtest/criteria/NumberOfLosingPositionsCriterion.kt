/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Number of closed losing positions criterion.
 */
class NumberOfLosingPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (position.hasLoss)
            position.numFactory.one()
        else
            position.numFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfLosingPositions =
            tradingRecord.positions.count { it.hasLoss }
        return tradingRecord.numFactory.numOf(numberOfLosingPositions)
    }
}
