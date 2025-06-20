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
package org.ta4j.core.indicators.numeric.volume

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class RelativeVolumeStandardDeviationIndicatorTest {

    private fun createTestData(): List<CandleReceived> {
        val volumes = listOf(10.0, 11.0, 12.0, 13.0, 11.0, 10.0, 12.0, 15.0, 12.0)
        val prices = listOf(
            Triple(10.0, 9.0, 10.0), // open, close, high/low
            Triple(10.0, 11.0, 11.0),
            Triple(11.0, 12.0, 12.0),
            Triple(10.0, 12.0, 12.0),
            Triple(9.0, 12.0, 12.0),
            Triple(9.0, 8.0, 9.0),
            Triple(11.0, 8.0, 11.0),
            Triple(10.0, 13.0, 13.0),
            Triple(11.0, 2.0, 11.0)
        )

        return volumes.zip(prices).mapIndexed { index, (volume, priceData) ->
            val (open, close, high) = priceData
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong() + 1)),
                openPrice = open,
                closePrice = close,
                highPrice = high,
                lowPrice = if (index == 8) 2.0 else if (index in 4..5) 9.0 else 10.0,
                volume = volume
            )
        }
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `given bar count of 2 when get value then return correct value`(numFactory: NumFactory) {
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, 2)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(createTestData())

        context.fastForwardUntilStable()

        context.assertNext(0.70710678)
        context.assertNext(0.70710678)
        context.assertNext(-0.70710678)
        context.assertNext(-0.70710678)
        context.assertNext(0.70710678)
        context.assertNext(0.70710678)
        context.assertNext(-0.70710678)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `given bar count of 3 when get value then return correct value`(numFactory: NumFactory) {
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(createTestData())


        context.fastForwardUntilStable()

        context.assertNext(1.0)
        context.assertNext(-1.0)
        context.assertNext(-0.872871)
        context.assertNext(1.0)
        context.assertNext(1.05962)
        context.assertNext(-0.577350)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant volume correctly`(numFactory: NumFactory) {
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, 3)

        val marketEvents = (0..7).map { index ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong() + 1)),
                openPrice = 100.0,
                highPrice = 100.0,
                lowPrice = 100.0,
                closePrice = 100.0,
                volume = 1000.0 // Constant volume
            )
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(marketEvents)

        context.fastForwardUntilStable()
        // When all volumes are the same, standard deviation is 0
        // The indicator should return 0 to avoid division by zero
        context.assertNext(0.0) // Bar 0
        context.assertNext(0.0) // Bar 1
        context.assertNext(0.0) // Bar 2: constant volume should result in 0
        context.assertNext(0.0) // Bar 3: constant volume should result in 0
        context.assertNext(0.0) // Bar 4: constant volume should result in 0
    }

    @Test
    fun `should require positive bar count`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            RelativeVolumeStandardDeviationIndicator(numFactory, 0)
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            RelativeVolumeStandardDeviationIndicator(numFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be unstable until enough data is available`(numFactory: NumFactory) {
        val barCount = 5
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, barCount)

        val marketEvents = (0..5).map { index ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong() + 1)),
                openPrice = 100.0, highPrice = 100.0, lowPrice = 100.0, closePrice = 100.0,
                volume = (index + 1) * 1000.0
            )
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(marketEvents)

        // Check that indicator is not stable until we have enough data
        repeat(barCount - 1) {
            context.advance()
            assertThat(indicator.isStable).isFalse()
        }

        // After barCount periods, should be stable
        context.advance()
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate lag correctly`(numFactory: NumFactory) {
        val barCount = 10
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, barCount)

        assertThat(indicator.lag).isEqualTo(barCount)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero volume correctly`(numFactory: NumFactory) {
        val indicator = RelativeVolumeStandardDeviationIndicator(numFactory, 3)

        val marketEvents = (0..10).map { index ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong() + 1)),
                openPrice = 100.0,
                highPrice = 100.0,
                lowPrice = 100.0,
                closePrice = 100.0,
                volume = if (index == 2) 0.0 else 1000.0
            )
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(marketEvents)

        // The indicator should handle zero volume without crashing
        context.fastForwardUntilStable()
            .assertNext(0.5773502691896258)
            .assertNext(0.5773502691896258)
        context.assertNext(0.0)
        context.assertNext(0.0)
    }
}
