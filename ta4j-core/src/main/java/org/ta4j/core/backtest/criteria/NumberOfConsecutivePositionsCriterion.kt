/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
