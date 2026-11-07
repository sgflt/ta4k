/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * IsFalling indicator.
 *
 * Satisfied when the values of the indicator decrease within the specified bar count.
 * Uses a memory-efficient sliding window approach that only tracks the count of falling
 * periods without storing the entire history.
 *
 * @param numFactory the number factory for calculations
 * @param indicator the indicator to monitor for falling values
 * @param barCount the time frame (number of bars to consider)
 * @param minStrength the minimum required strength of the falling (between 0.0 and 1.0)
 */
class IsFallingIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
    private val minStrength: Double = 1.0,
) : BooleanIndicator() {

    private val previousValue = PreviousNumericValueIndicator(numFactory, indicator, 1)
    private val oldestValue = PreviousNumericValueIndicator(numFactory, indicator, barCount)
    private val oldestPreviousValue = PreviousNumericValueIndicator(numFactory, indicator, barCount + 1)

    private var fallingCount = 0
    private var processedBars = 0

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
        require(minStrength in 0.0..1.0) { "Min strength must be between 0.0 and 1.0, but was: $minStrength" }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        previousValue.onBar(bar)
        oldestValue.onBar(bar)
        oldestPreviousValue.onBar(bar)

        processedBars++

        if (!previousValue.isStable) {
            // Not enough data yet
            value = false
            return
        }

        // Check if current value is falling compared to previous
        val isFalling = indicator.value < previousValue.value
        if (isFalling) fallingCount++

        // If we have more than barCount comparisons, subtract the oldest one
        if (processedBars > barCount && oldestValue.isStable && oldestPreviousValue.isStable) {
            val oldestWasFalling = oldestValue.value < oldestPreviousValue.value
            if (oldestWasFalling) fallingCount--
        }

        // Calculate ratio and determine if satisfied
        val currentWindowSize = minOf(barCount, processedBars - 1)
        val ratio = if (currentWindowSize > 0) fallingCount.toDouble() / currentWindowSize else 0.0

        value = ratio >= minStrength
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = previousValue.isStable && processedBars > 1

    override fun toString(): String {
        return "IsFalling($barCount, $minStrength) => $value"
    }
}
