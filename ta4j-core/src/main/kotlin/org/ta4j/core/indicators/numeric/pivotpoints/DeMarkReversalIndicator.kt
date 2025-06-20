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

enum class DeMarkPivotLevel {
    /**
     * Resistance level calculation.
     */
    RESISTANCE,

    /**
     * Support level calculation.
     */
    SUPPORT
}

/**
 * DeMark Reversal Indicator.
 *
 * Calculates resistance and support levels based on DeMark pivot points:
 * - Resistance = (Pivot Point × 2) - Previous Period Low
 * - Support = (Pivot Point × 2) - Previous Period High
 *
 * @param pivotPointIndicator the DeMark pivot point indicator
 * @param level the type of reversal level to calculate (RESISTANCE or SUPPORT)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
 *      StockCharts - Pivot Points</a>
 */
class DeMarkReversalIndicator(
    private val pivotPointIndicator: DeMarkPivotPointIndicator,
    private val level: DeMarkPivotLevel,
) : NumericIndicator(pivotPointIndicator.numFactory) {

    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()

    // Store previous bar's high/low for calculation
    private var previousHigh: Num = NaN
    private var previousLow: Num = NaN

    override fun updateState(bar: Bar) {
        // Update price indicators
        highPrice.onBar(bar)
        lowPrice.onBar(bar)

        // Update pivot point
        pivotPointIndicator.onBar(bar)

        // Calculate using previous bar's high/low
        value = calculate()

        // Store current bar's high/low for next calculation
        previousHigh = highPrice.value
        previousLow = lowPrice.value
    }

    private fun calculate(): Num {
        val pivot = pivotPointIndicator.value
        if (pivot.isNaN || previousHigh.isNaN || previousLow.isNaN) {
            return NaN
        }

        return when (level) {
            DeMarkPivotLevel.RESISTANCE -> (pivot * numFactory.two()) - previousLow
            DeMarkPivotLevel.SUPPORT -> (pivot * numFactory.two()) - previousHigh
        }
    }

    override val lag = pivotPointIndicator.lag

    override val isStable: Boolean
        get() = pivotPointIndicator.isStable && !previousHigh.isNaN && !previousLow.isNaN

    override fun toString(): String {
        return "DeMarkReversal($level) => $value"
    }
}
