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
package org.ta4j.core.indicators.numeric.average

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

/**
 * Tests for [VWMAIndicator].
 */
class VWMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate VWMA correctly with known values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        // Manually create market events with prices and volumes
        val marketEvents = mutableListOf<org.ta4j.core.events.MarketEvent>()
        val prices = doubleArrayOf(
            101.0,
            100.0,
            104.0,
            102.0,
            100.0,
            99.0,
            98.0,
            100.0,
            100.0,
            102.0,
            100.0,
            97.0,
            102.0,
            97.0,
            104.0,
            99.0,
            102.0,
            98.0,
            100.0,
            101.0,
            102.0,
            99.0,
            99.0,
            101.0,
            96.0,
            98.0,
            99.0,
            99.0,
            100.0,
            103.0,
            100.0,
            100.0,
            102.0,
            99.0,
            98.0,
            104.0,
            100.0,
            101.0,
            99.0,
            102.0,
            100.0,
            96.0,
            102.0,
            100.0,
            102.0,
            102.0,
            98.0,
            102.0,
            97.0,
            102.0,
            101.0
        )
        val volumes = doubleArrayOf(
            1847.0,
            1290.0,
            1856.0,
            1993.0,
            1942.0,
            1893.0,
            1813.0,
            1024.0,
            1289.0,
            1006.0,
            1992.0,
            1180.0,
            1268.0,
            1934.0,
            1585.0,
            1727.0,
            1884.0,
            1134.0,
            1701.0,
            1134.0,
            1675.0,
            1166.0,
            1220.0,
            1854.0,
            1779.0,
            1970.0,
            1579.0,
            1515.0,
            1732.0,
            1234.0,
            1651.0,
            1457.0,
            1990.0,
            1707.0,
            1937.0,
            1537.0,
            1864.0,
            1616.0,
            1712.0,
            1906.0,
            1287.0,
            1518.0,
            1362.0,
            1104.0,
            1948.0,
            1064.0,
            1108.0,
            1933.0,
            1717.0,
            1808.0,
            1623.0
        )

        for (i in prices.indices) {
            marketEvents.add(
                org.ta4j.core.events.CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.EPOCH.plus(java.time.Duration.ofDays(i.toLong())),
                    java.time.Instant.EPOCH.plus(java.time.Duration.ofDays(i.toLong() + 1)),
                    prices[i], prices[i], prices[i], prices[i], volumes[i]
                )
            )
        }

        context.withMarketEvents(marketEvents)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 5))

        // Skip to index 10 and test against expected values
        repeat(10) { context.advance() }

        val expectedValues = doubleArrayOf(
            99.77344188658058, 99.76459713449391, 100.14966592427618, 99.35040650406505,
            99.94144993089584, 99.71497270600467, 100.60907358894976, 100.03763310745403,
            100.76117544515004, 100.11965699208443, 100.79489904357067, 100.15418502202643,
            100.3042343387471, 100.56064690026955
        )

        for (expectedValue in expectedValues) {
            context.assertNext(expectedValue)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero volume gracefully`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 2))

        // When volume is 1.0 (default), VWMA should equal simple MA
        context.assertNext(10.0)
        context.assertNext(15.0) // (10+20)/2
        context.assertNext(25.0) // (20+30)/2
        context.assertNext(35.0) // (30+40)/2
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle different volume weights`(numFactory: NumFactory) {
        val marketEvents = mutableListOf<org.ta4j.core.events.MarketEvent>()
        val prices = doubleArrayOf(10.0, 20.0, 30.0)
        val volumes = doubleArrayOf(100.0, 200.0, 300.0)

        for (i in prices.indices) {
            marketEvents.add(
                org.ta4j.core.events.CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.EPOCH.plus(java.time.Duration.ofDays(i.toLong())),
                    java.time.Instant.EPOCH.plus(java.time.Duration.ofDays(i.toLong() + 1)),
                    prices[i], prices[i], prices[i], prices[i], volumes[i]
                )
            )
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 2))

        context.assertNext(10.0) // First bar: 10.0
        context.assertNext(16.666666666666668) // (10*100 + 20*200) / (100+200) = 5000/300 = 16.67
        context.assertNext(26.0) // (20*200 + 30*300) / (200+300) = 13000/500 = 26.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate simple VWMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 2))

        context.assertNext(10.0) // First bar: 10.0
        context.assertNext(15.0) // Equal weights: (10 + 20) / 2 = 15.0  
        context.assertNext(25.0) // Equal weights: (20 + 30) / 2 = 25.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        val vwma = VWMAIndicator(numFactory, Indicators.closePrice(), 5)

        // Lag should equal bar count
        assertThat(vwma.lag).isEqualTo(5)

        context.withIndicator(vwma)

        // Initially unstable
        assertThat(vwma.isStable).isFalse()

        // Should become stable after barCount bars
        repeat(4) {
            context.advance()
            assertThat(vwma.isStable).isFalse()
        }

        context.advance() // 5th bar
        assertThat(vwma.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should format toString correctly`(numFactory: NumFactory) {
        val vwma = VWMAIndicator(numFactory, Indicators.closePrice(), 10)

        val toStringResult = vwma.toString()
        assertThat(toStringResult).contains("VWMA")
        assertThat(toStringResult).contains("10")
        assertThat(toStringResult).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.0, 25.0, 35.0)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 1))

        // With barCount=1, VWMA should equal current price
        context.assertNext(15.0)
        context.assertNext(25.0)
        context.assertNext(35.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle equal weights scenario`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)
            .withIndicator(VWMAIndicator(numFactory, Indicators.closePrice(), 3))

        context.assertNext(10.0)
        context.assertNext(15.0) // (10+20)/2 = 15.0 
        context.assertNext(20.0) // (10+20+30)/3 = 20.0
        context.assertNext(30.0) // (20+30+40)/3 = 30.0
    }
}
