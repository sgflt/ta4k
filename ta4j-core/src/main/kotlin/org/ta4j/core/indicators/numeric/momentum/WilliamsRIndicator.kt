/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * William's R indicator.
 *
 * William's %R is a momentum indicator that measures overbought and oversold levels.
 * It compares the closing price of a stock to the high-low range over a certain period of time.
 *
 * Formula: %R = (Highest High - Close) / (Highest High - Lowest Low) * -100
 *
 * @param numFactory the number factory
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="https://www.investopedia.com/terms/w/williamsr.asp">
 *      Williams %R</a>
 */
class WilliamsRIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val barCount: Int,
    private val closePriceIndicator: NumericIndicator = Indicators.extended(numFactory).closePrice(),
    private val highPriceIndicator: NumericIndicator = Indicators.extended(numFactory).highPrice(),
    private val lowPriceIndicator: NumericIndicator = Indicators.extended(numFactory).lowPrice(),
) : NumericIndicator(numFactory) {

    private val highestHigh = highPriceIndicator.highest(barCount)
    private val lowestLow = lowPriceIndicator.lowest(barCount)
    private val multiplier = numFactory.numOf(-100)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = when {
        !highestHigh.isStable || !lowestLow.isStable -> numFactory.zero()
        else -> {
            val range = highestHigh.value - lowestLow.value
            if (range.isZero) {
                numFactory.zero()
            } else {
                (highestHigh.value - closePriceIndicator.value) / range * multiplier
            }
        }
    }

    override fun updateState(bar: Bar) {
        closePriceIndicator.onBar(bar)
        highPriceIndicator.onBar(bar)
        lowPriceIndicator.onBar(bar)

        highestHigh.onBar(bar)
        lowestLow.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = highestHigh.isStable
                && lowestLow.isStable
                && closePriceIndicator.isStable
                && highPriceIndicator.isStable
                && lowPriceIndicator.isStable

    override fun toString(): String {
        return "WilliamsR($barCount) => $value"
    }
}
