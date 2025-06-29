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
package org.ta4j.core.backtest.criteria

import lombok.extern.slf4j.Slf4j
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
@Slf4j
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
