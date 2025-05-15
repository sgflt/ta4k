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
package org.ta4j.core.indicators.numeric.oscilators

import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.api.Indicators.highPrice
import org.ta4j.core.api.Indicators.lowPrice
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Stochastic oscillator K.
 */
class StochasticOscillatorKIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    barCount: Int,
    highPriceIndicator: HighPriceIndicator,
    lowPriceIndicator: LowPriceIndicator,
) : NumericIndicator(numFactory) {
    private val highestHigh = highPriceIndicator.highest(barCount)
    private val lowestMin = lowPriceIndicator.lowest(barCount)


    /**
     * Constructor with:
     *
     *
     *  * `indicator` = [ClosePriceIndicator]
     *  * `highPriceIndicator` = [HighPriceIndicator]
     *  * `lowPriceIndicator` = [LowPriceIndicator]
     *
     *
     * @param numFactory the bar series
     * @param barCount the time frame
     */
    constructor(numFactory: NumFactory, barCount: Int) : this(
        numFactory,
        closePrice(),
        barCount,
        highPrice(),
        lowPrice()
    )


    private fun calculate(): Num {
        val highestHighPrice = highestHigh.value
        val lowestLowPrice = lowestMin.value

        val fullCandleHeight = highestHighPrice.minus(lowestLowPrice)
        if (fullCandleHeight.isZero) {
            return numFactory.fifty()
        }

        return ((indicator.value - lowestLowPrice) / fullCandleHeight) * numFactory.hundred()
    }


    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        highestHigh.onBar(bar)
        lowestMin.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = indicator.isStable && highestHigh.isStable && lowestMin.isStable

    override val lag: Int
        get() = highestHigh.lag
}
