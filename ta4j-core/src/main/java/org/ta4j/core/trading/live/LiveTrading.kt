/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
