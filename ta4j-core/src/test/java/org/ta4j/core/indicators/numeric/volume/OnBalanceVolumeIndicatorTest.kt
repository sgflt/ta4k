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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class OnBalanceVolumeIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `initial value should be zero`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `rising prices should add volume to OBV`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // First candle: price 100, volume 1000 - OBV starts at 0
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 100.0,
                highPrice = 105.0,
                lowPrice = 95.0,
                closePrice = 100.0,
                volume = 1000.0
            ),
            // Second candle: price rises to 110, volume 2000 - should add 2000 to OBV
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 105.0,
                highPrice = 115.0,
                lowPrice = 108.0,
                closePrice = 110.0,
                volume = 2000.0
            ),
            // Third candle: price rises to 120, volume 1500 - should add 1500 to OBV
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 112.0,
                highPrice = 125.0,
                lowPrice = 110.0,
                closePrice = 120.0,
                volume = 1500.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.fastForwardUntilStable()
        context.assertNext(0.0)    // First candle, no previous price to compare
        context.assertNext(2000.0) // 0 + 2000
        context.assertNext(3500.0) // 2000 + 1500
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `falling prices should subtract volume from OBV`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // First candle: price 120, volume 1000
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 120.0,
                highPrice = 125.0,
                lowPrice = 115.0,
                closePrice = 120.0,
                volume = 1000.0
            ),
            // Second candle: price falls to 110, volume 2000 - should subtract 2000
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 118.0,
                highPrice = 120.0,
                lowPrice = 108.0,
                closePrice = 110.0,
                volume = 2000.0
            ),
            // Third candle: price falls to 100, volume 1500 - should subtract 1500
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 108.0,
                highPrice = 112.0,
                lowPrice = 95.0,
                closePrice = 100.0,
                volume = 1500.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.assertNext(0.0)     // First candle
        context.assertNext(-2000.0) // 0 - 2000
        context.assertNext(-3500.0) // -2000 - 1500
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `unchanged prices should not change OBV`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // First candle: price 100, volume 1000
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 98.0,
                highPrice = 102.0,
                lowPrice = 97.0,
                closePrice = 100.0,
                volume = 1000.0
            ),
            // Second candle: same price 100, volume 2000 - should not change OBV
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 99.0,
                highPrice = 103.0,
                lowPrice = 96.0,
                closePrice = 100.0,
                volume = 2000.0
            ),
            // Third candle: same price 100, volume 3000 - should not change OBV
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 98.5,
                highPrice = 101.5,
                lowPrice = 98.0,
                closePrice = 100.0,
                volume = 3000.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.assertNext(0.0) // First candle
        context.assertNext(0.0) // 0 + 0 (no change)
        context.assertNext(0.0) // 0 + 0 (no change)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `complex mixed sequence should calculate OBV correctly`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // Day 1: price 100, volume 1000 (baseline)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 98.0,
                highPrice = 102.0,
                lowPrice = 97.0,
                closePrice = 100.0,
                volume = 1000.0
            ),
            // Day 2: price rises to 105, volume 2000 (OBV = +2000)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 101.0,
                highPrice = 107.0,
                lowPrice = 100.0,
                closePrice = 105.0,
                volume = 2000.0
            ),
            // Day 3: price falls to 95, volume 1500 (OBV = 2000 - 1500 = 500)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 104.0,
                highPrice = 105.0,
                lowPrice = 93.0,
                closePrice = 95.0,
                volume = 1500.0
            ),
            // Day 4: price unchanged at 95, volume 3000 (OBV = 500 + 0 = 500)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                openPrice = 94.0,
                highPrice = 97.0,
                lowPrice = 92.0,
                closePrice = 95.0,
                volume = 3000.0
            ),
            // Day 5: price rises to 110, volume 2500 (OBV = 500 + 2500 = 3000)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(5)),
                openPrice = 96.0,
                highPrice = 112.0,
                lowPrice = 95.0,
                closePrice = 110.0,
                volume = 2500.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.assertNext(0.0)    // Day 1: baseline
        context.assertNext(2000.0) // Day 2: +2000
        context.assertNext(500.0)  // Day 3: 2000 - 1500 = 500
        context.assertNext(500.0)  // Day 4: no change
        context.assertNext(3000.0) // Day 5: 500 + 2500 = 3000
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `zero volume should not affect OBV calculation`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // First candle: price 100, volume 1000
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 98.0,
                highPrice = 102.0,
                lowPrice = 97.0,
                closePrice = 100.0,
                volume = 1000.0
            ),
            // Second candle: price rises to 110, but zero volume - should add 0
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 101.0,
                highPrice = 112.0,
                lowPrice = 100.0,
                closePrice = 110.0,
                volume = 0.0
            ),
            // Third candle: price falls to 90, zero volume - should subtract 0
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 108.0,
                highPrice = 110.0,
                lowPrice = 88.0,
                closePrice = 90.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(OnBalanceVolumeIndicator(numFactory))

        context.assertNext(0.0) // First candle
        context.assertNext(0.0) // 0 + 0 = 0
        context.assertNext(0.0) // 0 - 0 = 0
    }

    @Test
    fun `indicator should be stable from start and have zero lag`() {
        val context = MarketEventTestContext()
            .withCandlePrices(100.0)
            .withIndicator(OnBalanceVolumeIndicator(org.ta4j.core.num.DoubleNumFactory))

        val indicator = context.firstNumericIndicator!!

        assert(indicator.isStable) { "OBV should be stable from the start" }
        assert(indicator.lag == 0) { "OBV should have zero lag" }
    }
}
