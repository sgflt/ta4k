/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Number of break even position criterion.
 */
class NumberOfBreakEvenPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (isBreakEvenPosition(position))
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfBreakEvenTrades = tradingRecord.positions
            .stream()
            .filter(Position::isClosed)
            .filter({ position: Position -> this.isBreakEvenPosition(position) })
            .count()
        return defaultNumFactory.numOf(numberOfBreakEvenTrades)
    }


    private fun isBreakEvenPosition(position: Position): Boolean {
        return position.isClosed && position.profit.isZero
    }
}
