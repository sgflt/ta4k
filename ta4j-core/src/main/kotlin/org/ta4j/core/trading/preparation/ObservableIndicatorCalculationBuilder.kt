/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.preparation

import org.slf4j.LoggerFactory
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.api.series.Symbol
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
 * This class is usable for persistence of calculated indicators.
 */
class ObservableIndicatorCalculationBuilder {
    private var symbol: Symbol = Symbol(UNNAMED_SERIES_NAME)
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
            symbol = symbol,
            timeFrame = timeFrame,
            numFactory = numFactory,
            barBuilderFactory = barBuilderFactory,
            indicatorContext = indicatorContexts[timeFrame],
            runtimeContext = NOOPRuntimeContext,
        )
    }

    companion object {
        /** The [.symbol] for an unnamed bar series.  */
        private const val UNNAMED_SERIES_NAME = "unnamed_series"
        val log = LoggerFactory.getLogger(ObservableIndicatorCalculationBuilder::class.java)
    }
}
