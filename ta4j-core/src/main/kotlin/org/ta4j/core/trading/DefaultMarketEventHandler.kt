/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading

import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.NewsReceived
import org.ta4j.core.events.TickReceived

/**
 * This class handles market events
 */
class DefaultMarketEventHandler(private val series: MultiTimeFrameSeries<BarSeries>) : MarketEventHandler {
    override fun onCandle(event: CandleReceived) {
        series.onCandle(event)
    }

    override fun onTick(event: TickReceived) {
        TODO("Not yet implemented")
    }

    override fun onNews(event: NewsReceived) {
        TODO("Not yet implemented")
    }


    val lastEventTimes: MultiTimeFrameSeries.TimeFrameState
        get() = series.lastEventTimes
}
