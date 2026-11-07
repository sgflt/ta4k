/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api

import org.ta4j.core.api.series.Bar

/**
 * Indicator over a [bar series][BarSeries].
 *
 *
 *
 * Returns a value of type **T** for each index of the bar series.
 *
 * @param <T> the type of the returned value (Double, Boolean, etc.)
</T> */
interface Indicator<out T> {
    /**
     * @return the value of the indicator
     */
    val value: T

    /**
     * @return how much this indicator lags behind the current bar. Usually it is a barCount. Indicators without lag returns 0.
     */
    val lag: Int

    /**
     * updates its state based on current bar
     *
     * Implementation of indicator should be aware of that it may be called multiple
     * times for single bar. If there is extensive calculation, implementation may
     * count on that for each bar there will be discrete time passed that may be
     * used for caching purposes.
     *
     * Backtesting may rewind time to past, this event should invalidate calculated
     * value.
     *
     * @param bar current time
     */
    fun onBar(bar: Bar)

    /**
     * @return true if indicator is stabilized
     */
    val isStable: Boolean
}
