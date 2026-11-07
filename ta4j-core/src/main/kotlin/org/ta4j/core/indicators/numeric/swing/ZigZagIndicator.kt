/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.swing

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/** Direction of the current ZigZag leg. */
enum class ZigZagTrend { UNDEFINED, UP, DOWN }

/**
 * ZigZag: ignores moves smaller than [reversalAmount] and confirms a swing when the confirmation price
 * (close by default) moves that far back from the leg's extreme.
 *
 * - while the trend is undefined it tracks the highest high and lowest low; the first unambiguous reversal from
 *   one of them sets the trend and confirms that pivot
 * - in an up leg it follows the highest high; a close at least `reversalAmount` below it confirms a swing high and
 *   starts a down leg from the current low (and the mirror image for down legs)
 *
 * The value is the price of the most recently confirmed pivot (NaN before the first). [pivotHigh] and [pivotLow]
 * are true on the confirmation bar, so there is no look-ahead. [reversalAmount] can be a constant or adaptive,
 * e.g. a multiple of ATR.
 *
 * @see <a href="https://www.investopedia.com/terms/z/zigzagindicator.asp">Investopedia</a>
 */
class ZigZagIndicator(
    private val highPrice: NumericIndicator,
    private val lowPrice: NumericIndicator,
    private val confirmationPrice: NumericIndicator,
    private val reversalAmount: NumericIndicator,
) : NumericIndicator(highPrice.numFactory) {

    var trend = ZigZagTrend.UNDEFINED
        private set

    /** Price of the last confirmed swing high, NaN before the first. */
    var lastHighPrice: Num = NaN
        private set

    /** Price of the last confirmed swing low, NaN before the first. */
    var lastLowPrice: Num = NaN
        private set

    /** Extreme of the current leg (highest high in an up leg, lowest low in a down leg). */
    var extremePrice: Num = NaN
        private set

    /** True on the bar that confirmed a swing high. */
    var highConfirmed = false
        private set

    /** True on the bar that confirmed a swing low. */
    var lowConfirmed = false
        private set

    private var initialHigh: Num = NaN
    private var initialLow: Num = NaN

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        confirmationPrice.onBar(bar)
        reversalAmount.onBar(bar)
        highConfirmed = false
        lowConfirmed = false

        val high = highPrice.value
        val low = lowPrice.value
        val confirmation = confirmationPrice.value
        val reversal = reversalAmount.value
        if (high.isNaN || low.isNaN || confirmation.isNaN || reversal.isNaN || !reversal.isPositive) return

        when (trend) {
            ZigZagTrend.UNDEFINED -> undefined(high, low, confirmation, reversal)
            ZigZagTrend.UP -> {
                val newExtreme = high > extremePrice
                if (newExtreme) extremePrice = high
                if (!newExtreme && extremePrice - confirmation >= reversal) confirmHigh(extremePrice, low)
            }

            ZigZagTrend.DOWN -> {
                val newExtreme = low < extremePrice
                if (newExtreme) extremePrice = low
                if (!newExtreme && confirmation - extremePrice >= reversal) confirmLow(extremePrice, high)
            }
        }
    }

    private fun undefined(high: Num, low: Num, confirmation: Num, reversal: Num) {
        val extendsHigh = initialHigh.isNaN || high > initialHigh
        val extendsLow = initialLow.isNaN || low < initialLow
        if (extendsHigh) initialHigh = high
        if (extendsLow) initialLow = low

        val confirmsUp = !extendsLow && confirmation - initialLow >= reversal
        val confirmsDown = !extendsHigh && initialHigh - confirmation >= reversal
        // a bar confirming both ways has no known intrabar order: wait
        if (confirmsUp && !confirmsDown) {
            confirmLow(initialLow, initialHigh)
        } else if (confirmsDown && !confirmsUp) {
            confirmHigh(initialHigh, initialLow)
        }
    }

    private fun confirmHigh(pivot: Num, nextExtreme: Num) {
        lastHighPrice = pivot
        value = pivot
        highConfirmed = true
        trend = ZigZagTrend.DOWN
        extremePrice = nextExtreme
    }

    private fun confirmLow(pivot: Num, nextExtreme: Num) {
        lastLowPrice = pivot
        value = pivot
        lowConfirmed = true
        trend = ZigZagTrend.UP
        extremePrice = nextExtreme
    }

    /** True on the bar that confirms a swing high. */
    fun pivotHigh(): BooleanIndicator = view("ZigZagPivotHigh") { highConfirmed }

    /** True on the bar that confirms a swing low. */
    fun pivotLow(): BooleanIndicator = view("ZigZagPivotLow") { lowConfirmed }

    /** Price of the most recent confirmed swing high. */
    fun recentSwingHigh(): NumericIndicator = numericView("RecentZigZagHigh") { lastHighPrice }

    /** Price of the most recent confirmed swing low. */
    fun recentSwingLow(): NumericIndicator = numericView("RecentZigZagLow") { lastLowPrice }

    private fun view(name: String, read: ZigZagIndicator.() -> Boolean): BooleanIndicator {
        val source = this
        return object : BooleanIndicator() {
            override fun updateState(bar: Bar) {
                source.onBar(bar)
                value = source.read()
            }

            override val lag: Int
                get() = source.lag

            override val isStable: Boolean
                get() = source.isStable

            override fun toString() = "$name => $value"
        }
    }

    private fun numericView(name: String, read: ZigZagIndicator.() -> Num): NumericIndicator {
        val source = this
        return object : NumericIndicator(numFactory) {
            override fun updateState(bar: Bar) {
                source.onBar(bar)
                value = source.read()
            }

            override val lag: Int
                get() = source.lag

            override val isStable: Boolean
                get() = !value.isNaN

            override fun toString() = "$name => $value"
        }
    }

    override val lag = 1

    /** Stable once the first swing is confirmed. */
    override val isStable: Boolean
        get() = trend != ZigZagTrend.UNDEFINED

    override fun toString() = "ZigZag($trend) => $value"
}
