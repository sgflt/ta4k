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

package org.ta4j.core.indicators.numeric.pivotpoints

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

class PivotPointIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate bar-based pivot points correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 25.0, 12.0)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.BARBASED))

        // First bar - no pivot calculated yet
        context.assertNextNaN()

        // Second bar - pivot = (10 + 10 + 10) / 3 = 10.0
        context.assertNext(10.0)

        // Third bar - pivot = (20 + 20 + 20) / 3 = 20.0
        context.assertNext(20.0)

        // Fourth bar - pivot = (15 + 15 + 15) / 3 = 15.0
        context.assertNext(15.0)

        // Fifth bar - pivot = (25 + 25 + 25) / 3 = 25.0
        context.assertNext(25.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate bar-based pivot with different OHLC values`(numFactory: NumFactory) {
        val events = MockMarketEventBuilder()
            .withStartTime(Instant.EPOCH)
            .withCandleDuration(Duration.ofDays(1))
            .candle().openPrice(10.0).highPrice(15.0).lowPrice(8.0).closePrice(12.0).add()
            .candle().openPrice(12.0).highPrice(18.0).lowPrice(10.0).closePrice(16.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.BARBASED))

        // First bar - no pivot calculated yet
        context.assertNextNaN()

        // Second bar - pivot = (15 + 8 + 12) / 3 = 11.67 (approximately)
        context.assertNext(11.666666666666666)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle daily pivot calculation`(numFactory: NumFactory) {
        val startTime = Instant.parse("2023-01-01T00:00:00Z")

        val events = MockMarketEventBuilder()
            .withStartTime(startTime)
            .withCandleDuration(Duration.ofDays(1))
            // Day 1
            .candle().openPrice(100.0).highPrice(110.0).lowPrice(95.0).closePrice(105.0).add()
            // Day 2
            .candle().openPrice(105.0).highPrice(115.0).lowPrice(100.0).closePrice(108.0).add()
            // Day 3
            .candle().openPrice(108.0).highPrice(120.0).lowPrice(105.0).closePrice(112.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.DAY))

        // First and second day - no pivot calculated yet
        context.assertNextNaN()
        context.assertNextNaN()

        // Third day - pivot = (115 + 100 + 108) / 3 = 107.67
        context.assertNext(107.66666666666667)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle intraday data with daily pivot calculation`(numFactory: NumFactory) {
        val startTime = Instant.parse("2023-01-01T09:00:00Z")

        val events = MockMarketEventBuilder()
            .withStartTime(startTime)
            .withCandleDuration(Duration.ofHours(1))
            // Day 1 - multiple hours
            .candle().openPrice(100.0).highPrice(105.0).lowPrice(99.0).closePrice(103.0).add()
            .candle().openPrice(103.0).highPrice(108.0).lowPrice(102.0).closePrice(106.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            // Day 2
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            .candle().openPrice(106.0).highPrice(110.0).lowPrice(104.0).closePrice(108.0).add()
            // Day 3
            .candle().openPrice(108.0).highPrice(112.0).lowPrice(107.0).closePrice(110.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.DAY))

        repeat(37) {
            context.assertNextNaN()
        }

        context.assertNext(107.333333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge case with single bar per period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.BARBASED))

        // First bar - no pivot yet
        context.assertNextNaN()

        // Subsequent bars get pivots from previous bars
        context.assertNext(100.0)  // (100+100+100)/3
        context.assertNext(110.0)  // (110+110+110)/3
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should become stable after first period completion`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0)
            .withIndicator(PivotPointIndicator(numFactory, TimeLevel.BARBASED))

        // Initially not stable
        context.advance()
        Assertions.assertThat(context.firstNumericIndicator!!.isStable).isFalse()

        // After second bar, should be stable
        context.advance()
        Assertions.assertThat(context.firstNumericIndicator!!.isStable).isTrue()
    }

    @Test
    fun `should use correct string representation`() {
        val numFactory = NumFactoryProvider.defaultNumFactory

        val indicator = PivotPointIndicator(numFactory, TimeLevel.DAY)
        Assertions.assertThat(indicator.toString()).contains("PivotPoint")
        Assertions.assertThat(indicator.toString()).contains("DAY")
    }
}
