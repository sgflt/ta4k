/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.CircularNumArray

/**
 * Mass Index indicator.
 *
 * The Mass Index is a technical indicator that uses the high-low range to identify trend reversals
 * based on range expansions. It identifies range bulges that can foreshadow a reversal of the current trend.
 *
 * The calculation involves:
 * 1. Calculate the high-low differential for each period
 * 2. Apply a 9-period EMA to the differential (single EMA)
 * 3. Apply a 9-period EMA to the single EMA (double EMA)
 * 4. Calculate the ratio of single EMA to double EMA for each period
 * 5. Sum these ratios over the specified number of periods (usually 25)
 *
 * @param numFactory the number factory
 * @param emaBarCount the time frame for EMAs (usually 9)
 * @param barCount the time frame for mass index calculation (usually 25)
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:mass_index">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:mass_index</a>
 */
class MassIndexIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val emaBarCount: Int = 9,
    private val barCount: Int = 25,
) : NumericIndicator(numFactory) {

    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()
    private val highLowSpread = highPrice.minus(lowPrice)
    private val singleEma = highLowSpread.ema(emaBarCount)
    private val doubleEma = singleEma.ema(emaBarCount) // EMA of EMA, NOT DoubleEMAIndicator (DEMA)
    // Note: DoubleEMAIndicator uses formula: 2×EMA - EMA(EMA)
    // Mass Index requires simple EMA(EMA), which is different!

    private val emaRatios = CircularNumArray(barCount)
    private var runningSum = numFactory.zero()

    init {
        require(emaBarCount > 0) { "EMA bar count must be positive" }
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        // Calculate the current EMA ratio (single EMA / double EMA)
        val currentRatio = if (doubleEma.value.isZero) {
            numFactory.zero()
        } else {
            singleEma.value / doubleEma.value
        }

        // Update the circular array and running sum using streaming algorithm
        if (emaRatios.isNotFull) {
            // Still filling the window
            emaRatios.addLast(currentRatio)
            runningSum += currentRatio
        } else {
            // Window is full, subtract the oldest value and add the new one
            val oldestRatio = emaRatios.first!!
            runningSum = runningSum - oldestRatio + currentRatio
            emaRatios.addLast(currentRatio)
        }

        return runningSum
    }

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        highLowSpread.onBar(bar)
        singleEma.onBar(bar)
        doubleEma.onBar(bar)

        value = calculate()
    }

    override val lag = emaBarCount * 2 + barCount - 1

    override val isStable: Boolean
        get() = doubleEma.isStable && !emaRatios.isNotFull

    override fun toString(): String {
        return "MassIndex($emaBarCount, $barCount) => $value"
    }
}
