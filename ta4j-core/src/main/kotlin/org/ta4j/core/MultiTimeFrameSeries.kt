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
