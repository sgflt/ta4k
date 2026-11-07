/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

/**
 * Base class for criteria reporting the most extreme net profit of a single closed position.
 *
 * Only positions whose profit [accepts] are considered; zero when there is none.
 */
abstract class AbstractPositionExtremePnlCriterion : AnalysisCriterion {

    /**
     * @return true if a position with net [profit] is considered
     */
    protected abstract fun accepts(profit: Num): Boolean

    /**
     * @return true if [candidate] is more extreme than [best]
     */
    protected abstract fun isMoreExtreme(candidate: Num, best: Num): Boolean

    override fun calculate(position: Position): Num =
        if (position.isClosed) position.profit else position.numFactory.zero()

    override fun calculate(tradingRecord: TradingRecord): Num {
        val zero = tradingRecord.numFactory.zero()
        return tradingRecord.positions
            .filter { it.isClosed }
            .map { it.profit }
            .filter { accepts(it) }
            .fold(zero) { best, profit -> if (best.isZero || isMoreExtreme(profit, best)) profit else best }
    }
}
