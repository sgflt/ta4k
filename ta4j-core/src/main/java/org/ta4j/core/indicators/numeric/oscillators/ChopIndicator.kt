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
package org.ta4j.core.indicators.numeric.oscillators

import kotlin.math.log10
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The Choppiness Index (CHOP) indicator.
 *
 * The CHOP index is used to indicate sideways markets. It measures the degree of trend
 * or choppiness in the market. Values closer to 100 indicate a choppy, sideways market,
 * while values closer to 0 indicate a trending market.
 *
 * Formula:
 * CHOP = scaleTo * LOG10(SUM(ATR(1), n) / (MaxHi(n) - MinLo(n))) / LOG10(n)
 *
 * Where:
 * - n = period length
 * - ATR(1) = Average True Range with period of 1
 * - SUM(ATR(1), n) = Sum of ATR over past n bars
 * - MaxHi(n) = Highest high over past n bars
 * - MinLo(n) = Lowest low over past n bars
 * - scaleTo = scaling factor (usually 100)
 *
 * @param numFactory the number factory
 * @param timeFrame the period length (commonly 14)
 * @param scaleTo the scaling factor (usually 100)
 *
 * @see <a href="https://www.tradingview.com/wiki/Choppiness_Index_(CHOP)">
 *      TradingView - Choppiness Index</a>
 */
class ChopIndicator(
    numFactory: NumFactory,
    private val timeFrame: Int,
    private val scaleTo: Int = 100,
) : NumericIndicator(numFactory) {

    private val atrIndicator = ATRIndicator(numFactory, barCount = 1)
    private val atrSum = atrIndicator.runningTotal(timeFrame)

    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()
    private val highestHigh = highPrice.highest(timeFrame)
    private val lowestLow = lowPrice.lowest(timeFrame)

    private val log10n = numFactory.numOf(log10(timeFrame.toDouble()))
    private val scaleUpTo = numFactory.numOf(scaleTo)

    init {
        require(timeFrame > 0) { "Time frame must be positive" }
        require(scaleTo > 0) { "Scale factor must be positive" }
    }

    private fun calculate(): Num {
        val sumATR = atrSum.value
        val highLowRange = highestHigh.value - lowestLow.value

        // Avoid division by zero
        if (highLowRange.isZero || sumATR.isZero) {
            return numFactory.zero()
        }

        val ratio = sumATR / highLowRange

        // Calculate log10(ratio) and scale
        val log10Ratio = numFactory.numOf(log10(ratio.doubleValue()))

        return scaleUpTo * log10Ratio / log10n
    }

    override fun updateState(bar: Bar) {
        atrIndicator.onBar(bar)
        atrSum.onBar(bar)
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        highestHigh.onBar(bar)
        lowestLow.onBar(bar)

        value = calculate()
    }

    override val lag = timeFrame

    override val isStable: Boolean
        get() = atrSum.isStable && highestHigh.isStable && lowestLow.isStable

    override fun toString(): String {
        return "CHOP($timeFrame, $scaleTo) => $value"
    }
}
