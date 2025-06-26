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
 * Tests for [SMMAIndicator].
 */
class SMMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate SMMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0)
            .withIndicator(SMMAIndicator(numFactory, Indicators.closePrice(), 4))

        // First value is just the first price
        context.assertNext(10.0)
        
        // Second value: (10 + 20) / 2 = 15.0
        context.assertNext(15.0)
        
        // Third value: (10 + 20 + 30) / 3 = 20.0
        context.assertNext(20.0)
        
        // Fourth value (first real SMMA): SMA(4) = (10 + 20 + 30 + 40) / 4 = 25.0
        context.assertNext(25.0)
        
        // Fifth value: SMMA = (25 * 3 + 50) / 4 = 125 / 4 = 31.25
        context.assertNext(31.25)
        
        // Sixth value: SMMA = (31.25 * 3 + 60) / 4 = 153.75 / 4 = 38.4375
        context.assertNext(38.4375)
        
        // Seventh value: SMMA = (38.4375 * 3 + 70) / 4 = 185.3125 / 4 = 46.328125
        context.assertNext(46.328125)
        
        // Eighth value: SMMA = (46.328125 * 3 + 80) / 4 = 218.984375 / 4 = 54.74609375
        context.assertNext(54.74609375)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 50.0, 50.0, 50.0, 50.0, 50.0)
            .withIndicator(SMMAIndicator(numFactory, Indicators.closePrice(), 3))

        // All values should be 50.0 for flat data
        repeat(6) {
            context.assertNext(50.0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)
            .withIndicator(SMMAIndicator(numFactory, Indicators.closePrice(), 1))

        // With barCount=1, SMMA should equal current price
        context.assertNext(10.0)
        context.assertNext(20.0)
        context.assertNext(30.0)
        context.assertNext(40.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val smma = SMMAIndicator(numFactory, Indicators.closePrice(), 5)
        
        // Lag should equal bar count
        assertThat(smma.lag).isEqualTo(5)
        
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            .withIndicator(smma)

        // Initially unstable
        assertThat(smma.isStable).isFalse()
        
        // Should become stable after barCount bars
        repeat(4) { 
            context.advance()
            assertThat(smma.isStable).isFalse()
        }
        
        context.advance() // 5th bar
        assertThat(smma.isStable).isTrue()
        
        context.advance() // 6th bar - should remain stable
        assertThat(smma.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should format toString correctly`(numFactory: NumFactory) {
        val smma = SMMAIndicator(numFactory, Indicators.closePrice(), 10)
        
        val toStringResult = smma.toString()
        assertThat(toStringResult).contains("SMMAIndicator")
        assertThat(toStringResult).contains("barCount: 10")
        assertThat(toStringResult).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should converge to trend over time`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0, 135.0, 140.0)
            .withIndicator(SMMAIndicator(numFactory, Indicators.closePrice(), 3))

        // Skip initial values
        repeat(3) { context.advance() }
        
        // SMMA should gradually increase and lag behind the actual prices
        // Fourth value: SMMA = (105 * 2 + 115) / 3 = 325 / 3 = 108.333...
        context.assertNext(108.333333333333333)
        // Fifth value: SMMA = (108.333... * 2 + 120) / 3 = 336.666... / 3 = 112.222...
        context.assertNext(112.222222222222222)
        // Sixth value: SMMA = (112.222... * 2 + 125) / 3 = 349.444... / 3 = 116.481...
        context.assertNext(116.481481481481481)
        // Seventh value: SMMA = (116.481... * 2 + 130) / 3 = 362.962... / 3 = 120.987...
        context.assertNext(120.987654320987654)
        // Eighth value: SMMA = (120.987... * 2 + 135) / 3 = 376.975... / 3 = 125.658...
        context.assertNext(125.658436213991769)
        // Ninth value: SMMA = (125.658... * 2 + 140) / 3 = 391.316... / 3 = 130.438...
        context.assertNext(130.438957475994514)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large bar count`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0)
            .withIndicator(SMMAIndicator(numFactory, Indicators.closePrice(), 10))

        // Skip to 10th bar (first stable SMMA)
        repeat(9) { context.advance() }
        
        // 10th bar: SMA(10) = (10+11+12+13+14+15+16+17+18+19) / 10 = 145 / 10 = 14.5
        context.assertNext(14.5)
        
        // 11th bar: SMMA = (14.5 * 9 + 20) / 10 = 150.5 / 10 = 15.05
        context.assertNext(15.05)
    }
}