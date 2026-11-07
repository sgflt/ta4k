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
 * Base class for criteria that sum the net profit of consecutive closed positions
 * and report the best streak.
 *
 * Subclasses decide which positions extend a streak ([accepts]) and which streak
 * total is preferred ([preferForStreak]). Any other position resets the streak.
 */
abstract class AbstractConsecutivePnlCriterion : AnalysisCriterion {

    /**
     * @return true if a position with net [profit] extends the current streak
     */
    protected abstract fun accepts(profit: Num): Boolean

    /**
     * @return true if streak total [candidate] is preferred over [best]
     */
    protected abstract fun preferForStreak(candidate: Num, best: Num): Boolean

    override fun calculate(position: Position): Num {
        val zero = position.numFactory.zero()
        if (!position.isClosed) {
            return zero
        }
        return position.profit.takeIf { accepts(it) } ?: zero
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val zero = tradingRecord.positions.firstOrNull()?.numFactory?.zero() ?: defaultNumFactory.zero()
        var current = zero
        var best = zero
        for (position in tradingRecord.positions) {
            val contribution = calculate(position)
            if (contribution.isZero) {
                current = zero
                continue
            }
            current += contribution
            if (preferForStreak(current, best)) {
                best = current
            }
        }
        return best
    }
}
