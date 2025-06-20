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
package org.ta4j.core.indicators.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Difference Percentage Indicator.
 *
 * Returns the percentage difference from the last time the [percentageThreshold] was reached.
 * If the threshold is 0 or not specified, only the percentage difference from the previous
 * value is returned.
 *
 * @param indicator the source indicator
 * @param percentageThreshold the threshold percentage (default: 0)
 */
class DifferencePercentageIndicator(
    private val indicator: NumericIndicator,
    private val percentageThreshold: Num = indicator.numFactory.zero(),
) : NumericIndicator(indicator.numFactory) {

    /**
     * Constructor with Number threshold.
     *
     * @param indicator the source indicator
     * @param percentageThreshold the threshold percentage as Number
     */
    constructor(indicator: NumericIndicator, percentageThreshold: Number) : this(
        indicator,
        indicator.numFactory.numOf(percentageThreshold)
    )

    private var lastNotification: Num? = null
    private val hundred = numFactory.hundred()

    init {
        require(!percentageThreshold.isNaN) { "Percentage threshold cannot be NaN" }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)

        val currentValue = indicator.value

        if (currentValue.isNaN || currentValue.isZero) {
            value = NaN
            return
        }

        // Initialize last notification on first valid value
        if (lastNotification == null) {
            lastNotification = currentValue
            value = numFactory.zero() // No change on first value
            return
        }

        val lastNotificationValue = lastNotification!!

        // Calculate percentage change from last notification
        val changeFraction = currentValue / lastNotificationValue
        val changePercentage = fractionToPercentage(changeFraction)

        // Update last notification if threshold is exceeded
        if (changePercentage.abs() >= percentageThreshold) {
            lastNotification = currentValue
        }

        value = changePercentage
    }

    private fun fractionToPercentage(changeFraction: Num): Num {
        return changeFraction * hundred - hundred
    }

    override val lag = 1

    override val isStable: Boolean
        get() = indicator.isStable && lastNotification != null

    override fun toString(): String {
        return "DifferencePercentage($percentageThreshold) => $value"
    }
}
