/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * A builder to build a new [BacktestBarSeries].
 */
class BacktestBarSeriesBuilder {
    private var symbol = Symbol("UNDEFINED")
    private var numFactory = defaultNumFactory
    private var barBuilderFactory: BarBuilderFactory = BacktestBarBuilderFactory()
    private var timeFrame: TimeFrame = TimeFrame.DAY
    private var indicatorContext: IndicatorContext = IndicatorContext.empty(
        symbol = symbol,
        timeFrame = timeFrame
    )


    /**
     * Sets numFactory as default for whole runtime.
     *
     * @param numFactory to set [BacktestBarSeries.numFactory]
     *
     * @return `this`
     */
    fun withNumFactory(numFactory: NumFactory): BacktestBarSeriesBuilder {
        this.numFactory = numFactory
        defaultNumFactory = numFactory
        return this
    }


    /**
     * @param name to set [BacktestBarSeries.getName]
     *
     * @return `this`
     */
    fun withSymbol(name: Symbol): BacktestBarSeriesBuilder {
        this.symbol = name
        return this
    }


    /**
     * Mutually exclusive with [.withIndicatorContext]
     */
    fun withTimeFrame(timeFrame: TimeFrame): BacktestBarSeriesBuilder {
        this.timeFrame = timeFrame
        this.indicatorContext = IndicatorContext.empty(symbol = symbol, timeFrame = timeFrame)
        return this
    }


    /**
     * @param barBuilderFactory to build bars with the same datatype as series
     *
     * @return `this`
     */
    fun withBarBuilderFactory(barBuilderFactory: BarBuilderFactory): BacktestBarSeriesBuilder {
        this.barBuilderFactory = barBuilderFactory
        return this
    }


    /**
     * Mutually exclusive with [.withTimeFrame]
     */
    fun withIndicatorContext(indicatorContext: IndicatorContext): BacktestBarSeriesBuilder {
        this.indicatorContext = indicatorContext
        this.timeFrame = indicatorContext.timeFrame
        return this
    }


    fun build(): BacktestBarSeries {
        return BacktestBarSeries(
            symbol,
            timeFrame,
            numFactory,
            barBuilderFactory,
            listOf(indicatorContext)
        )
    }
}
