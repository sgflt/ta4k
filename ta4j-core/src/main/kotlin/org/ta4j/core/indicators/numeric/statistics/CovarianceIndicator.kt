/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Covariance indicator.
 */
class CovarianceIndicator(
    private val indicator1: NumericIndicator,
    private val indicator2: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(
    indicator1.numFactory
) {
    private val window: Queue<XY> = LinkedList<XY>()
    private var sumX: Num
    private var sumXY: Num
    private var sumY: Num


    /**
     * Constructor.
     *
     * @param indicator1 the first indicator
     * @param indicator2 the second indicator
     * @param barCount the time frame
     */
    init {
        sumX = numFactory.zero()
        sumXY = numFactory.zero()
        sumY = numFactory.zero()
    }


    protected fun calculate(): Num {
        val x = indicator1.value
        val y = indicator2.value

        val newValue = XY(x, y)
        window.offer(newValue)

        if (window.size > barCount) {
            val polled = window.poll()
            removeOldPoint(polled)
        }

        // Update the mean and covariance
        return updateMeanAndCovariance(newValue)
    }


    private fun removeOldPoint(polled: XY) {
        sumX -= polled.x
        sumY -= polled.y
        sumXY -= polled.x * polled.y
    }

    private fun updateMeanAndCovariance(newValue: XY): Num {
        sumX += newValue.x
        sumY += newValue.y
        sumXY += newValue.x * newValue.y

        val divisor = numFactory.numOf(window.size)
        val meanX = sumX / divisor
        val meanY = sumY / divisor

        return (sumXY / divisor) - (meanX * meanY)
    }


    public override fun updateState(bar: Bar) {
        indicator1.onBar(bar)
        indicator2.onBar(bar)
        value = calculate()
        value = if (window.isEmpty()) numFactory.zero() else value
    }


    override val isStable
        get() = window.size == barCount && indicator1.isStable && indicator2.isStable

    override val lag: Int
        get() = barCount

    override fun toString() = "COV($barCount) => $value"

    @JvmRecord
    private data class XY(val x: Num, val y: Num)
}
