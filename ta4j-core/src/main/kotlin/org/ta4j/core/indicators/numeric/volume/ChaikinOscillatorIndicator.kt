/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Chaikin Oscillator indicator.
 *
 * The Chaikin Oscillator is the difference between the 3-day EMA and 10-day EMA
 * of the Accumulation Distribution Line. It oscillates above and below the zero line.
 *
 * @param numFactory the number factory
 * @param shortBarCount the bar count for the short EMA (typically 3)
 * @param longBarCount the bar count for the long EMA (typically 10)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chaikin_oscillator">
 *      StockCharts - Chaikin Oscillator</a>
 */
class ChaikinOscillatorIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val shortBarCount: Int = 3,
    private val longBarCount: Int = 10,
) : NumericIndicator(numFactory) {

    private val accDist = AccumulationDistributionIndicator(numFactory)
    private val emaShort = accDist.ema(shortBarCount)
    private val emaLong = accDist.ema(longBarCount)

    init {
        require(shortBarCount > 0) { "Short bar count must be positive" }
        require(longBarCount > 0) { "Long bar count must be positive" }
        require(shortBarCount < longBarCount) { "Short bar count must be less than long bar count" }
    }

    private fun calculate() = emaShort.value - emaLong.value

    override fun updateState(bar: Bar) {
        emaShort.onBar(bar)
        emaLong.onBar(bar)
        value = calculate()
    }

    override val lag = longBarCount

    override val isStable: Boolean
        get() = emaShort.isStable && emaLong.isStable

    override fun toString(): String {
        return "ChaikinOscillator($shortBarCount, $longBarCount) => $value"
    }
}
