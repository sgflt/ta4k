/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar

/**
 * A fixed indicator.
 *
 *
 *
 * Returns constant values for a bar.
 *
 * @param <T> the type of returned constant values (Double, Boolean, etc.)
</T> */
internal class FixedIndicator<T> @SafeVarargs constructor(vararg values: T) : Indicator<T> {
    private val values = ArrayList<T>()
    private var index = -1


    /**
     * Constructor.
     *
     * @param values the values to be returned by this indicator
     */
    init {
        this.values.addAll(listOf<T>(*values))
    }


    /**
     * Adds the `value` to [.values].
     *
     * @param value the value to onCandle
     */
    fun addValue(value: T) {
        values.add(value)
    }


    override val value: T
        get() = values[index]

    override val lag = 0


    override fun onBar(bar: Bar) {
        ++index
    }


    override val isStable = true
}
