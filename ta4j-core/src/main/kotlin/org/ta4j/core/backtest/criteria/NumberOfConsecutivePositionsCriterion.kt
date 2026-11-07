/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Number of maximum consecutive winning or losing positions criterion.
 */
class NumberOfConsecutivePositionsCriterion
/**
 * Constructor.
 *
 * @param positionFilter consider either the winning or losing positions
 */(private val positionFilter: PositionFilter?) : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        return if (isConsecutive(position))
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        var maxConsecutive = 0
        var consecutives = 0
        for (position in tradingRecord.positions) {
            if (isConsecutive(position)) {
                ++consecutives
            } else {
                if (maxConsecutive < consecutives) {
                    maxConsecutive = consecutives
                }
                consecutives = 0 // reset
            }
        }

        // in case all positions are consecutive positions
        if (maxConsecutive < consecutives) {
            maxConsecutive = consecutives
        }

        return defaultNumFactory.numOf(maxConsecutive)
    }

    private fun isConsecutive(position: Position): Boolean {
        if (position.isClosed) {
            val pnl = position.profit
            return if (this.positionFilter == PositionFilter.PROFIT) pnl.isPositive else pnl.isNegative
        }
        return false
    }
}
