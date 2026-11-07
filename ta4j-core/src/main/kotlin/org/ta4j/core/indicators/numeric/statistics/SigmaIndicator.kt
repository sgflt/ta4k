/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Sigma-Indicator (also called, "z-score" or "standard score").
 */
class SigmaIndicator(private val ref: NumericIndicator, barCount: Int) : NumericIndicator(ref.numFactory) {
    private val mean = ref.sma(barCount)
    private val sd = ref.stddev(barCount)


    private fun calculate(): Num {
        return if (sd.value.isZero) {
            numFactory.one()
        } else {
            (ref.value - mean.value) / sd.value
        }
    }


    public override fun updateState(bar: Bar) {
        ref.onBar(bar)
        mean.onBar(bar)
        sd.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = ref.isStable && mean.isStable && sd.isStable

    override val lag: Int
        get() = ref.lag
}
