/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.indicators.numeric.helpers.BuyingPressureIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Ultimate Oscillator (Larry Williams).
 *
 * BP = close - min(low, previous close), TR = true range, avgN = sum(BP, N) / sum(TR, N),
 * UO = 100 * (4 * avgShort + 2 * avgMiddle + avgLong) / 7.
 *
 * NaN while a true-range sum is zero.
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:ultimate_oscillator">StockCharts</a>
 */
class UltimateOscillatorIndicator(
    numFactory: NumFactory,
    private val shortPeriod: Int = 7,
    private val middlePeriod: Int = 14,
    private val longPeriod: Int = 28,
) : NumericIndicator(numFactory) {

    init {
        require(shortPeriod > 0) { "Short period must be positive, but was: $shortPeriod" }
        require(middlePeriod > shortPeriod) { "Middle period must be greater than short period" }
        require(longPeriod > middlePeriod) { "Long period must be greater than middle period" }
    }

    private val buyingPressure = BuyingPressureIndicator(numFactory)
    private val trueRange = TRIndicator(numFactory)
    private val averages = listOf(shortPeriod, middlePeriod, longPeriod).map {
        buyingPressure.runningTotal(it) to trueRange.runningTotal(it)
    }
    private val weights = listOf(4, 2, 1).map { numFactory.numOf(it) }
    private val totalWeight = numFactory.numOf(7)
    private val hundred = numFactory.hundred()

    override fun updateState(bar: Bar) {
        buyingPressure.onBar(bar)
        trueRange.onBar(bar)
        averages.forEach { (pressureSum, rangeSum) ->
            pressureSum.onBar(bar)
            rangeSum.onBar(bar)
        }
        value = calculate()
    }

    private fun calculate(): Num {
        var weighted = numFactory.zero()
        for ((index, sums) in averages.withIndex()) {
            val (pressureSum, rangeSum) = sums
            if (rangeSum.value.isZero || rangeSum.value.isNaN || pressureSum.value.isNaN) {
                return NaN
            }
            weighted += pressureSum.value / rangeSum.value * weights[index]
        }
        return weighted / totalWeight * hundred
    }

    override val lag = longPeriod

    override val isStable: Boolean
        get() = averages.all { (pressureSum, rangeSum) -> pressureSum.isStable && rangeSum.isStable }

    override fun toString() = "UltimateOscillator($shortPeriod, $middlePeriod, $longPeriod) => $value"
}
