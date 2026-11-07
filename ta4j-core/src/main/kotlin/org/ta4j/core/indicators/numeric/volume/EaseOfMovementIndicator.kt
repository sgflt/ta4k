/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Ease of Movement (Richard Arms): average over [barCount] bars of
 * `(midpoint - previous midpoint) / ((volume / volumeDivisor) / (high - low))`.
 *
 * A bar with no previous bar, zero volume or zero range has no raw value; the average is NaN while such a bar is in
 * the window.
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:ease_of_movement_emv">StockCharts</a>
 */
class EaseOfMovementIndicator(
    private val highPrice: NumericIndicator,
    private val lowPrice: NumericIndicator,
    private val volume: NumericIndicator,
    private val barCount: Int = 14,
    volumeDivisor: Number = 100_000_000L,
) : NumericIndicator(highPrice.numFactory) {

    private val divisor = numFactory.numOf(volumeDivisor)
    private val two = numFactory.two()
    private val window = ArrayDeque<Num>(barCount)
    private var sum = numFactory.zero()
    private var invalidInWindow = 0
    private var previousMidpoint: Num = NaN

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
        require(divisor.isPositive) { "Volume divisor must be positive, but was: $volumeDivisor" }
    }

    private fun rawValue(): Num {
        val high = highPrice.value
        val low = lowPrice.value
        val midpoint = (high + low) / two
        val previous = previousMidpoint
        previousMidpoint = midpoint

        val range = high - low
        val barVolume = volume.value
        if (previous.isNaN || midpoint.isNaN || barVolume.isNaN || barVolume.isZero || range.isZero) {
            return NaN
        }
        return (midpoint - previous) / (barVolume / divisor / range)
    }

    private fun push(raw: Num) {
        if (window.size == barCount) {
            val old = window.removeFirst()
            if (old.isNaN) --invalidInWindow else sum -= old
        }
        window.addLast(raw)
        if (raw.isNaN) ++invalidInWindow else sum += raw
    }

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        volume.onBar(bar)
        push(rawValue())
        value = if (window.size < barCount || invalidInWindow > 0) NaN else sum / numFactory.numOf(barCount)
    }

    override val lag = barCount + 1

    override val isStable: Boolean
        get() = window.size >= barCount && !value.isNaN

    override fun toString() = "EaseOfMovement($barCount) => $value"
}
