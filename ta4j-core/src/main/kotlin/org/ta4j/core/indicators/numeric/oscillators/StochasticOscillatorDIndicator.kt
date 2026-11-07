/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Stochastic oscillator D.
 */
class StochasticOscillatorDIndicator private constructor(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
) : NumericIndicator(numFactory) {
    private var barsPassed = 0

    constructor(numFactory: NumFactory, k: StochasticOscillatorKIndicator) : this(numFactory, k.sma(3))

    private fun calculate(): Num = indicator.value

    override fun toString() = "StochD() => ${if (isStable) value else NaN}"

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        ++barsPassed
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable && barsPassed >= 3

    override val lag = 3
}
