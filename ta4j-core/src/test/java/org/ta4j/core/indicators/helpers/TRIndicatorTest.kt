/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.indicators.helpers

import java.time.Duration
import java.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.num.NumFactory

class TRIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getValue(numFactory: NumFactory) {
        val trIndicator = TRIndicator(numFactory)

        // Create market events with specific OHLC values
        // Bar 0: O=0, C=12, H=15, L=8  -> TR = |15-8| = 7 (first bar, no previous close)
        // Bar 1: O=0, C=8, H=11, L=6   -> TR = max(|11-6|, |11-12|, |12-6|) = max(5, 1, 6) = 6
        // Bar 2: O=0, C=15, H=17, L=14 -> TR = max(|17-14|, |17-8|, |8-14|) = max(3, 9, 6) = 9
        // Bar 3: O=0, C=15, H=17, L=14 -> TR = max(|17-14|, |17-15|, |15-14|) = max(3, 2, 1) = 3
        // Bar 4: O=0, C=0, H=0, L=2    -> TR = max(|0-2|, |0-15|, |15-2|) = max(2, 15, 13) = 15

        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 15.0,
                lowPrice = 8.0,
                closePrice = 12.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 11.0,
                lowPrice = 6.0,
                closePrice = 8.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 0.0,
                highPrice = 17.0,
                lowPrice = 14.0,
                closePrice = 15.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                openPrice = 0.0,
                highPrice = 17.0,
                lowPrice = 14.0,
                closePrice = 15.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(5)),
                openPrice = 0.0,
                highPrice = 0.0,
                lowPrice = 2.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(trIndicator)
            .assertNext(7.0)   // First bar: |15-8| = 7
            .assertNext(6.0)   // Second bar: max(5, 1, 6) = 6
            .assertNext(9.0)   // Third bar: max(3, 9, 6) = 9
            .assertNext(3.0)   // Fourth bar: max(3, 2, 1) = 3
            .assertNext(15.0)  // Fifth bar: max(2, 15, 13) = 15
    }
}