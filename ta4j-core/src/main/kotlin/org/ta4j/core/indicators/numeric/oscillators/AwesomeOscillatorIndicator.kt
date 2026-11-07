/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.Indicators.medianPrice
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Awesome oscillator (AO) indicator.
 *
 * @see [](https://www.tradingview.com/wiki/Awesome_Oscillator_
) */
class AwesomeOscillatorIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    indicator: NumericIndicator = medianPrice(),
    shortBarCount: Int = 5,
    longBarCount: Int = 34,
) : NumericIndicator(numFactory) {
    private val shortSma = indicator.sma(shortBarCount)
    private val longSma = indicator.sma(longBarCount)


    private fun calculate() = shortSma.value.minus(longSma.value)


    public override fun updateState(bar: Bar) {
        shortSma.onBar(bar)
        longSma.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = shortSma.isStable && longSma.isStable

    override val lag: Int
        get() = shortSma.lag
}
