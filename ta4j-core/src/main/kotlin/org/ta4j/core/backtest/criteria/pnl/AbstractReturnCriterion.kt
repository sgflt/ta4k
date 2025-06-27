/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

/**
 * Base class for return based criteria.
 * 
 * Handles calculation of the aggregated return across positions and the
 * optional inclusion of the base percentage.
 */
abstract class AbstractReturnCriterion(
    /**
     * If `true` the base percentage of `1` (equivalent to 100%) is
     * included in the returned value.
     */
    protected val addBase: Boolean = true
) : AnalysisCriterion {

    override fun calculate(position: Position): Num {
        if (position.isClosed) {
            return calculateReturn(position)
        }
        return if (addBase) {
            position.numFactory.one()
        } else {
            position.numFactory.zero()
        }
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val one = tradingRecord.currentPosition.numFactory.one()
        val result = tradingRecord.positions
            .map { calculate(it) }
            .fold(one) { acc, value -> acc * value }
        
        return if (addBase) {
            result
        } else {
            result - one
        }
    }

    /**
     * Calculates the return of the given closed position including the base.
     *
     * @param position the closed position
     * @return the return of the position including the base
     */
    protected abstract fun calculateReturn(position: Position): Num
}