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
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Positive Volume Index (PVI) indicator.
 *
 * The PVI tracks the cumulative change in price on days when volume increases.
 * It starts at 1000 and is updated only when current volume exceeds previous volume.
 *
 * Formula:
 * - If volume increased: PVI = previous PVI × (1 + price change ratio)
 * - Otherwise: PVI = previous PVI
 *
 * Where price change ratio = (current close - previous close) / previous close
 *
 * @param numFactory the number factory
 *
 * @see <a href="http://www.metastock.com/Customer/Resources/TAAZ/Default.aspx?p=92">
 *      MetaStock - Positive Volume Index</a>
 * @see <a href="http://www.investopedia.com/terms/p/pvi.asp">
 *      Investopedia - Positive Volume Index</a>
 */
class PVIIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    private val closePrice = ClosePriceIndicator(numFactory)
    private val volume = VolumeIndicator(numFactory)
    private val previousClosePrice = closePrice.previous()
    private val previousVolume = volume.previous()

    private var pvi = numFactory.thousand()
    private var isFirstBar = true

    override fun updateState(bar: Bar) {
        previousClosePrice.onBar(bar)
        previousVolume.onBar(bar)

        value = calculate()
    }

    private fun calculate(): Num {
        if (isFirstBar) {
            isFirstBar = false
            return pvi
        }

        if (previousClosePrice.isStable && previousVolume.isStable) {
            val currentVolume = volume.value
            val prevVolume = previousVolume.value

            // Update PVI only when volume increases
            if (currentVolume > prevVolume) {
                val currentPrice = closePrice.value
                val prevPrice = previousClosePrice.value
                val priceChangeRatio = (currentPrice - prevPrice) / prevPrice
                pvi += priceChangeRatio * pvi
            }
            // If volume doesn't increase, PVI remains the same
        }
        return pvi
    }

    override val lag = 1

    override val isStable: Boolean
        get() = !isFirstBar && previousClosePrice.isStable && previousVolume.isStable

    override fun toString(): String {
        return "PVI => $value"
    }
}
