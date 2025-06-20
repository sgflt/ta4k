/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.mocks

import java.time.Duration
import java.time.Instant
import java.util.stream.DoubleStream
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.utils.TimeFrameMapping.getDuration

/**
 * Generates BacktestBar implementations with mocked time or duration if not set
 * by tester.
 */
class MockMarketEventBuilder {
    private var startTime: Instant = Instant.ofEpochMilli(-1)
    private var defaultData = false
    private var candleEvents = ArrayList<CandleReceived>()
    private var candlesProduced = 0
    private var candleDuration = getDuration(TimeFrame.DAY)
    private var timeFrame = TimeFrame.DAY


    private fun createCandleSerialNumber(): Int {
        return ++candlesProduced
    }


    /**
     * Generates bars with given close prices.
     *
     * @param data close prices
     *
     * @return this
     */
    fun withCandlePrices(data: MutableList<Double?>): MockMarketEventBuilder = apply {
        data.forEach { d ->
            val startTime = startTime.plus(candleDuration.multipliedBy(createCandleSerialNumber().toLong()))
            candleEvents.add(
                CandleReceived(
                    timeFrame,
                    startTime,
                    startTime.plus(candleDuration),
                    d!!,
                    d,
                    d,
                    d,
                    1.0
                )
            )
        }
    }


    /**
     * Generates bars with given close prices.
     *
     * @param data close prices
     *
     * @return this
     */
    fun withCandlePrices(vararg data: Double): MockMarketEventBuilder = apply {
        withCandlePrices(DoubleStream.of(*data).boxed().toList())
    }


    fun withDefaultData(): MockMarketEventBuilder = apply {
        defaultData = true
    }


    private fun arbitraryBars() {
        val dataSetSize = 5000
        val candleEvents = ArrayList<CandleReceived>(dataSetSize)
        val timePeriod = candleDuration

        for (i in 0..<dataSetSize) {
            val beginTime = startTime.plus(timePeriod.multipliedBy(createCandleSerialNumber().toLong()))
            candleEvents.add(
                CandleReceived(
                    timeFrame,
                    beginTime,
                    beginTime.plus(timePeriod.multipliedBy(candlesProduced.toLong())),
                    (i + 1).toDouble(),
                    (i + 2).toDouble(),
                    (i + 3).toDouble(),
                    (i + 4).toDouble(),
                    (i + 5).toDouble()
                )
            )
        }

        this.candleEvents = candleEvents
    }


    fun build(): List<MarketEvent> {
        if (defaultData) {
            arbitraryBars()
        }

        return candleEvents.toList()
    }


    fun withCandleDuration(candleDuration: Duration): MockMarketEventBuilder = apply {
        this.candleDuration = candleDuration
    }


    fun withStartTime(startTime: Instant): MockMarketEventBuilder = apply {
        this.startTime = startTime
    }

    fun withTimeFrame(timeFrame: TimeFrame): MockMarketEventBuilder = apply {
        this.timeFrame = timeFrame
    }


    fun candle(): MockCandleBuilder {
        return MockCandleBuilder()
    }


    inner class MockCandleBuilder {
        private val startTime = this@MockMarketEventBuilder.startTime.plus(
            this@MockMarketEventBuilder.candleDuration.multipliedBy(
                createCandleSerialNumber().toLong()
            )
        )
        private val endTime = startTime.plus(this@MockMarketEventBuilder.candleDuration)
        private var open = 0.0
        private var close = 0.0
        private var high = 0.0
        private var low = 0.0
        private var volume = 0.0
        private var timeFrame = this@MockMarketEventBuilder.timeFrame


        fun openPrice(open: Double): MockCandleBuilder = apply {
            this.open = open
        }


        fun closePrice(close: Double): MockCandleBuilder = apply {
            this.close = close
        }


        fun highPrice(high: Double): MockCandleBuilder = apply {
            this.high = high
        }


        fun lowPrice(low: Double): MockCandleBuilder = apply {
            this.low = low
        }


        fun timeFrame(timeFrame: TimeFrame): MockCandleBuilder = apply {
            this.timeFrame = timeFrame
        }


        fun volume(volume: Double): MockCandleBuilder = apply {
            this.volume = volume
        }


        fun add(): MockMarketEventBuilder {
            this@MockMarketEventBuilder.candleEvents.add(
                CandleReceived(
                    timeFrame = timeFrame,
                    beginTime = startTime,
                    endTime = endTime,
                    openPrice = open,
                    closePrice = close,
                    highPrice = high,
                    lowPrice = low,
                    volume = volume
                )
            )
            return this@MockMarketEventBuilder
        }
    }
}
