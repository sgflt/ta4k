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

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class IIIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return zero for first bar`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return zero when high equals low`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 100.0,
                highPrice = 100.0, // Same as low
                lowPrice = 100.0,
                closePrice = 100.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 100.0,
                highPrice = 100.0, // Same as low
                lowPrice = 100.0,
                closePrice = 100.0,
                volume = 1000.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0) // First bar
        context.assertNext(0.0) // Second bar (division by zero case)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return zero when volume is zero`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 100.0,
                highPrice = 110.0,
                lowPrice = 90.0,
                closePrice = 105.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 105.0,
                highPrice = 115.0,
                lowPrice = 95.0,
                closePrice = 110.0,
                volume = 0.0 // Zero volume
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0) // First bar
        context.assertNext(0.0) // Second bar (zero volume case)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate III correctly for positive case`(numFactory: NumFactory) {
        // Case where close is closer to high than low (positive III)
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 100.0,
                highPrice = 110.0,
                lowPrice = 90.0,
                closePrice = 105.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 105.0,
                highPrice = 120.0,
                lowPrice = 100.0,
                closePrice = 115.0, // Closer to high
                volume = 2000.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0) // First bar

        // Second bar calculation:
        // close = 115, high = 120, low = 100, volume = 2000
        // III = (2*115 - 120 - 100) / ((120 - 100) * 2000)
        // III = (230 - 220) / (20 * 2000) = 10 / 40000 = 0.00025
        context.assertNext(0.00025)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate III correctly for negative case`(numFactory: NumFactory) {
        // Case where close is closer to low than high (negative III)
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 100.0,
                highPrice = 110.0,
                lowPrice = 90.0,
                closePrice = 105.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 105.0,
                highPrice = 120.0,
                lowPrice = 100.0,
                closePrice = 105.0, // Closer to low
                volume = 1000.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0) // First bar

        // Second bar calculation:
        // close = 105, high = 120, low = 100, volume = 1000
        // III = (2*105 - 120 - 100) / ((120 - 100) * 1000)
        // III = (210 - 220) / (20 * 1000) = -10 / 20000 = -0.0005
        context.assertNext(-0.0005)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle close at midpoint correctly`(numFactory: NumFactory) {
        // Case where close is exactly at midpoint (III should be close to zero)
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 100.0,
                highPrice = 110.0,
                lowPrice = 90.0,
                closePrice = 105.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 105.0,
                highPrice = 120.0,
                lowPrice = 100.0,
                closePrice = 110.0, // Exactly at midpoint (100+120)/2 = 110
                volume = 1000.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        context.assertNext(0.0) // First bar

        // Second bar calculation:
        // close = 110, high = 120, low = 100, volume = 1000
        // III = (2*110 - 120 - 100) / ((120 - 100) * 1000)
        // III = (220 - 220) / (20 * 1000) = 0 / 20000 = 0
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after first bar`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
            .withIndicator(IIIIndicator(numFactory))

        val indicator = context.firstNumericIndicator!!

        // Not stable initially
        assertThat(indicator.isStable).isFalse()

        context.fastForward(1)
        assertThat(indicator.isStable).isTrue()

        context.fastForward(1)
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have zero lag`(numFactory: NumFactory) {
        val indicator = IIIIndicator(numFactory)
        assertThat(indicator.lag).isEqualTo(0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate III correctly for original test case`(numFactory: NumFactory) {
        val events = listOf(
            // First bar: open=0, close=10, high=12, low=8, volume=200
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(0),
                endTime = Instant.ofEpochSecond(86400),
                openPrice = 0.0,
                highPrice = 12.0,
                lowPrice = 8.0,
                closePrice = 10.0,
                volume = 200.0
            ),
            // Second bar: open=0, close=8, high=10, low=7, volume=100
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(86400),
                endTime = Instant.ofEpochSecond(172800),
                openPrice = 0.0,
                highPrice = 10.0,
                lowPrice = 7.0,
                closePrice = 8.0,
                volume = 100.0
            ),
            // Third bar: open=0, close=9, high=15, low=6, volume=300
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(172800),
                endTime = Instant.ofEpochSecond(259200),
                openPrice = 0.0,
                highPrice = 15.0,
                lowPrice = 6.0,
                closePrice = 9.0,
                volume = 300.0
            ),
            // Fourth bar: open=0, close=20, high=40, low=5, volume=50
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(259200),
                endTime = Instant.ofEpochSecond(345600),
                openPrice = 0.0,
                highPrice = 40.0,
                lowPrice = 5.0,
                closePrice = 20.0,
                volume = 50.0
            ),
            // Fifth bar: open=0, close=30, high=30, low=3, volume=600
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.ofEpochSecond(345600),
                endTime = Instant.ofEpochSecond(432000),
                openPrice = 0.0,
                highPrice = 30.0,
                lowPrice = 3.0,
                closePrice = 30.0,
                volume = 600.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(IIIIndicator(numFactory))

        // First bar: should return 0
        context.assertNext(0.0)

        // Second bar: (2*8-10-7)/((10-7)*100) = (16-17)/(3*100) = -1/300
        val expected1 = (2 * 8.0 - 10.0 - 7.0) / ((10.0 - 7.0) * 100.0)
        context.assertNext(expected1)

        // Third bar: (2*9-15-6)/((15-6)*300) = (18-21)/(9*300) = -3/2700
        val expected2 = (2 * 9.0 - 15.0 - 6.0) / ((15.0 - 6.0) * 300.0)
        context.assertNext(expected2)

        // Fourth bar: (2*20-40-5)/((40-5)*50) = (40-45)/(35*50) = -5/1750
        val expected3 = (2 * 20.0 - 40.0 - 5.0) / ((40.0 - 5.0) * 50.0)
        context.assertNext(expected3)

        // Fifth bar: (2*30-30-3)/((30-3)*600) = (60-33)/(27*600) = 27/16200
        val expected4 = (2 * 30.0 - 30.0 - 3.0) / ((30.0 - 3.0) * 600.0)
        context.assertNext(expected4)
    }
}
