/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.signal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.aggregator.BarAggregator
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.api.series.Symbol
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
    private var symbol = Symbol(UNNAMED_SERIES_NAME)
    private var numFactory = defaultNumFactory
    private var barBuilderFactory: BarBuilderFactory = LightweightBarBuilderFactory()
    private var strategyFactories = mutableListOf<StrategyFactory<Strategy>>()
    private var runtimeContext: RuntimeContext = NOOPRuntimeContext
    private var indicatorContexts = IndicatorContexts.empty()
    private var configuration = StrategyConfiguration()


    fun withNumFactory(numFactory: NumFactory) = apply {
        this.numFactory = numFactory
    }


    fun withSymbol(symbol: Symbol) = apply {
        this.symbol = symbol
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
            symbol = symbol,
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
