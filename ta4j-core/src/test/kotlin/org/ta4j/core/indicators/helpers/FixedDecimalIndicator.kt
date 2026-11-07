/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * A fixed `Num` indicator.
 *
 *
 *
 * Returns constant [Num] values for a bar.
 */
class FixedDecimalIndicator : NumericIndicator {
    private val fixedIndicator: FixedIndicator<Num>


    /**
     * Constructor.
     *
     * @param series the bar series
     * @param values the values to be returned by this indicator
     */
    constructor(series: BarSeries, vararg values: Double) : super(series.numFactory) {
        fixedIndicator = FixedIndicator<Num>()
        for (value in values) {
            fixedIndicator.addValue(series.numFactory.numOf(value))
        }
    }


    /**
     * Constructor.
     *
     * @param series the bar series
     * @param values the values to be returned by this indicator
     */
    constructor(series: BarSeries, vararg values: String) : super(series.numFactory) {
        fixedIndicator = FixedIndicator<Num>()
        for (value in values) {
            fixedIndicator.addValue(series.numFactory.numOf(value))
        }
    }


    private fun calculate() = fixedIndicator.value

    override fun updateState(bar: Bar) {
        fixedIndicator.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = fixedIndicator.lag


    override val isStable
        get() = fixedIndicator.isStable
}
