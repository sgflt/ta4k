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

package org.ta4j.core.trading

import java.time.Instant
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.api.series.PastCandleParadoxException
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
    override val name: String,
    override val timeFrame: TimeFrame,
    override val numFactory: NumFactory,
    private val barBuilderFactory: BarBuilderFactory,
    indicatorContext: IndicatorContext,
    runtimeContext: RuntimeContext,
) : BarSeries {
    private val barListeners: MutableList<BarListener> = ArrayList<BarListener>().apply {
        add(indicatorContext)
        add(runtimeContext)
    }

    override lateinit var bar: Bar
        private set


    override val currentTime: Instant
        get() = if (::bar.isInitialized) bar.endTime else Instant.EPOCH


    override fun barBuilder() = barBuilderFactory.createBarBuilder(this) as LightweightBarBuilder

    override fun addBar(bar: Bar) {
        if (bar.endTime.isBefore(bar.endTime)) {
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
            throw WrongTimeFrameException(
                timeFrame,
                candleReceived.timeFrame
            )
        }
    }
}
