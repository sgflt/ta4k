/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries

/**
 * @author Lukáš Kvídera
 */
internal class BacktestBarBuilderFactory : BarBuilderFactory {
    private lateinit var backtestBarBuilder: BacktestBarBuilder

    override fun createBarBuilder(series: BarSeries): BacktestBarBuilder {
        if (!::backtestBarBuilder.isInitialized) {
            backtestBarBuilder = BacktestBarBuilder(series)
        }

        return backtestBarBuilder
    }
}
