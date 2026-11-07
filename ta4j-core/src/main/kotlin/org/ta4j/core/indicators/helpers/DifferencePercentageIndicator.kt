/*
 * SPDX-License-Identifier: MIT
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
