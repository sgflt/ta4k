/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers.previous

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar

/**
 * Returns the (n-th) previous value of an indicator.
 */
internal class PreviousValueHelper<T>(private val indicator: Indicator<T>, private val n: Int) : Indicator<T?> {
    private val previousValues = ArrayDeque<T>()
    override var value: T? = null
        private set
    override val lag = n
    private var currentBar: Bar? = null

    /**
     * Constructor.
     *
     * @param indicator the indicator from which to calculate the previous value
     * @param n parameter defines the previous n-th value
     */
    init {
        require(n >= 1) { "n must be positive number, but was: " + n }
    }

    private fun calculate(): T? {
        val currentValue = indicator.value
        previousValues.addLast(currentValue)

        if (previousValues.size > n) {
            return previousValues.removeFirst()
        }

        return null
    }

    override fun toString() = "PREV($n, $indicator) => $value"

    override fun onBar(bar: Bar) {
        if (bar !== currentBar) {
            updateState(bar)
            currentBar = bar
        }
    }

    fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = previousValues.size == n && value != null
}
