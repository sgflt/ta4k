/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

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
            return if (loss.isNegative) loss else position.numFactory.zero()
        }
        return position.numFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        return tradingRecord.positions
            .filter { it.isClosed }
            .fold(tradingRecord.numFactory.zero()) { total, position -> total + calculate(position) }
    }
}
