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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class TimeSegmentedVolumeIndicatorTest {

    @Test
    fun `should throw exception for non-positive bar count`() {
        assertThrows<IllegalArgumentException> {
            TimeSegmentedVolumeIndicator(org.ta4j.core.num.DoubleNumFactory, 0)
        }

        assertThrows<IllegalArgumentException> {
            TimeSegmentedVolumeIndicator(org.ta4j.core.num.DoubleNumFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate TSV with original test data and barCount=1`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 1))

        // Original test data: close prices [10, 9, 10, 11, 12, 11, 11], volumes [0, 10, 10, 10, 10, 10, 10]
        val candles = listOf(
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH,
                Instant.EPOCH.plus(Duration.ofDays(1)),
                closePrice = 10.0,
                volume = 0.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(1)),
                Instant.EPOCH.plus(Duration.ofDays(2)),
                closePrice = 9.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(2)),
                Instant.EPOCH.plus(Duration.ofDays(3)),
                closePrice = 10.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(3)),
                Instant.EPOCH.plus(Duration.ofDays(4)),
                closePrice = 11.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(4)),
                Instant.EPOCH.plus(Duration.ofDays(5)),
                closePrice = 12.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(5)),
                Instant.EPOCH.plus(Duration.ofDays(6)),
                closePrice = 11.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(6)),
                Instant.EPOCH.plus(Duration.ofDays(7)),
                closePrice = 11.0,
                volume = 10.0
            )
        )
        context.withMarketEvents(candles)

        // Bar 0: difference = 0 (no previous), TSV = 0 * 0 = 0
        context.assertNextNaN()

        // Bar 1: difference = 9 - 10 = -1, TSV = -1 * 10 = -10
        context.assertNext(-10.0)

        // Bar 2: difference = 10 - 9 = 1, TSV = 1 * 10 = 10
        context.assertNext(10.0)

        // Bar 3: difference = 11 - 10 = 1, TSV = 1 * 10 = 10
        context.assertNext(10.0)

        // Bar 4: difference = 12 - 11 = 1, TSV = 1 * 10 = 10
        context.assertNext(10.0)

        // Bar 5: difference = 11 - 12 = -1, TSV = -1 * 10 = -10
        context.assertNext(-10.0)

        // Bar 6: difference = 11 - 11 = 0, TSV = 0 * 10 = 0
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate TSV with original test data and barCount=3`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 3))

        // Same test data as above
        val candles = listOf(
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH,
                Instant.EPOCH.plus(Duration.ofDays(1)),
                closePrice = 10.0,
                volume = 0.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(1)),
                Instant.EPOCH.plus(Duration.ofDays(2)),
                closePrice = 9.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(2)),
                Instant.EPOCH.plus(Duration.ofDays(3)),
                closePrice = 10.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(3)),
                Instant.EPOCH.plus(Duration.ofDays(4)),
                closePrice = 11.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(4)),
                Instant.EPOCH.plus(Duration.ofDays(5)),
                closePrice = 12.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(5)),
                Instant.EPOCH.plus(Duration.ofDays(6)),
                closePrice = 11.0,
                volume = 10.0
            ),
            CandleReceived(
                TimeFrame.DAY,
                Instant.EPOCH.plus(Duration.ofDays(6)),
                Instant.EPOCH.plus(Duration.ofDays(7)),
                closePrice = 11.0,
                volume = 10.0
            )
        )
        context.withMarketEvents(candles)

        // TSV components: [0, -10, 10, 10, 10, -10, 0]

        // Bar 0: TSV = 0 (only first component)
        context.assertNextNaN()
        assertUnstable(context.firstNumericIndicator!!)

        // Bar 1: TSV = 0 + (-10) = -10 (sum of first 2 components)
        context.assertNext(-10.0)
        assertUnstable(context.firstNumericIndicator!!)

        // Bar 2: TSV = 0 + (-10) + 10 = 0 (sum of first 3 components)
        context.assertNext(0.0)

        // Bar 3: TSV = (-10) + 10 + 10 = 10 (last 3 components)
        context.assertNext(10.0)

        // Bar 4: TSV = 10 + 10 + 10 = 30 (last 3 components)
        context.assertNext(30.0)

        // Bar 5: TSV = 10 + 10 + (-10) = 10 (last 3 components)
        context.assertNext(10.0)

        // Bar 6: TSV = 10 + (-10) + 0 = 0 (last 3 components)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant prices correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 3))

        // Add candles with same price (no change)
        val candles = (0..5).map { i ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(i.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(i + 1L)),
                closePrice = 100.0, // Same price
                volume = 1000.0
            )
        }

        context.withMarketEvents(candles)

        // All bars after first should have TSV = 0 (no price difference)
        context.fastForwardUntilStable()
        context.assertNext(0.0) // Second bar: difference = 0, TSV = 0
        context.assertNext(0.0) // Third bar: TSV = 0 + 0 = 0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle varying volumes correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 2))
            .withCandlePrices(100.0, 101.0, 102.0, 103.0, 110.0, 105.0)  // ✅ Simple price setup

        context.fastForwardUntilStable()
        context.assertNext(2.0)
        context.assertNext(8.0)
        context.assertNext(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar count correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 1))

        val prices = doubleArrayOf(100.0, 105.0, 95.0, 110.0)
        val volumes = doubleArrayOf(1000.0, 2000.0, 1500.0, 3000.0)

        val candles = (0..3).map { i ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(i.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(i + 1L)),
                closePrice = prices[i],
                volume = volumes[i]
            )
        }

        context.withMarketEvents(candles)

        // With barCount = 1, TSV should equal just the current component
        context.assertNextNaN() // First bar: no previous close

        context.assertNext(10000.0) // (105-100) * 2000 = 10000
        context.assertNext(-15000.0) // (95-105) * 1500 = -15000
        context.assertNext(45000.0) // (110-95) * 3000 = 45000
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should verify lag and stability properties`(numFactory: NumFactory) {
        val indicator = TimeSegmentedVolumeIndicator(numFactory, 5)

        // Lag should equal barCount
        assertThat(indicator.lag).isEqualTo(5)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(100.0, 101.0, 102.0, 103.0, 104.0, 105.0)

        // Should be unstable until we have filled the window
        context.assertNextNaN()
        assertUnstable(indicator)

        context.assertNext(1.0) // Bar 2
        assertUnstable(indicator)

        context.assertNext(2.0) // Bar 3
        assertUnstable(indicator)

        context.assertNext(3.0) // Bar 4
        assertUnstable(indicator)

        context.assertNext(4.0) // Bar 5
        assertUnstable(indicator)

        context.assertNext(5.0) // Bar 6
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle NaN values in price differences`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 2))

        // First bar should result in NaN for difference calculation
        val candle = CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = Instant.EPOCH,
            endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
            closePrice = 100.0,
            volume = 1000.0
        )
        context.withMarketEvents(listOf(candle))

        context.assertNextNaN()
        assertUnstable(context.firstNumericIndicator!!)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero volume correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(TimeSegmentedVolumeIndicator(numFactory, 2))

        val candles = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                closePrice = 100.0,
                volume = 1000.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                closePrice = 105.0,
                volume = 0.0 // Zero volume
            )
        )

        context.withMarketEvents(candles)

        context.advance()
        context.assertNext(0.0) // Second bar: (105-100) * 0 = 0
    }

    @Test
    fun `toString should return meaningful representation`() {
        val indicator = TimeSegmentedVolumeIndicator(org.ta4j.core.num.DoubleNumFactory, 10)
        assertThat(indicator.toString()).startsWith("TSV(10)")
    }
}
