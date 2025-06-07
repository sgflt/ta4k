/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
