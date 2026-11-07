/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading

import java.time.Instant
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.api.series.PastCandleParadoxException
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.api.series.WrongTimeFrameException
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext

/**
 * Class deigned for use as live trading backing. Stores only current bar for analysis.
 *
 * Bar addition forces recalculation of all strategies.
 *
 * @author Lukáš Kvídera
 */
internal class LiveBarSeries(
    override val symbol: Symbol,
    override val timeFrame: TimeFrame,
    override val numFactory: NumFactory,
    private val barBuilderFactory: BarBuilderFactory,
    indicatorContext: IndicatorContext,
    runtimeContext: RuntimeContext,
) : BarSeries {
    private val barListeners = ArrayList<BarListener>().apply {
        add(indicatorContext)
        add(runtimeContext)
    }

    override lateinit var bar: Bar
        private set


    override val currentTime: Instant
        get() = if (::bar.isInitialized) bar.endTime else Instant.EPOCH


    override fun barBuilder() = barBuilderFactory.createBarBuilder(this) as LightweightBarBuilder

    override fun addBar(bar: Bar) {
        if (bar.endTime.isBefore(currentTime)) {
            throw PastCandleParadoxException(bar.endTime, currentTime)
        }

        this.bar = bar

        for (barListener in barListeners) {
            barListener.onBar(bar)
        }
    }


    override fun addBarListener(listener: BarListener) {
        barListeners.add(listener)
    }


    override fun onCandle(event: CandleReceived) {
        checkTimeFrame(event)

        barBuilder()
            .startTime(event.beginTime)
            .endTime(event.beginTime)
            .openPrice(event.openPrice)
            .highPrice(event.highPrice)
            .lowPrice(event.lowPrice)
            .closePrice(event.closePrice)
            .volume(event.volume)
            .add()
    }


    private fun checkTimeFrame(candleReceived: CandleReceived) {
        if (candleReceived.timeFrame != timeFrame) {
            throw WrongTimeFrameException(timeFrame, candleReceived.timeFrame)
        }
    }
}
