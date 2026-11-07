/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Simple moving average (SMA) indicator.
 *
 * @see [https://www.investopedia.com/terms/s/sma.asp](https://www.investopedia.com/terms/s/sma.asp)
 */
class SMAIndicator(indicator: NumericIndicator, private val barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val sum = indicator.runningTotal(barCount)
    private var processedBars = 0
    private val divisor = numFactory.numOf(barCount)

    private fun calculate(): Num {
        val sum = partialSum()
        return sum / divisor
    }

    private fun partialSum() = sum.value

    override val lag: Int
        get() = barCount

    override val isStable
        get() = processedBars >= barCount && sum.isStable

    public override fun updateState(bar: Bar) {
        ++processedBars
        sum.onBar(bar)
        value = calculate()
    }

    override fun toString() = "SMA($barCount) => $value"
}
