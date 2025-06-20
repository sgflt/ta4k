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

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Negative Volume Index (NVI) indicator.
 *
 * The NVI is a cumulative indicator that uses the change in volume to decide when
 * the smart money is active. The NVI assumes that the smart money trades when volume
 * decreases and the uninformed crowd trades when volume increases.
 *
 * The NVI starts with a base value (typically 1000) and:
 * - When volume decreases compared to the previous period, the NVI is updated based on price change
 * - When volume increases or stays the same, the NVI remains unchanged
 *
 * Formula:
 * - If Volume(today) < Volume(yesterday): NVI = Previous NVI × (1 + Price Change %)
 * - If Volume(today) ≥ Volume(yesterday): NVI = Previous NVI
 *
 * Where Price Change % = (Close - Previous Close) / Previous Close
 *
 * @param numFactory the number factory
 * @param startingValue the initial value for the NVI (default: 1000)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:negative_volume_inde">
 *      StockCharts - Negative Volume Index</a>
 * @see <a href="http://www.investopedia.com/terms/n/nvi.asp">
 *      Investopedia - Negative Volume Index</a>
 */
class NVIIndicator(
    numFactory: NumFactory,
    private val startingValue: Num = numFactory.thousand(),
) : NumericIndicator(numFactory) {

    private val closePrice = Indicators.extended(numFactory).closePrice()
    private val volume = Indicators.extended(numFactory).volume()
    private val previousClosePrice = closePrice.previous()
    private val previousVolume = volume.previous()

    private var currentNvi = startingValue
    private var isFirstBar = true

    private fun calculate(): Num {
        if (isFirstBar) {
            isFirstBar = false
            currentNvi = startingValue
            return currentNvi
        }

        val currentClose = closePrice.value
        val currentVolumeValue = volume.value

        // Update NVI only when volume decreases
        if (currentVolumeValue < previousVolume.value) {
            val priceChangeRatio = (currentClose - previousClosePrice.value) / previousClosePrice.value
            currentNvi += priceChangeRatio * currentNvi
        }

        // When volume increases or stays the same, NVI remains unchanged
        return currentNvi
    }

    override fun updateState(bar: Bar) {
        closePrice.onBar(bar)
        volume.onBar(bar)
        previousClosePrice.onBar(bar)
        previousVolume.onBar(bar)
        value = calculate()
    }

    override val lag = 1

    override val isStable: Boolean
        get() = previousClosePrice.isStable && previousVolume.isStable

    override fun toString(): String {
        return "NVI(startingValue=$startingValue) => $value"
    }
}
