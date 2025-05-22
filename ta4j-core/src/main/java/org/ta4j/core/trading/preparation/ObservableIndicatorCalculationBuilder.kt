/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.trading.preparation

import org.slf4j.LoggerFactory
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContext
import org.ta4j.core.indicators.IndicatorChangeListener
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory
import org.ta4j.core.trading.DefaultMarketEventHandler
import org.ta4j.core.trading.LightweightBarBuilderFactory
import org.ta4j.core.trading.LiveBarSeries

/**
 * A builder to build a new [BacktestBarSeries].
 */
class ObservableIndicatorCalculationBuilder {
    private var name: String = UNNAMED_SERIES_NAME
    private var numFactory = defaultNumFactory
    private var barBuilderFactory: BarBuilderFactory = LightweightBarBuilderFactory()
    private var indicatorContexts: IndicatorContexts = IndicatorContexts.empty()
    private val contextUpdateListeners = mutableListOf<IndicatorContextUpdateListener>()
    private val indicatorChangeListeners = mutableListOf<IndicatorChangeListener>()


    /**
     * @param numFactory to set [BacktestBarSeries.numFactory]
     *
     * @return `this`
     */
    fun withNumFactory(numFactory: NumFactory) = apply {
        this.numFactory = numFactory
    }


    /**
     * @param name to set [BacktestBarSeries.getName]
     *
     * @return `this`
     */
    fun withName(name: String) = apply {
        this.name = name
    }


    /**
     * @param barBuilderFactory to build bars with the same datatype as series
     *
     * @return `this`
     */
    fun withBarBuilderFactory(barBuilderFactory: BarBuilderFactory) = apply {
        this.barBuilderFactory = barBuilderFactory
    }


    fun withIndicatorContexts(indicatorContexts: IndicatorContexts) = apply {
        this.indicatorContexts = indicatorContexts
    }

    fun withIndicatorContextUpdateListener(updateListener: IndicatorContextUpdateListener) = apply {
        this.contextUpdateListeners += updateListener
    }

    fun withIndicatorChangeListener(changeListener: IndicatorChangeListener) = apply {
        this.indicatorChangeListeners += changeListener
    }

    /**
     * Have to be called after initialization of indicatorContexts.
     *
     * @return instance for live trading or live indicator calculation
     */
    fun build(): MarketEventHandler {

        val series = MultiTimeFrameSeries<BarSeries>().apply {
            indicatorContexts.timeFrames.forEach { add(createSeriesPerTimeFrame(it)) }
        }

        contextUpdateListeners.forEach { listener -> indicatorContexts.register(listener) }
        indicatorChangeListeners.forEach { listener -> indicatorContexts.register(listener) }

        return DefaultMarketEventHandler(series)
    }


    private fun createSeriesPerTimeFrame(timeFrame: TimeFrame): LiveBarSeries {
        return LiveBarSeries(
            name = name,
            timeFrame = timeFrame,
            numFactory = numFactory,
            barBuilderFactory = barBuilderFactory,
            indicatorContext = indicatorContexts[timeFrame],
            runtimeContext = NOOPRuntimeContext,
        )
    }

    companion object {
        /** The [.name] for an unnamed bar series.  */
        private const val UNNAMED_SERIES_NAME = "unnamed_series"
        val log = LoggerFactory.getLogger(ObservableIndicatorCalculationBuilder::class.java)
    }
}
