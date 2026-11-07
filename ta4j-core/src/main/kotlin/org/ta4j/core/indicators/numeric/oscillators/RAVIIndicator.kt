/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Chande's Range Action Verification Index (RAVI) indicator.
 *
 * To preserve trend direction, default calculation does not use absolute value.
 * The RAVI oscillates around zero and can be used to identify trend strength
 * and potential reversal points.
 *
 * Formula: RAVI = ((Short SMA - Long SMA) / Long SMA) × 100
 *
 * @param numFactory the number factory for calculations
 * @param indicator the price indicator (typically close price)
 * @param shortSmaBarCount the time frame for the short SMA (usually 7)
 * @param longSmaBarCount the time frame for the long SMA (usually 65)
 *
 * @see <a href="https://www.investopedia.com/terms/r/range_accrual.asp">
 *      Range Action Verification Index</a>
 */
class RAVIIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val shortSmaBarCount: Int = 7,
    private val longSmaBarCount: Int = 65,
) : NumericIndicator(numFactory) {

    private val shortSma = indicator.sma(shortSmaBarCount)
    private val longSma = indicator.sma(longSmaBarCount)
    private val hundred = numFactory.hundred()

    init {
        require(shortSmaBarCount > 0) { "Short SMA bar count must be positive" }
        require(longSmaBarCount > 0) { "Long SMA bar count must be positive" }
        require(shortSmaBarCount < longSmaBarCount) { "Short SMA bar count must be less than long SMA bar count" }
    }

    private fun calculate(): Num {
        val shortMA = shortSma.value
        val longMA = longSma.value

        return if (longMA.isZero) {
            numFactory.zero()
        } else {
            (shortMA - longMA) / longMA * hundred
        }
    }

    override fun updateState(bar: Bar) {
        shortSma.onBar(bar)
        longSma.onBar(bar)
        value = calculate()
    }

    override val lag = longSmaBarCount

    override val isStable: Boolean
        get() = shortSma.isStable && longSma.isStable

    override fun toString(): String {
        return "RAVI($shortSmaBarCount, $longSmaBarCount) => $value"
    }
}
