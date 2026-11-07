/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.live

import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.MultiTimeFrameSeries.TimeFrameState
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.strategy.Strategy

/**
 * Trading contest that traces bars and respective strategy
 *
 * @author Lukáš Kvídera
 */
class LiveTrading(private val series: MultiTimeFrameSeries<BarSeries>, private val strategy: Strategy) {
    fun onCandle(event: CandleReceived) {
        this.series.onCandle(event)
    }


    val lastEventTimes: TimeFrameState
        get() = this.series.lastEventTimes


    fun shouldEnter(): Boolean {
        return this.strategy.shouldEnter()
    }


    fun shouldExit(): Boolean {
        return this.strategy.shouldExit()
    }
}
