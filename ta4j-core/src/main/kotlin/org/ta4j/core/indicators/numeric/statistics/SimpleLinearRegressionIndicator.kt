/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

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
    private val type: SimpleLinearRegressionType = SimpleLinearRegressionType.Y,
) : NumericIndicator(indicator.numFactory) {
    private val window = ArrayDeque<XY>(barCount)
    private var sumX = numFactory.zero()
    private var sumY = numFactory.zero()
    private var sumXY = numFactory.zero()
    private var sumXX = numFactory.zero()
    private var barsPassed = 0

    private fun calculate(): Num {
        calculateRegressionLine()
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
            with(window.removeFirst()) {
                sumX -= x
                sumY -= y
                sumXX -= x * x
                sumXY -= x * y
            }
        }

        val x = numFactory.numOf(barsPassed)
        val y = indicator.value
        window.addLast(XY(x, y))

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
