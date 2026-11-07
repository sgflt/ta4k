/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.live

import org.slf4j.LoggerFactory
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContext
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory
import org.ta4j.core.strategy.NOOPStrategyFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.LightweightBarBuilderFactory
import org.ta4j.core.trading.LiveBarSeries

/**
 * A builder to build a new [BacktestBarSeries].
 */
class LiveTradingBuilder {
    private var windowSize: Int? = null
    private var symbol = Symbol(UNNAMED_SERIES_NAME)
    private var numFactory = defaultNumFactory
    private var barBuilderFactory: BarBuilderFactory = LightweightBarBuilderFactory()
    private var strategyFactory: StrategyFactory<Strategy> = NOOPStrategyFactory()
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
     */
    fun withBarBuilderFactory(barBuilderFactory: BarBuilderFactory) = apply {
        this.barBuilderFactory = barBuilderFactory
    }


    fun withStrategyFactory(strategy: StrategyFactory<Strategy>) = apply {
        this.strategyFactory = strategy
    }


    fun withRuntimeContext(runtimeContext: RuntimeContext) = apply {
        this.runtimeContext = runtimeContext
    }


    fun withIndicatorContexts(indicatorContexts: IndicatorContexts) = apply {
        this.indicatorContexts = indicatorContexts
        symbol = indicatorContexts.symbol
    }


    fun withConfiguration(configuration: StrategyConfiguration) = apply {
        this.configuration = configuration
    }

    fun enableHistory(windowSize: Int) = apply {
        this.windowSize = windowSize
    }

    /**
     * Have to be called after initialization of indicatorContexts.
     *
     * @return instance for live trading or live indicator calculation
     */
    fun build(): LiveTrading {
        if (strategyFactory is NOOPStrategyFactory) {
            log.warn("Using NOOP strategy")
        }

        val strategy = strategyFactory.createStrategy(configuration, runtimeContext, indicatorContexts)
        val series = MultiTimeFrameSeries<BarSeries>().apply {
            (strategy.timeFrames + indicatorContexts.timeFrames)
                .distinct()
                .forEach { add(createSeriesPerTimeFrame(it)) }
        }

        windowSize?.let { indicatorContexts.enableHistory(it) }

        return LiveTrading(series, strategy)
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
        private const val UNNAMED_SERIES_NAME = "UNDEFINED"
        val log = LoggerFactory.getLogger(LiveTradingBuilder::class.java)
    }
}
