/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.statistics

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Simple linear regression indicator.
 *
 *
 *
 * A moving (i.e. over the time frame) simple linear regression (least squares).
 *
 * <pre>
 * y = slope * x + intercept
</pre> *
 *
 * see [LinearRegression](https://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html)
 */
class SimpleLinearRegressionIndicator @JvmOverloads constructor(
    private val indicator: NumericIndicator,
    private val barCount: Int,
    private val type: SimpleLinearRegressionType? = SimpleLinearRegressionType.Y,
) : NumericIndicator(indicator.numFactory) {
    private val window = ArrayDeque<XY>(barCount)
    private var sumX = numFactory.zero()
    private var sumY = numFactory.zero()
    private var sumXY = numFactory.zero()
    private var sumXX = numFactory.zero()
    private var barsPassed = 0

    private fun calculate(): Num {
        return when (type) {
            SimpleLinearRegressionType.SLOPE -> slope
            SimpleLinearRegressionType.INTERCEPT -> intercept
            else -> slope * numFactory.numOf(barCount) + intercept
        }
    }

    /**
     * Calculates the regression line.
     */
    private fun calculateRegressionLine() {
        if (window.size == barCount) {
            with(window.remove()) {
                sumX -= x
                sumY -= y
                sumXX -= x * x
                sumXY -= x * y
            }
        }

        val x = numFactory.numOf(barsPassed)
        val y = indicator.value
        window.offer(XY(x, y))

        sumX += x
        sumY += y
        sumXX += x * x
        sumXY += x * y
    }


    private val slope: Num
        get() {
            if (window.size < 2) {
                return numFactory.zero() // Not enough points for regression
            }

            val nNum = numFactory.numOf(window.size)
            val numerator = nNum * sumXY - sumX * sumY
            val denominator = nNum * sumXX - sumX * sumX
            return numerator / denominator
        }


    private val intercept: Num
        get() {
            if (window.size < 2) {
                return numFactory.zero() // Not enough points for regression
            }

            val nNum = numFactory.numOf(window.size)
            val xMean = sumX / nNum
            val yMean = sumY / nNum

            return yMean - (slope * xMean)
        }


    public override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount

    override val isStable
        get() = indicator.isStable


    /**
     * The type for the outcome of the [SimpleLinearRegressionIndicator].
     */
    enum class SimpleLinearRegressionType {
        Y,
        SLOPE,
        INTERCEPT
    }

    @JvmRecord
    private data class XY(val x: Num, val y: Num)
}
