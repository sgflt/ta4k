/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Chande Momentum Oscillator (CMO) indicator.
 *
 * The CMO is a momentum oscillator that measures the momentum of a security by comparing
 * the sum of gains to the sum of losses over a specified period.
 *
 * Formula: CMO = ((Sum of Gains - Sum of Losses) / (Sum of Gains + Sum of Losses)) * 100
 *
 * @param indicator a price indicator
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="http://tradingsim.com/blog/chande-momentum-oscillator-cmo-technical-indicator/">
 *      Chande Momentum Oscillator Technical Indicator</a>
 * @see <a href="http://www.investopedia.com/terms/c/chandemomentumoscillator.asp">
 *      Investopedia - Chande Momentum Oscillator</a>
 */
class CMOIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val gainIndicator = indicator.gain()
    private val lossIndicator = indicator.loss()

    private val gainsWindow = gainIndicator.previous(barCount)
    private val lossesWindow = lossIndicator.previous(barCount)

    private var sumOfGains = numFactory.zero()
    private var sumOfLosses = numFactory.zero()

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        // Remove old values if window is full
        if (gainsWindow.isStable) {
            sumOfGains -= gainsWindow.value
            sumOfLosses -= lossesWindow.value
        }

        sumOfGains += gainIndicator.value
        sumOfLosses += lossIndicator.value

        // Calculate CMO
        val totalSum = sumOfGains + sumOfLosses
        return if (totalSum.isZero) {
            numFactory.zero()
        } else {
            (sumOfGains - sumOfLosses) / totalSum * numFactory.hundred()
        }
    }

    override fun updateState(bar: Bar) {
        gainsWindow.onBar(bar)
        lossesWindow.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = gainsWindow.isStable && lossesWindow.isStable

    override fun toString(): String {
        return "CMO($barCount) => $value"
    }
}
