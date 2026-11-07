/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.pivotpoints

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

enum class FibReversalType {
    SUPPORT, RESISTANCE
}

/**
 * Standard Fibonacci factors.
 */
enum class FibonacciFactor(val factor: Double) {
    FACTOR_1(0.382),
    FACTOR_2(0.618),
    FACTOR_3(1.0)
}

/**
 * Fibonacci Reversal Indicator.
 *
 * Calculates fibonacci-based reversal levels using the formula:
 * - Resistance = Pivot Point + (Fibonacci Factor × (High - Low))
 * - Support = Pivot Point - (Fibonacci Factor × (High - Low))
 *
 * @param pivotPointIndicator the pivot point indicator for this reversal
 * @param fibonacciFactor the fibonacci factor for this reversal
 * @param fibReversalType the type of reversal (SUPPORT, RESISTANCE)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
 *      StockCharts - Pivot Points</a>
 */
class FibonacciReversalIndicator(
    private val pivotPointIndicator: PivotPointIndicator,
    fibonacciFactor: Double,
    private val fibReversalType: FibReversalType,
) : NumericIndicator(pivotPointIndicator.numFactory) {

    private val fibonacciFactor: Num = numFactory.numOf(fibonacciFactor)
    private val highPrice = Indicators.extended(numFactory).highPrice()
    private val lowPrice = Indicators.extended(numFactory).lowPrice()

    // Store previous period's high/low for calculation
    private var previousHigh: Num = NaN
    private var previousLow: Num = NaN

    /**
     * Constructor with FibonacciFactor enum.
     */
    constructor(
        pivotPointIndicator: PivotPointIndicator,
        fibonacciFactor: FibonacciFactor,
        fibReversalType: FibReversalType,
    ) : this(pivotPointIndicator, fibonacciFactor.factor, fibReversalType)

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

        val pivotPointValue = pivotPointIndicator.value
        val fibValue = fibonacciFactor * (previousHigh - previousLow)

        return when (fibReversalType) {
            FibReversalType.RESISTANCE -> pivotPointValue + fibValue
            FibReversalType.SUPPORT -> pivotPointValue - fibValue
        }
    }

    override val lag = pivotPointIndicator.lag + 1

    override val isStable: Boolean
        get() = pivotPointIndicator.isStable && !previousHigh.isNaN && !previousLow.isNaN

    override fun toString(): String {
        return "FibonacciReversal($fibReversalType, ${fibonacciFactor.doubleValue()}) => $value"
    }
}
