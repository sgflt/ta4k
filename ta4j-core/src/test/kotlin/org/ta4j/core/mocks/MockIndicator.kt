/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.mocks

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.Num

// FIXME isn't it FixedIndicator?
class MockIndicator(private val values: List<Num>) : Indicator<Num> {
    private var currentIndex = -1

    /**
     * Gets a value from the Indicator
     *
     * @return Num Indicator value at index
     */
    override val value
        get() = values[currentIndex]

    override fun onBar(bar: Bar) {
        ++currentIndex
    }

    override val isStable = true

    override val lag = 0
}
