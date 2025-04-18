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
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import java.util.*

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
            val polled = window.poll()
            removeOldValue(polled)
        }


        sx = sx.plus(x)
        sy = sy.plus(y)
        sxy = sxy.plus(x.multipliedBy(y))
        sxx = sxx.plus(x.multipliedBy(x))
        syy = syy.plus(y.multipliedBy(y))

        // (n * Sxx - Sx * Sx) * (n * Syy - Sy * Sy)
        val toSqrt = (n.multipliedBy(sxx).minus(sx.multipliedBy(sx)))
            .multipliedBy(n.multipliedBy(syy).minus(sy.multipliedBy(sy)))

        if (toSqrt.isGreaterThan(numFactory.zero())) {
            // pearson = (n * Sxy - Sx * Sy) / sqrt((n * Sxx - Sx * Sx) * (n * Syy - Sy *
            // Sy))
            return (n.multipliedBy(sxy).minus(sx.multipliedBy(sy))).dividedBy(toSqrt.sqrt())
        }

        return NaN
    }


    private fun removeOldValue(polled: XY) {
        sx = sx.minus(polled.x)
        sy = sy.minus(polled.y)
        sxy = sxy.minus(polled.x.multipliedBy(polled.y))
        sxx = sxx.minus(polled.x.multipliedBy(polled.x))
        syy = syy.minus(polled.y.multipliedBy(polled.y))
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
