/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Least Squares Moving Average (LSMA) Indicator.
 *
 * Least Squares Moving Average (LSMA), also known as the Linear Regression Line
 * or End Point Moving Average, is a unique type of moving average that
 * minimizes the sum of squared differences between data points and a regression
 * line. Unlike other moving averages that calculate a simple or weighted
 * average of prices, the LSMA fits a straight line to the price data over a
 * specific period and uses the end point of that line as the average. This
 * makes LSMA effective at forecasting trends while reducing lag, making it
 * popular in technical analysis.
 *
 * slope = (N * ∑(X * Y) - ∑(X) * ∑(Y)) / (N * ∑(X^2) - (∑(X))^2)
 * intercept = (∑(Y) - slope * ∑(X)) / N
 * lsma = slope * x + intercept
 *
 * @property indicator the indicator to calculate the LSMA from
 * @property barCount the moving average time window
 */
class LSMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val values = mutableListOf<Num>()
    private var processedBars = 0

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        processedBars++
        values.addLast(indicator.value)

        if (processedBars < barCount) {
            // Not enough data points
            value = indicator.value
            return
        }

        // Keep only the last barCount values
        if (values.size > barCount) {
            values.removeFirst()
        }

        val zero = numFactory.zero()
        var sumX = zero
        var sumY = zero
        var sumXY = zero
        var sumX2 = zero

        for (i in 0 until barCount) {
            val x = numFactory.numOf(i + 1) // 1-based index for X
            val y = values[i] // Y values are prices
            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
        }

        val numBarCount = numFactory.numOf(barCount)

        // Calculate slope
        val numerator = numBarCount * sumXY - sumX * sumY
        val denominator = numBarCount * sumX2 - sumX * sumX

        if (denominator.isZero) {
            value = zero
            return
        }

        val slope = numerator / denominator
        val intercept = (sumY - slope * sumX) / numBarCount

        value = slope * numBarCount + intercept
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars >= barCount

    override fun toString(): String {
        return "LSMA($barCount) => $value"
    }
}
