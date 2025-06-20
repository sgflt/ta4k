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

import kotlin.math.max
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.Returns
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Value at Risk criterion, returned in decimal format.
 *
 * @see [Value at Risk](https://en.wikipedia.org/wiki/Value_at_risk)
 */
class ValueAtRiskCriterion(
    private val numFactory: NumFactory,
    /** Confidence level as absolute value (e.g. 0.95).  */
    private val confidence: Double,
) : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        if (!position.isClosed) {
            return position.entry!!.netPrice.numFactory.zero()
        }

        val returns = Returns(this.numFactory, position, Returns.ReturnType.ARITHMETIC)
        return calculateVaR(returns)
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val returns = Returns(this.numFactory, tradingRecord, Returns.ReturnType.ARITHMETIC)
        return calculateVaR(returns)
    }


    /**
     * Calculates the VaR on the return series.
     *
     * @param returns the corresponding returns
     *
     * @return the relative Value at Risk
     */
    private fun calculateVaR(returns: Returns): Num {
        val zero = numFactory.zero()
        val returnRates = returns.values.toMutableList()

        if (returnRates.isEmpty()) {
            return zero
        }

        val nInTail = returns.size - (returns.size * confidence).toInt()
        returnRates.sort()
        var valueAtRisk = returnRates[max(0, nInTail - 1)]

        if (valueAtRisk > zero) {
            valueAtRisk = zero
        }

        return valueAtRisk
    }
}
