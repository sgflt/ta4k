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
package org.ta4j.core.indicators.numeric.momentum

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class IntraDayMomentumIndexIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate IMI correctly with mixed positive and negative moves`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // Bar 1: close > open (+2)
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH, Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                10.0, 12.0, 9.0, 12.0, 1000.0
            ),
            // Bar 2: close > open (+1)
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(1, ChronoUnit.DAYS), Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                12.0, 14.0, 11.0, 13.0, 1100.0
            ),
            // Bar 3: close < open (-2)
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(2, ChronoUnit.DAYS), Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                13.0, 15.0, 10.0, 11.0, 1200.0
            ),
            // Bar 4: close > open (+3)
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(3, ChronoUnit.DAYS), Instant.EPOCH.plus(4, ChronoUnit.DAYS),
                11.0, 16.0, 10.0, 14.0, 1300.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(IntraDayMomentumIndexIndicator(numFactory, 3))

        // Bar 1: +2, IMI = 2/2 * 100 = 100%
        context.assertNext(100.0)

        // Bar 2: +2,+1, IMI = 3/3 * 100 = 100%
        context.assertNext(100.0)

        // Bar 3: +2,+1,-2, IMI = 3/5 * 100 = 60%
        context.assertNext(60.0)

        // Bar 4: +1,-2,+3 (sliding window), IMI = 4/6 * 100 = 66.67%
        context.assertNext(66.66666)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle doji bars with no movement`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // Doji bar: open = close
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH, Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                10.0, 12.0, 9.0, 10.0, 1000.0
            ),
            // Another doji
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(1, ChronoUnit.DAYS), Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                10.0, 11.0, 9.0, 10.0, 1100.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(IntraDayMomentumIndexIndicator(numFactory, 2))

        // No movement should result in 0 IMI
        context.assertNext(0.0)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle all negative moves`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // All bearish bars
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH, Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                12.0, 13.0, 9.0, 10.0, 1000.0
            ), // -2
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(1, ChronoUnit.DAYS), Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                10.0, 11.0, 8.0, 9.0, 1100.0
            ), // -1
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(2, ChronoUnit.DAYS), Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                9.0, 10.0, 6.0, 7.0, 1200.0
            ) // -2
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(IntraDayMomentumIndexIndicator(numFactory, 3))

        // Bar 1: 0/2 * 100 = 0%
        context.assertNext(0.0)

        // Bar 2: 0/3 * 100 = 0%
        context.assertNext(0.0)

        // Bar 3: 0/5 * 100 = 0%
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after barCount periods`(numFactory: NumFactory) {
        val marketEvents = listOf(
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH, Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                10.0, 11.0, 9.0, 11.0, 1000.0
            ),
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(1, ChronoUnit.DAYS), Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                11.0, 12.0, 10.0, 12.0, 1100.0
            ),
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(2, ChronoUnit.DAYS), Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                12.0, 13.0, 11.0, 13.0, 1200.0
            ),
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(3, ChronoUnit.DAYS), Instant.EPOCH.plus(4, ChronoUnit.DAYS),
                13.0, 14.0, 12.0, 14.0, 1300.0
            ),
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(4, ChronoUnit.DAYS), Instant.EPOCH.plus(5, ChronoUnit.DAYS),
                14.0, 15.0, 13.0, 15.0, 1400.0
            )
        )

        val indicator = IntraDayMomentumIndexIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(indicator)

        // Should not be stable initially
        context.advance()
        assertUnstable(indicator)

        context.advance()
        assertUnstable(indicator)

        // Should be stable after 3rd bar (barCount = 3)
        context.advance()
        assertStable(indicator)

        context.advance()
        assertStable(indicator)
    }

    @Test
    fun `should have correct lag value`() {
        val indicator = IntraDayMomentumIndexIndicator(org.ta4j.core.num.DoubleNumFactory, 14)
        assertThat(indicator.lag).isEqualTo(14)
    }

    @Test
    fun `should require positive bar count`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            IntraDayMomentumIndexIndicator(org.ta4j.core.num.DoubleNumFactory, 0)
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            IntraDayMomentumIndexIndicator(org.ta4j.core.num.DoubleNumFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain correct window size`(numFactory: NumFactory) {
        val marketEvents = listOf(
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH, Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                10.0, 11.0, 9.0, 12.0, 1000.0
            ), // +2
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(1, ChronoUnit.DAYS), Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                12.0, 13.0, 11.0, 13.0, 1100.0
            ), // +1
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(2, ChronoUnit.DAYS), Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                13.0, 15.0, 12.0, 11.0, 1200.0
            ), // -2
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(3, ChronoUnit.DAYS), Instant.EPOCH.plus(4, ChronoUnit.DAYS),
                11.0, 12.0, 10.0, 14.0, 1300.0
            ), // +3
            CandleReceived(
                TimeFrame.DAY, Instant.EPOCH.plus(4, ChronoUnit.DAYS), Instant.EPOCH.plus(5, ChronoUnit.DAYS),
                14.0, 16.0, 13.0, 13.0, 1400.0
            ) // -1
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(IntraDayMomentumIndexIndicator(numFactory, 2))

        // Bar 1: +2, IMI = 2/2 * 100 = 100%
        context.assertNext(100.0)

        // Bar 2: [+2,+1], IMI = 3/3 * 100 = 100%
        context.assertNext(100.0)

        // Bar 3: [+1,-2] (window size 2), IMI = 1/3 * 100 = 33.33%
        context.assertNext(33.3333)

        // Bar 4: [-2,+3] (window size 2), IMI = 3/5 * 100 = 60%
        context.assertNext(60.0)

        // Bar 5: [+3,-1] (window size 2), IMI = 3/4 * 100 = 75%
        context.assertNext(75.0)
    }
}
