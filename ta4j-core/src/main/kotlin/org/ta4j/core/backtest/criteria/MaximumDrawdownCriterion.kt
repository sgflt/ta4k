/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Maximum drawdown criterion, returned in decimal format.
 *
 *
 *
 * The maximum drawdown measures the largest loss. Its value can be within the
 * range of [0,1], e.g. a maximum drawdown of `+1` (= +100%) means a total
 * loss, a maximum drawdown of `0` (= 0%) means no loss at all.
 *
 * @see [
 * https://en.wikipedia.org/wiki/Drawdown_
](http://en.wikipedia.org/wiki/Drawdown_%28economics%29) */
/**
 * Maximum drawdown criterion.
 */
class MaximumDrawdownCriterion(private val numFactory: NumFactory) : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        if (position.entry == null) {
            return numFactory.zero()
        }

        return position.maxDrawdown
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.isEmpty) {
            return numFactory.zero()
        }

        return tradingRecord.maximumDrawdown!!
    }
}
