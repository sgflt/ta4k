/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Number of break even position criterion.
 */
class NumberOfBreakEvenPositionsCriterion : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (isBreakEvenPosition(position))
            position.numFactory.one()
        else
            position.numFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfBreakEvenTrades = tradingRecord.positions.count(::isBreakEvenPosition)
        return tradingRecord.numFactory.numOf(numberOfBreakEvenTrades)
    }


    private fun isBreakEvenPosition(position: Position): Boolean {
        return position.isClosed && position.profit.isZero
    }
}
