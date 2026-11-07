/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Rate of change (ROCIndicator) indicator (also called "Momentum").
 *
 * The ROCIndicator calculation compares the current value with the value "n" periods ago.
 *
 * @see <a href="https://www.investopedia.com/terms/p/pricerateofchange.asp">ROC</a>
 */
class ROCIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {
    private val nPeriodsAgo = indicator.previous(barCount)
    private var processedBars = 0

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars > barCount

    private fun calculate(): Num {
        return (indicator.value - nPeriodsAgo.value) / nPeriodsAgo.value * numFactory.hundred()
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        nPeriodsAgo.onBar(bar)
        value = calculate()
        processedBars++
    }
}
