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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class AccumulationDistributionIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate accumulation distribution correctly`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // Bar 1: Close=10, High=12, Low=8, Volume=200
            // Money Flow Multiplier = ((10-8) - (12-10)) / (12-8) = 0/4 = 0
            // AD = 0 * 200 = 0
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plusSeconds(86400),
                openPrice = 10.0,
                highPrice = 12.0,
                lowPrice = 8.0,
                closePrice = 10.0,
                volume = 200.0
            ),
            // Bar 2: Close=8, High=10, Low=7, Volume=100
            // Money Flow Multiplier = ((8-7) - (10-8)) / (10-7) = -1/3
            // AD = 0 + (-1/3 * 100) = -100/3
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plusSeconds(86400),
                endTime = Instant.EPOCH.plusSeconds(172800),
                openPrice = 8.0,
                highPrice = 10.0,
                lowPrice = 7.0,
                closePrice = 8.0,
                volume = 100.0
            ),
            // Bar 3: Close=9, High=15, Low=6, Volume=300
            // Money Flow Multiplier = ((9-6) - (15-9)) / (15-6) = -3/9 = -1/3
            // AD = -100/3 + (-1/3 * 300) = -100/3 - 100 = -400/3
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plusSeconds(172800),
                endTime = Instant.EPOCH.plusSeconds(259200),
                openPrice = 9.0,
                highPrice = 15.0,
                lowPrice = 6.0,
                closePrice = 9.0,
                volume = 300.0
            ),
            // Bar 4: Close=20, High=40, Low=5, Volume=50
            // Money Flow Multiplier = ((20-5) - (40-20)) / (40-5) = -5/35 = -1/7
            // AD = -400/3 + (-1/7 * 50) = -400/3 - 250/35
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plusSeconds(259200),
                endTime = Instant.EPOCH.plusSeconds(345600),
                openPrice = 20.0,
                highPrice = 40.0,
                lowPrice = 5.0,
                closePrice = 20.0,
                volume = 50.0
            ),
            // Bar 5: Close=30, High=30, Low=3, Volume=600
            // Money Flow Multiplier = ((30-3) - (30-30)) / (30-3) = 27/27 = 1
            // AD = previous + (1 * 600) = previous + 600
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plusSeconds(345600),
                endTime = Instant.EPOCH.plusSeconds(432000),
                openPrice = 30.0,
                highPrice = 30.0,
                lowPrice = 3.0,
                closePrice = 30.0,
                volume = 600.0
            )
        )

        val accumulationDistribution = AccumulationDistributionIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(accumulationDistribution)

        // Test each bar's AD value
        context.assertNext(0.0) // Bar 1: AD = 0

        val expectedBar2 = -100.0 / 3.0
        context.assertNext(expectedBar2) // Bar 2: AD = -100/3

        val expectedBar3 = expectedBar2 - 100.0 // Bar 3: AD = -100/3 - 100
        context.assertNext(expectedBar3)

        val expectedBar4 = expectedBar3 + (-250.0 / 35.0) // Bar 4: AD = previous - 250/35
        context.assertNext(expectedBar4)

        val expectedBar5 = expectedBar4 + 600.0 // Bar 5: AD = previous + 600
        context.assertNext(expectedBar5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero range correctly`(numFactory: NumFactory) {
        val marketEvents = listOf(
            // Bar with High = Low (zero range)
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plusSeconds(86400),
                openPrice = 10.0,
                highPrice = 10.0,
                lowPrice = 10.0,
                closePrice = 10.0,
                volume = 100.0
            )
        )

        val accumulationDistribution = AccumulationDistributionIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(accumulationDistribution)

        // When high = low, money flow multiplier should be 0
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero volume correctly`(numFactory: NumFactory) {
        val marketEvents = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plusSeconds(86400),
                openPrice = 8.0,
                highPrice = 12.0,
                lowPrice = 6.0,
                closePrice = 10.0,
                volume = 0.0 // Zero volume
            )
        )

        val accumulationDistribution = AccumulationDistributionIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(accumulationDistribution)

        // Zero volume should result in zero accumulation/distribution
        context.assertNext(0.0)
    }
}
