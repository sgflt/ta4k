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
package org.ta4j.core.indicators.numeric.pivotpoints

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Standard Pivot Reversal Indicator.
 *
 * Calculates standard pivot reversal levels:
 * - R3 = High + 2 × (Pivot Point - Low)
 * - R2 = Pivot Point + (High - Low)
 * - R1 = 2 × Pivot Point - Low
 * - S1 = 2 × Pivot Point - High
 * - S2 = Pivot Point - (High - Low)
 * - S3 = Low - 2 × (High - Pivot Point)
 *
 * @param pivotPointIndicator the pivot point indicator for this reversal
 * @param level the pivot level for this reversal
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
 *      StockCharts - Pivot Points</a>
 */
class StandardReversalIndicator(
    private val pivotPointIndicator: PivotPointIndicator,
    private val level: PivotLevel,
) : NumericIndicator(pivotPointIndicator.numFactory) {

    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()
    private val two = numFactory.two()

    // Store previous period's high/low for calculation
    private var previousHigh: Num = NaN
    private var previousLow: Num = NaN

    override fun updateState(bar: Bar) {
        // Update price indicators
        highPrice.onBar(bar)
        lowPrice.onBar(bar)

        // Update pivot point
        pivotPointIndicator.onBar(bar)

        // Calculate reversal level
        value = calculate()

        // Store current bar's high/low for next calculation
        previousHigh = highPrice.value
        previousLow = lowPrice.value
    }

    private fun calculate(): Num {
        if (pivotPointIndicator.value.isNaN || previousHigh.isNaN || previousLow.isNaN) {
            return NaN
        }

        val pivotPoint = pivotPointIndicator.value
        val high = previousHigh
        val low = previousLow

        return when (level) {
            PivotLevel.RESISTANCE_3 -> high + two * (pivotPoint - low)
            PivotLevel.RESISTANCE_2 -> pivotPoint + (high - low)
            PivotLevel.RESISTANCE_1 -> two * pivotPoint - low
            PivotLevel.SUPPORT_1 -> two * pivotPoint - high
            PivotLevel.SUPPORT_2 -> pivotPoint - (high - low)
            PivotLevel.SUPPORT_3 -> low - two * (high - pivotPoint)
        }
    }

    override val lag = pivotPointIndicator.lag + 1

    override val isStable: Boolean
        get() = pivotPointIndicator.isStable && !previousHigh.isNaN && !previousLow.isNaN

    override fun toString(): String {
        return "StandardReversal($level) => $value"
    }
}
