/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.oscillators

import java.time.Duration
import java.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame.Companion.DAY
import org.ta4j.core.num.NumFactory

class PVOIndicatorTest {

    private val marketEvents = listOf(
        CandleReceived(DAY, Instant.EPOCH, Instant.EPOCH.plus(Duration.ofDays(1)), closePrice = 0.0, volume = 10.0),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(1)),
            Instant.EPOCH.plus(Duration.ofDays(2)),
            closePrice = 0.0,
            volume = 11.0
        ),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(2)),
            Instant.EPOCH.plus(Duration.ofDays(3)),
            closePrice = 0.0,
            volume = 12.0
        ),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(3)),
            Instant.EPOCH.plus(Duration.ofDays(4)),
            closePrice = 0.0,
            volume = 13.0
        ),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(4)),
            Instant.EPOCH.plus(Duration.ofDays(5)),
            closePrice = 0.0,
            volume = 150.0
        ),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(5)),
            Instant.EPOCH.plus(Duration.ofDays(6)),
            closePrice = 0.0,
            volume = 155.0
        ),
        CandleReceived(
            DAY,
            Instant.EPOCH.plus(Duration.ofDays(6)),
            Instant.EPOCH.plus(Duration.ofDays(7)),
            closePrice = 0.0,
            volume = 160.0
        )
    )

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculatePvoIndicator(numFactory: NumFactory) {
        val pvo = Indicators.extended(numFactory).pvo(1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(pvo)

        context.advance()
        context.assertNext(0.791855204)
        context.assertNext(2.164434756)
        context.assertNext(3.925400464)
        context.assertNext(55.296120101)
        context.assertNext(66.511722064)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `Calculate PV with running total with window 2`(numFactory: NumFactory) {
        val pvo = Indicators.extended(numFactory).pvo(2)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(pvo)

        // First value (index 0) - PVO typically needs some history to stabilize
        context.advance()

        context.assertNext(8.11380)
        context.assertNext(14.632328)
        context.assertNext(19.764380)
        context.assertNext(59.215585)
        context.assertNext(76.959974)
    }
}
