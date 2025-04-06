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

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import java.util.*

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
        calculateRegressionLine()

        if (type == SimpleLinearRegressionType.SLOPE) {
            return slope
        }

        if (type == SimpleLinearRegressionType.INTERCEPT) {
            return intercept
        }

        return slope.multipliedBy(numFactory.numOf(barCount)).plus(intercept)
    }


    /**
     * Calculates the regression line.
     */
    private fun calculateRegressionLine() {
        if (window.size == barCount) {
            val old = window.remove()
            sumX = sumX.minus(old.x)
            sumY = sumY.minus(old.y)
            sumXX = sumXX.minus(old.x.multipliedBy(old.x))
            sumXY = sumXY.minus(old.x.multipliedBy(old.y))
        }

        val x = numFactory.numOf(barsPassed)
        val y = indicator.value
        window.offer(XY(x, y))

        sumX = sumX.plus(x)
        sumY = sumY.plus(y)
        sumXX = sumXX.plus(x.multipliedBy(x))
        sumXY = sumXY.plus(x.multipliedBy(y))
    }


    private val slope: Num
        get() {
            val n = window.size
            if (n < 2) {
                return numFactory.zero() // Not enough points for regression
            }

            val nNum = numFactory.numOf(n)
            val numerator = nNum.multipliedBy(sumXY).minus(sumX.multipliedBy(sumY))
            val denominator = nNum.multipliedBy(sumXX).minus(sumX.multipliedBy(sumX))
            return numerator.dividedBy(denominator)
        }


    private val intercept: Num
        get() {
            val n = window.size
            if (n < 2) {
                return numFactory.zero() // Not enough points for regression
            }

            val nNum = numFactory.numOf(n)
            val xMean = sumX.dividedBy(nNum)
            val yMean = sumY.dividedBy(nNum)

            return yMean.minus(slope.multipliedBy(xMean))
        }


    public override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        value = calculate()
    }


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
