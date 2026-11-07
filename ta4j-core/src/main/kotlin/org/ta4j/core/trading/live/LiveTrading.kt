/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.live

import java.time.Instant
import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.MultiTimeFrameSeries.TimeFrameState
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.Strategy

/**
 * Trading contest that traces bars and respective strategy
 *
 * @author Lukáš Kvídera
 */
class LiveTrading(
    private val series: MultiTimeFrameSeries<BarSeries>,
    private val strategy: Strategy,
    /** Record of reported fills; null when a custom runtime context without a record is used. */
    val tradingRecord: TradingRecord? = null,
) {
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


    /**
     * Reports an executed entry (e.g. a broker fill), so position-aware rules see the open position.
     *
     * @return false if a position is already open
     */
    fun enter(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean =
        requireRecord().enter(whenExecuted, pricePerAsset, amount)


    /**
     * Reports an executed exit, which closes the current position.
     *
     * @return false if no position is open
     */
    fun exit(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean =
        requireRecord().exit(whenExecuted, pricePerAsset, amount)


    private fun requireRecord(): TradingRecord =
        checkNotNull(tradingRecord) { "The runtime context of this live trading keeps no trading record" }
}
