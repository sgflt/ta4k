/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.donchian

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Donchian Channel Lower Indicator.
 *
 * The lower band of the Donchian Channel, which represents the lowest low
 * over a specified period. The Donchian Channel is a technical analysis
 * indicator that identifies potential breakouts by tracking the highest high
 * and lowest low over a given period.
 *
 * @param numFactory the number factory
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="https://www.investopedia.com/terms/d/donchianchannels.asp">
 *      Investopedia - Donchian Channels</a>
 */
class DonchianChannelLowerIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val lowPrice = Indicators.extended(numFactory).lowPrice()
    private val lowestValue = lowPrice.lowest(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = lowestValue.value

    override fun updateState(bar: Bar) {
        lowestValue.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = lowestValue.isStable

    override fun toString(): String {
        return "DCL($barCount) => $value"
    }
}
