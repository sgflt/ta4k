package org.ta4j.core.backtest

import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries

/**
 * @author Lukáš Kvídera
 */
internal class BacktestBarBuilderFactory : BarBuilderFactory {
    private var backtestBarBuilder: BacktestBarBuilder? = null


    override fun createBarBuilder(series: BarSeries): BacktestBarBuilder {
        if (backtestBarBuilder == null) {
            backtestBarBuilder = BacktestBarBuilder(series)
        }

        return backtestBarBuilder!!
    }
}
