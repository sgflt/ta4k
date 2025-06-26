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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.operation.BinaryOperation
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Volume Weighted Moving Average (VWMA) indicator.
 *
 * Volume Weighted Moving Average (VWMA) is a type of moving average where
 * each data point's weight is dynamically adjusted based on market conditions
 * or other factors such as price, volume, or volatility. This makes the VWMA a
 * powerful tool for analyzing trends in varying market conditions, offering
 * both responsiveness and stability.
 *
 * VWMA_t = (Sum(Weight_i * Price_i)) / (Sum(Weight_i))
 *
 * @property priceIndicator price based indicator
 * @property barCount the time frame
 */
class VWMAIndicator(
    numFactory: NumFactory,
    private val priceIndicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val volumeIndicator = Indicators.volume()
    private val weighteedPriceSum = BinaryOperation.product(priceIndicator, volumeIndicator)
    private val volumeWeightedIndicator = BinaryOperation.quotient(
        weighteedPriceSum.sma(barCount),
        volumeIndicator.sma(barCount)
    );

    private fun calculate(): Num = volumeWeightedIndicator.value

    override fun updateState(bar: Bar) {
        volumeWeightedIndicator.onBar(bar)
        value = calculate()
    }


    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = volumeWeightedIndicator.isStable

    override fun toString(): String {
        return "VWMA($barCount) => $value"
    }
}
