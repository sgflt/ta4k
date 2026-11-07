/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.num.Num

/**
 * Relative strength index indicator.
 *
 *
 *
 * Computed using original Welles Wilder formula.
 */
class RSIIndicator(indicator: NumericIndicator, barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val averageGainIndicator: MMAIndicator = indicator.gain().mma(barCount)
    private val averageLossIndicator: MMAIndicator = indicator.loss().mma(barCount)

    init {
        value = numFactory.zero()
    }

    private fun calculate(): Num {
        // compute relative strength
        val averageGain = averageGainIndicator.value
        val averageLoss = averageLossIndicator.value
        if (averageLoss.isZero) {
            return if (averageGain.isZero) numFactory.fifty() else numFactory.hundred()
        }

        val relativeStrength = averageGain / averageLoss
        // compute relative strength index
        return numFactory.hundred() - numFactory.hundred() / (numFactory.one() + relativeStrength)
    }


    public override fun updateState(bar: Bar) {
        averageGainIndicator.onBar(bar)
        averageLossIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = averageGainIndicator.isStable && averageLossIndicator.isStable

    override val lag: Int
        get() = averageGainIndicator.lag
}
