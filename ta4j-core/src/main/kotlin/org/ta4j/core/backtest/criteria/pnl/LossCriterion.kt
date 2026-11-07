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
 * Loss criterion with trading costs (= Gross loss) or without ( = Net loss).
 *
 *
 *
 * The loss of the provided [position(s)][Position]
 */
class LossCriterion
/**
 * Constructor for GrossLoss (includes trading costs).
 */ @JvmOverloads constructor(private val excludeCosts: Boolean = false) : AnalysisCriterion {
    /**
     * Constructor.
     *
     * @param excludeCosts set to true to exclude trading costs
     */


    override fun calculate(position: Position): Num {
        if (position.isClosed) {
            val loss = if (this.excludeCosts) position.grossProfit else position.profit
            return if (loss.isNegative) loss else defaultNumFactory.zero()
        }
        return defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        return tradingRecord.positions
            .stream()
            .filter(Position::isClosed)
            .map(this::calculate)
            .reduce(defaultNumFactory.zero(), { obj: Num, augend: Num -> obj.plus(augend) })
    }
}
