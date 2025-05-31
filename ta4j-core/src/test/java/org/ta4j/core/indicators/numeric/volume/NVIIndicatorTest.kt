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
package org.ta4j.core.indicators.numeric.volume

import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame.Companion.DAY
import org.ta4j.core.num.NumFactory

class NVIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should start with default value of 1000`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(NVIIndicator(numFactory))
            .withCandlePrices(100.0)

        context.assertNext(1000.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should start with custom starting value`(numFactory: NumFactory) {
        val customStartingValue = numFactory.numOf(500)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(NVIIndicator(numFactory, customStartingValue))
            .withCandlePrices(100.0)

        context.assertNext(500.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should update NVI when volume decreases and price increases`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        // First bar: Price=100, Volume=1000, NVI=1000
        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 110.0, 800.0) // Price +10%, Volume decreases
            )
        )

        context.assertNext(1000.0) // First bar

        // NVI = Previous NVI * (1 + Price Change %)
        // NVI = 1000 * (1 + 0.10) = 1000 * 1.10 = 1100
        context.assertNext(1100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should update NVI when volume decreases and price decreases`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 90.0, 800.0) // Price -10%, Volume decreases
            )
        )

        context.assertNext(1000.0)

        // NVI = 1000 * (1 + (-0.10)) = 1000 * 0.90 = 900
        context.assertNext(900.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not update NVI when volume increases`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 120.0, 1500.0) // Price +20%, Volume increases
            )
        )

        context.assertNext(1000.0)
        context.assertNext(1000.0) // NVI unchanged despite price increase
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not update NVI when volume stays the same`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 110.0, 1000.0) // Price +10%, Volume same
            )
        )

        context.assertNext(1000.0)
        context.assertNext(1000.0) // NVI unchanged
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle multiple consecutive volume decreases`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 110.0, 900.0),  // +10%, volume down
                createCandle(Instant.ofEpochSecond(3), 121.0, 800.0)   // +10%, volume down again
            )
        )

        context.assertNext(1000.0) // First bar: 1000
        context.assertNext(1100.0) // Second bar: 1000 * 1.10 = 1100
        context.assertNext(1210.0) // Third bar: 1100 * 1.10 = 1210
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle mixed volume changes correctly`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 110.0, 800.0),  // Price +10%, volume down -> NVI updates
                createCandle(Instant.ofEpochSecond(3), 120.0, 1200.0), // Price +9.09%, volume up -> NVI stays same
                createCandle(Instant.ofEpochSecond(4), 125.0, 1000.0), // Price +4.17%, volume down -> NVI updates
            )
        )

        context.assertNext(1000.0) // 1000
        context.assertNext(1100.0) // 1000 * 1.10 = 1100
        context.assertNext(1100.0) // No change (volume increased)

        // Price change: (125 - 120) / 120 = 0.04167
        // NVI = 1100 * (1 + 0.04167) = 1100 * 1.04167 ≈ 1145.83
        context.assertNext(1145.833333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero price change when volume decreases`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 100.0, 1000.0),
                createCandle(Instant.ofEpochSecond(2), 100.0, 800.0) // Same price, volume down
            )
        )

        context.assertNext(1000.0)
        context.assertNext(1000.0) // No change as price didn't change
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate lag correctly`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        assertEquals(1, nvi.lag)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle real world example from documentation`(numFactory: NumFactory) {
        val nvi = NVIIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(nvi)

        // Example scenario: Smart money active during volume decreases
        context.withMarketEvents(
            listOf(
                createCandle(Instant.ofEpochSecond(1), 50.0, 2000.0),   // Base
                createCandle(Instant.ofEpochSecond(2), 52.0, 1800.0),   // +4%, vol down -> update
                createCandle(Instant.ofEpochSecond(3), 55.0, 2500.0),   // +5.77%, vol up -> no update
                createCandle(Instant.ofEpochSecond(4), 53.0, 2200.0),   // -3.64%, vol down -> update
                createCandle(Instant.ofEpochSecond(5), 56.0, 2800.0),   // +5.66%, vol up -> no update
            )
        )

        context.assertNext(1000.0) // Start: 1000
        context.assertNext(1040.0) // 1000 * (1 + 0.04) = 1040
        context.assertNext(1040.0) // No change (volume up)

        // Price change: (53 - 55) / 55 = -0.0364
        // NVI = 1040 * (1 - 0.0364) = 1040 * 0.9636 ≈ 1002.18
        context.assertNext(1002.181818)
        context.assertNext(1002.181818) // No change (volume up)
    }

    private fun createCandle(time: Instant, price: Double, volume: Double): CandleReceived {
        return CandleReceived(
            timeFrame = DAY,
            beginTime = time,
            endTime = time.plus(Duration.ofDays(1)),
            openPrice = price,
            highPrice = price,
            lowPrice = price,
            closePrice = price,
            volume = volume
        )
    }
}
