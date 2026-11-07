/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import java.time.Instant
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame

/**
 * After initialization thread safe router for multiple timeframes.
 *
 * Single thread may update single timeframe.
 * Each timeframe have corresponding series nad indicator context.
 *
 * @author Lukáš Kvídera
 */
class MultiTimeFrameSeries<B : BarSeries> : BarListener {
    private val timeFramedSeries = HashMap<TimeFrame, B>()


    fun add(series: B) {
        timeFramedSeries.put(series.timeFrame, series)
    }


    /**
     * Passes candle event to series with the same time frame. If strategy does not define given time frame, then this is
     * NOOP.
     *
     * @param event to process
     */
    fun onCandle(event: CandleReceived) {
        timeFramedSeries[event.timeFrame]?.onCandle(event)
    }


    /**
     * Consume bar event and pass it to the series with the same time frame.
     * Usually Bar is created by the {@link BarAggregator}.
     */
    override fun onBar(bar: Bar) {
        timeFramedSeries[bar.timeFrame]?.addBar(bar)
    }

    val lastEventTimes: TimeFrameState
        get() {
            val timeFrameState = TimeFrameState()
            for (series in timeFramedSeries.values) {
                timeFrameState.add(series.timeFrame, series.currentTime)
            }

            return timeFrameState
        }


    /**
     * Contains information how much stale data are.
     */
    class TimeFrameState {
        private val state = HashMap<TimeFrame, Instant>()


        internal fun add(timeFrame: TimeFrame, time: Instant) {
            state.put(timeFrame, time)
        }


        fun getLastEventTime(timeFrame: TimeFrame): Instant {
            return state.getOrDefault(timeFrame, Instant.EPOCH)
        }
    }
}
