/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Acceleration-deceleration indicator.
 */
class AccelerationDecelerationIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    shortBarCount: Int = 5,
    longBarCount: Int = 34,
) : NumericIndicator(numFactory) {
    private val awesome = Indicators.extended(numFactory).awesomeOscillator(shortBarCount, longBarCount)
    private val sma = awesome.sma(shortBarCount)


    private fun calculate(): Num = awesome.value.minus(sma.value)


    public override fun updateState(bar: Bar) {
        awesome.onBar(bar)
        sma.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = awesome.isStable && sma.isStable

    override val lag: Int
        get() = awesome.lag
}
