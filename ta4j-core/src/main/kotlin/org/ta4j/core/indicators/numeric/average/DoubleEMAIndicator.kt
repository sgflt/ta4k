/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Double exponential moving average indicator.
 *
 * @see [
 * https://en.wikipedia.org/wiki/Double_exponential_moving_average](https://en.wikipedia.org/wiki/Double_exponential_moving_average)
 */
class DoubleEMAIndicator(indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val ema = indicator.ema(barCount)
    private val emaEma = ema.ema(barCount)


    public override fun updateState(bar: Bar) {
        ema.onBar(bar)
        emaEma.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable
        get() = ema.isStable && emaEma.isStable

    private fun calculate() = ema.value * numFactory.two() - emaEma.value

    override fun toString() = "DoEMA(${barCount}) => $value"
}
