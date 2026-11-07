/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.donchian

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Donchian Channel Middle indicator.
 *
 * The middle line is calculated as the average of the upper and lower Donchian Channel lines.
 * The upper line represents the highest high over the specified period, and the lower line
 * represents the lowest low over the same period.
 *
 * Formula: Middle = (Highest High + Lowest Low) / 2
 *
 * @param numFactory the number factory
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="https://www.investopedia.com/terms/d/donchianchannels.asp">
 *      Investopedia - Donchian Channels</a>
 */
class DonchianChannelMiddleIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val highestHigh = Indicators.extended(numFactory).highPrice().highest(barCount)
    private val lowestLow = Indicators.extended(numFactory).lowPrice().lowest(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = (highestHigh.value + lowestLow.value) / numFactory.two()

    override fun updateState(bar: Bar) {
        highestHigh.onBar(bar)
        lowestLow.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = highestHigh.isStable && lowestLow.isStable

    override fun toString(): String {
        return "DCM($barCount) => $value"
    }
}
