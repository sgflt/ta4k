/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Intraday Intensity Index (III) indicator.
 *
 * The III measures the flow of volume in relation to price changes within the day.
 * It helps identify whether volume is flowing into or out of a security by comparing
 * the closing price relative to the high-low range, weighted by volume.
 *
 * Formula: III = ((Close - Low) - (High - Close)) / (High - Low) / Volume
 * Which simplifies to: III = (2×Close - High - Low) / ((High - Low) × Volume)
 *
 * The indicator returns 0 for the first bar as there's no previous comparison point.
 *
 * @param numFactory the number factory
 *
 * @see <a href="https://www.investopedia.com/terms/i/intradayintensityindex.asp">
 *      Intraday Intensity Index</a>
 */
class IIIIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    private val closePrice = Indicators.extended(numFactory).closePrice()
    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()
    private val volume = Indicators.extended(numFactory).volume()

    private var isFirstBar = true

    private fun calculate(): Num {
        if (isFirstBar) {
            isFirstBar = false
            return numFactory.zero()
        }

        val close = closePrice.value
        val high = highPrice.value
        val low = lowPrice.value
        val vol = volume.value

        val highMinusLow = high - low

        // Avoid division by zero
        if (highMinusLow.isZero || vol.isZero) {
            return numFactory.zero()
        }

        val doubledClose = numFactory.two() * close
        val highPlusLow = high + low

        return (doubledClose - highPlusLow) / (highMinusLow * vol)
    }

    override fun updateState(bar: Bar) {
        closePrice.onBar(bar)
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        volume.onBar(bar)

        value = calculate()
    }

    override val lag = 0

    override val isStable: Boolean
        get() = !isFirstBar

    override fun toString(): String {
        return "III => $value"
    }
}
