/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Indicator-Pearson-Correlation
 *
 * @see [
 * http://www.statisticshowto.com/probability-and-statistics/correlation-coefficient-formula/](http://www.statisticshowto.com/probability-and-statistics/correlation-coefficient-formula/)
 */
class PearsonCorrelationIndicator(
    private val indicator1: NumericIndicator,
    private val indicator2: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator1.numFactory) {
    private val window = ArrayDeque<XY>(barCount)
    private var sx = numFactory.zero()
    private var sy = numFactory.zero()
    private var sxx = numFactory.zero()
    private var syy = numFactory.zero()
    private var sxy = numFactory.zero()
    private val n = numFactory.numOf(barCount)

    private fun calculate(): Num {
        val x = indicator1.value
        val y = indicator2.value
        window.offer(XY(x, y))

        if (window.size > barCount) {
            removeOldValue(window.poll())
        }

        sx += x
        sy += y
        sxy += x * y
        sxx += x * x
        syy += y * y

        val toSqrt = (n * sxx - sx * sx) * (n * syy - sy * sy)

        return if (toSqrt > numFactory.zero()) {
            (n * sxy - sx * sy) / toSqrt.sqrt()
        } else {
            NaN
        }
    }

    private fun removeOldValue(polled: XY) = with(polled) {
        sx -= x
        sy -= y
        sxy -= x * y
        sxx -= x * x
        syy -= y * y
    }


    public override fun updateState(bar: Bar) {
        indicator1.onBar(bar)
        indicator2.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = indicator1.isStable && indicator2.isStable

    override val lag: Int
        get() = barCount

    @JvmRecord
    private data class XY(val x: Num, val y: Num)
}
