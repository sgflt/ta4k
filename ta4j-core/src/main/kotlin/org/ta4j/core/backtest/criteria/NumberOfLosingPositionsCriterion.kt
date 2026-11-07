/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Number of closed losing positions criterion.
 */
class NumberOfLosingPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (position.hasLoss())
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfLosingPositions =
            tradingRecord.positions.stream().filter({ obj: Position? -> obj!!.hasLoss() }).count()
        return defaultNumFactory.numOf(numberOfLosingPositions)
    }
}
