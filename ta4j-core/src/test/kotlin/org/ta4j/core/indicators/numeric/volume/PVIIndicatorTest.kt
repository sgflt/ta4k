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
import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class PVIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should start with 1000 on first bar`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        // First bar
        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    openPrice = 100.0,
                    highPrice = 105.0,
                    lowPrice = 95.0,
                    closePrice = 102.0,
                    volume = 1000.0
                )
            )
        )

        context.assertNext(1000.0)
        assertUnstable(pvi)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should update PVI when volume increases`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        // Create market events with increasing volume and prices
        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    openPrice = 100.0,
                    highPrice = 105.0,
                    lowPrice = 95.0,
                    closePrice = 100.0,
                    volume = 1000.0
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    openPrice = 100.0,
                    highPrice = 110.0,
                    lowPrice = 95.0,
                    closePrice = 110.0, // 10% increase
                    volume = 1500.0 // Volume increased
                )
            )
        )

        // First bar: PVI = 1000
        context.assertNext(1000.0)

        // Second bar: Volume increased, price increased 10%
        // PVI = 1000 + (0.10 * 1000) = 1100
        context.assertNext(1100.0)
        assertStable(pvi)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not update PVI when volume decreases or stays same`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    closePrice = 100.0,
                    volume = 1000.0
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    closePrice = 120.0, // 20% increase
                    volume = 800.0 // Volume decreased
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                    closePrice = 130.0, // Another increase
                    volume = 800.0 // Volume same
                )
            )
        )

        context.assertNext(1000.0) // First bar
        context.assertNext(1000.0) // Volume decreased, PVI unchanged
        context.assertNext(1000.0) // Volume same, PVI unchanged
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle price decrease with volume increase`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    closePrice = 100.0,
                    volume = 1000.0
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    closePrice = 90.0, // 10% decrease
                    volume = 1500.0 // Volume increased
                )
            )
        )

        context.assertNext(1000.0) // First bar

        // Second bar: Volume increased, price decreased 10%
        // PVI = 1000 + (-0.10 * 1000) = 900
        context.assertNext(900.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should accumulate changes correctly over multiple bars`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    closePrice = 100.0,
                    volume = 1000.0
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    closePrice = 110.0, // 10% increase
                    volume = 1200.0 // Volume increased
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(3, ChronoUnit.DAYS),
                    closePrice = 115.0, // ~4.55% increase from 110
                    volume = 1300.0 // Volume increased again
                )
            )
        )

        context.assertNext(1000.0) // First bar: 1000
        context.assertNext(1100.0) // Second bar: 1000 * (1 + 0.10) = 1100

        // Third bar: 1100 * (1 + (115-110)/110) = 1100 * (1 + 0.04545...) ≈ 1150
        context.assertNext(1150.0)
    }

    @Test
    fun `should handle zero volume edge case`() {
        val context = MarketEventTestContext()

        val pvi = PVIIndicator(context.barSeries.numFactory)
        context.withIndicator(pvi)

        context.withMarketEvents(
            listOf(
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH,
                    endTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    closePrice = 100.0,
                    volume = 0.0
                ),
                CandleReceived(
                    timeFrame = TimeFrame.DAY,
                    beginTime = Instant.EPOCH.plus(1, ChronoUnit.DAYS),
                    endTime = Instant.EPOCH.plus(2, ChronoUnit.DAYS),
                    closePrice = 110.0,
                    volume = 100.0 // Volume increased from 0
                )
            )
        )

        context.assertNext(1000.0) // First bar
        context.assertNext(1100.0) // Volume increased, price up 10%
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should match original test case values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pvi = PVIIndicator(numFactory)
        context.withIndicator(pvi)

        // Original test data with specific close prices and volumes
        val marketData = listOf(
            Pair(1355.69, 2739.55),
            Pair(1325.51, 3119.46), // Volume increased, price decreased
            Pair(1335.02, 3466.88), // Volume increased, price increased
            Pair(1313.72, 2577.12), // Volume decreased, price decreased
            Pair(1319.99, 2480.45), // Volume decreased, price increased
            Pair(1331.85, 2329.79), // Volume decreased, price increased
            Pair(1329.04, 2793.07), // Volume increased, price decreased
            Pair(1362.16, 3378.78), // Volume increased, price increased
            Pair(1365.51, 2417.59), // Volume decreased, price increased
            Pair(1374.02, 1442.81)  // Volume decreased, price increased
        )

        val events = marketData.mapIndexed { index, (closePrice, volume) ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(index.toLong(), ChronoUnit.DAYS),
                endTime = Instant.EPOCH.plus((index + 1).toLong(), ChronoUnit.DAYS),
                openPrice = closePrice,
                highPrice = closePrice,
                lowPrice = closePrice,
                closePrice = closePrice,
                volume = volume
            )
        }

        context.withMarketEvents(events)

        // Test each expected value from the original test
        context.assertNext(1000.0)      // Index 0: Starting value
        context.assertNext(977.7383)    // Index 1: Volume increased, price down
        context.assertNext(984.7532)    // Index 2: Volume increased, price up
        context.assertNext(984.7532)    // Index 3: Volume decreased, PVI unchanged
        context.assertNext(984.7532)    // Index 4: Volume decreased, PVI unchanged
        context.assertNext(984.7532)    // Index 5: Volume decreased, PVI unchanged
        context.assertNext(982.6755)    // Index 6: Volume increased, price down
        context.assertNext(1007.164)    // Index 7: Volume increased, price up
        context.assertNext(1007.164)    // Index 8: Volume decreased, PVI unchanged
        context.assertNext(1007.164)    // Index 9: Volume decreased, PVI unchanged
    }
}
