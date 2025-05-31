/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
