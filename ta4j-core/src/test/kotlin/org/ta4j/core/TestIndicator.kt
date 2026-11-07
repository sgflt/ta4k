/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.BacktestBarSeries

/**
 * Test wrapper for indicators that provides access to the underlying bar series.
 */
class TestIndicator<T>(
    val barSeries: BacktestBarSeries,
    private val indicator: Indicator<T>,
) : Indicator<T> {

    override val value: T
        get() = indicator.value

    override fun onBar(bar: Bar) {
        indicator.onBar(bar)
    }

    override val isStable: Boolean
        get() = indicator.isStable

    override val lag: Int
        get() = 0
}
