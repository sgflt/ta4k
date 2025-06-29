/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
