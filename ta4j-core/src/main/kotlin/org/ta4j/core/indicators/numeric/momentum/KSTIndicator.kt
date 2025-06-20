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

package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Know Sure Thing (KST) indicator.
 *
 * The KST indicator is a momentum oscillator that uses four different
 * rate-of-change periods and their respective moving averages.
 *
 * <pre>
 * RCMA1 = X1-Period SMA of Y1-Period Rate-of-Change
 * RCMA2 = X2-Period SMA of Y2-Period Rate-of-Change
 * RCMA3 = X3-Period SMA of Y3-Period Rate-of-Change
 * RCMA4 = X4-Period SMA of Y4-Period Rate-of-Change
 *
 * KST = (RCMA1 x 1) + (RCMA2 x 2) + (RCMA3 x 3) + (RCMA4 x 4)
 * </pre>
 *
 * @param numFactory the number factory
 * @param indicator the source indicator (typically close price)
 * @param rcma1SMABarCount RCMA1 SMA period (default: 10)
 * @param rcma1ROCBarCount RCMA1 ROC period (default: 10)
 * @param rcma2SMABarCount RCMA2 SMA period (default: 10)
 * @param rcma2ROCBarCount RCMA2 ROC period (default: 15)
 * @param rcma3SMABarCount RCMA3 SMA period (default: 10)
 * @param rcma3ROCBarCount RCMA3 ROC period (default: 20)
 * @param rcma4SMABarCount RCMA4 SMA period (default: 15)
 * @param rcma4ROCBarCount RCMA4 ROC period (default: 30)
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:know_sure_thing_kst">
 *      Know Sure Thing (KST) - StockCharts</a>
 */
class KSTIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    rcma1SMABarCount: Int = 10,
    rcma1ROCBarCount: Int = 10,
    rcma2SMABarCount: Int = 10,
    rcma2ROCBarCount: Int = 15,
    rcma3SMABarCount: Int = 10,
    rcma3ROCBarCount: Int = 20,
    rcma4SMABarCount: Int = 15,
    rcma4ROCBarCount: Int = 30,
) : NumericIndicator(numFactory) {

    private val rcma1 = indicator.roc(rcma1ROCBarCount).sma(rcma1SMABarCount)
    private val rcma2 = indicator.roc(rcma2ROCBarCount).sma(rcma2SMABarCount)
    private val rcma3 = indicator.roc(rcma3ROCBarCount).sma(rcma3SMABarCount)
    private val rcma4 = indicator.roc(rcma4ROCBarCount).sma(rcma4SMABarCount)

    private val multiplier1 = numFactory.one()
    private val multiplier2 = numFactory.two()
    private val multiplier3 = numFactory.three()
    private val multiplier4 = numFactory.numOf(4)

    init {
        require(rcma1SMABarCount > 0) { "RCMA1 SMA bar count must be positive" }
        require(rcma1ROCBarCount > 0) { "RCMA1 ROC bar count must be positive" }
        require(rcma2SMABarCount > 0) { "RCMA2 SMA bar count must be positive" }
        require(rcma2ROCBarCount > 0) { "RCMA2 ROC bar count must be positive" }
        require(rcma3SMABarCount > 0) { "RCMA3 SMA bar count must be positive" }
        require(rcma3ROCBarCount > 0) { "RCMA3 ROC bar count must be positive" }
        require(rcma4SMABarCount > 0) { "RCMA4 SMA bar count must be positive" }
        require(rcma4ROCBarCount > 0) { "RCMA4 ROC bar count must be positive" }
    }

    private fun calculate() =
        (rcma1.value * multiplier1) +
                (rcma2.value * multiplier2) +
                (rcma3.value * multiplier3) +
                (rcma4.value * multiplier4)

    override fun updateState(bar: Bar) {
        rcma1.onBar(bar)
        rcma2.onBar(bar)
        rcma3.onBar(bar)
        rcma4.onBar(bar)
        value = calculate()
    }

    override val lag = maxOf(
        rcma1.lag,
        rcma2.lag,
        rcma3.lag,
        rcma4.lag
    )

    override val isStable: Boolean
        get() = rcma1.isStable && rcma2.isStable && rcma3.isStable && rcma4.isStable

    override fun toString(): String {
        return "KST => $value"
    }
}
