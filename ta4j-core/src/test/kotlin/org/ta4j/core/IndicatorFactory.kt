/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.api.Indicator
import org.ta4j.core.backtest.BacktestBarSeries

/**
 * Functional interface for creating Indicator instances with parameters.
 */
fun interface IndicatorFactory<I> {
    /**
     * Applies parameters and data to an IndicatorFactory and returns the Indicator.
     *
     * @param data source data for building the indicator
     * @param params indicator parameters
     * @return Indicator<I> with the indicator parameters applied
     */
    fun getIndicator(data: BacktestBarSeries, vararg params: Any): Indicator<I>
}