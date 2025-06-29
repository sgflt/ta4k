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
package org.ta4j.core.api.series

import java.time.Instant
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

/**
 * A [BarSeries] is a sequence of [Bar] separated by a
 * predefined period (e.g. 15 minutes, 1 day, etc.).
 */
interface BarSeries {
    /**
     * @return factory that generates numbers usable in this BarSeries
     */
    val numFactory: NumFactory

    /**
     * @return which timeframe this series accepts
     */
    val timeFrame: TimeFrame

    /**
     * May be used for loading candles from broker or database.
     *
     * Example use case: Daily analysis without realtime updates
     *
     *  1. app wants to analyze symbol
     *  2. series is not updated from stream, it contains stale data
     *  3. loader checks last candle for given timeframe
     *  4. loads required window length to provide stable indicators
     *  5. pushes data into [onCandle]
     */
    val currentTime: Instant

    /**
     * @return builder that generates compatible bars
     */
    fun barBuilder(): BarBuilder

    /**
     * @return the name of the series
     */
    val name: String

    /**
     * Gets the bar from series.
     *
     * @return the bar at the current position
     * @throws IllegalStateException if the series is empty
     */
    val bar: Bar


    /**
     * Adds the `bar` at the end of the series.
     *
     * @param bar the bar to be added
     *
     * @throws PastCandleParadoxException if you try to push old candle into series that is relatively in future to
     * that candle.
     * @throws NullPointerException if bar is null
     */
    fun addBar(bar: Bar)

    /**
     * @param listener that is interested in bars with defined num implementation instead of market events
     */
    fun addBarListener(listener: BarListener)

    /**
     * Event listener for market event in form of candle.
     *
     * @param event that holds OHLCV data
     *
     * @throws PastCandleParadoxException if you try to push old candle into series that is relatively in future to
     * that candle.
     * @throws WrongTimeFrameException if you try to mix candles from different timeframes than this series expect
     * @throws NullPointerException if event is null
     */
    fun onCandle(event: CandleReceived)
}
