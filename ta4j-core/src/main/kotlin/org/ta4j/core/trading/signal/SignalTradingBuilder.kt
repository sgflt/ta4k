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
package org.ta4j.core.trading.signal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.aggregator.BarAggregator
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContext
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.DefaultMarketEventHandler
import org.ta4j.core.trading.LightweightBarBuilderFactory
import org.ta4j.core.trading.LiveBarSeries

/**
 * A builder to build a new [BacktestBarSeries].
 */
class SignalTradingBuilder {
    private var windowSize: Int? = null
    private var name = UNNAMED_SERIES_NAME
    private var numFactory = defaultNumFactory
    private var barBuilderFactory: BarBuilderFactory = LightweightBarBuilderFactory()
    private var strategyFactories = mutableListOf<StrategyFactory<Strategy>>()
    private var runtimeContext: RuntimeContext = NOOPRuntimeContext
    private var indicatorContexts = IndicatorContexts.empty()
    private var configuration = StrategyConfiguration()


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


    fun withStrategyFactory(strategy: List<StrategyFactory<Strategy>>) = apply {
        this.strategyFactories += strategy
    }


    fun withRuntimeContext(runtimeContext: RuntimeContext) = apply {
        this.runtimeContext = runtimeContext
    }


    fun withIndicatorContexts(indicatorContexts: IndicatorContexts) = apply {
        this.indicatorContexts = indicatorContexts
    }


    fun withConfiguration(configuration: StrategyConfiguration) = apply {
        this.configuration = configuration
    }

    fun withWindowSize(windowSize: Int) = apply {
        this.windowSize = windowSize
    }

    /**
     * Have to be called after initialization of indicatorContexts.
     *
     * @return instance for live trading or live indicator calculation
     */
    fun build(): DefaultMarketEventHandler {
        require(strategyFactories.isNotEmpty()) { "No strategy factory provided" }

        strategyFactories.map {
            it.createStrategy(configuration, runtimeContext, indicatorContexts)
        }

        val timeFrames = indicatorContexts.timeFrames - TimeFrame.MINUTES_1
        val multiTimeFrameSeries = MultiTimeFrameSeries<BarSeries>().apply {
            timeFrames.forEach { add(createSeriesPerTimeFrame(it)) }
        }

        // Minute bar series is essential for each broker
        createSeriesPerTimeFrame(TimeFrame.MINUTES_1).let {
            multiTimeFrameSeries.add(it)
            val barAggregator = BarAggregator(timeFrames)
            it.addBarListener(barAggregator)
            barAggregator.addBarListener(multiTimeFrameSeries)
        }

        windowSize?.let { indicatorContexts.enableHistory(it) }

        return DefaultMarketEventHandler(multiTimeFrameSeries)
    }


    private fun createSeriesPerTimeFrame(timeFrame: TimeFrame): LiveBarSeries {
        return LiveBarSeries(
            name = name,
            timeFrame = timeFrame,
            numFactory = numFactory,
            barBuilderFactory = barBuilderFactory,
            indicatorContext = indicatorContexts[timeFrame],
            runtimeContext = runtimeContext,
        )
    }

    companion object {
        /** The [.name] for an unnamed bar series.  */
        private const val UNNAMED_SERIES_NAME = "unnamed_series"
        val log: Logger = LoggerFactory.getLogger(SignalTradingBuilder::class.java)
    }
}
