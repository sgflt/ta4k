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
 * Tests for [TMAIndicator].
 */
class TMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate TMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
            .withIndicator(TMAIndicator(numFactory, Indicators.closePrice(), 3))

        // TMA requires 2 * barCount - 1 bars to be stable
        // First 3 bars: SMA1 values are 1, 1.5, 2
        // Bar 1: 1.0
        context.fastForwardUntilStable()
        
        // Bar 5: SMA1 = (3+4+5)/3 = 4, SMA2 = (2+3+4)/3 = 3
        context.assertCurrent(3.0)
        
        // Bar 6: SMA1 = (4+5+6)/3 = 5, SMA2 = (3+4+5)/3 = 4
        context.assertNext(4.0)
        
        // Bar 7: SMA1 = (5+6+7)/3 = 6, SMA2 = (4+5+6)/3 = 5
        context.assertNext(5.0)
        
        // Bar 8: SMA1 = (6+7+8)/3 = 7, SMA2 = (5+6+7)/3 = 6
        context.assertNext(6.0)
        
        // Bar 9: SMA1 = (7+8+9)/3 = 8, SMA2 = (6+7+8)/3 = 7
        context.assertNext(7.0)
        
        // Bar 10: SMA1 = (8+9+10)/3 = 9, SMA2 = (7+8+9)/3 = 8
        context.assertNext(8.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 50.0, 50.0, 50.0, 50.0, 50.0)
            .withIndicator(TMAIndicator(numFactory, Indicators.closePrice(), 2))

        // All values should converge to 50.0 for flat data
        context.fastForwardUntilStable()
        context.assertNext(50.0)
        context.assertNext(50.0)
        context.assertNext(50.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)
            .withIndicator(TMAIndicator(numFactory, Indicators.closePrice(), 1))

        // With barCount=1, TMA should equal current price (double SMA with period 1)
        context.assertNext(10.0)
        context.assertNext(20.0)
        context.assertNext(30.0)
        context.assertNext(40.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        // Lag should equal 2 * bar count (double smoothing)

        
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)
        val tma = TMAIndicator(numFactory, Indicators.closePrice(), 3)
        assertThat(tma.lag).isEqualTo(6)

        context.withIndicator(tma)

        // Initially unstable
        assertThat(tma.isStable).isFalse()
        
        // Should become stable after 2 * barCount - 1 bars
        repeat(4) { 
            context.advance()
            assertThat(tma.isStable).isFalse()
        }
        
        context.advance() // 5th bar (2 * 3 - 1 = 5)
        assertThat(tma.isStable).isTrue()
        
        context.advance() // 6th bar - should remain stable
        assertThat(tma.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should format toString correctly`(numFactory: NumFactory) {
        val tma = TMAIndicator(numFactory, Indicators.closePrice(), 5)
        
        val toStringResult = tma.toString()
        assertThat(toStringResult).contains("TMA")
        assertThat(toStringResult).contains("5")
        assertThat(toStringResult).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be smoother than single SMA`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 14.0, 7.0, 15.0, 9.0, 13.0)
            .withIndicator(TMAIndicator(numFactory, Indicators.closePrice(), 3))

        // TMA should smooth out the price volatility
        context.fastForwardUntilStable()
        // Bar 5: SMA1=(8+14+7)/3=9.666..., SMA2=(10+11.333+9.666)/3=10.333...
        context.assertCurrent(10.333333333333334)
        // Bar 6: SMA1=(14+7+15)/3=12, SMA2=(11.333+9.666+12)/3=11
        context.assertNext(11.0)
        // Bar 7: SMA1=(7+15+9)/3=10.333, SMA2=(9.666+12+10.333)/3=10.666...
        context.assertNext(10.666666666666666)
        // Bar 8: SMA1=(15+9+13)/3=12.333, SMA2=(12+10.333+12.333)/3=11.555...
        context.assertNext(11.555555555555555)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle trend data correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 104.0, 106.0, 108.0, 110.0)
            .withIndicator(TMAIndicator(numFactory, Indicators.closePrice(), 2))

        // TMA should follow the trend but with lag
        context.fastForwardUntilStable()
        context.assertCurrent(102.0)
        context.assertNext(104.0) // SMA1=(104+106)/2=105, SMA2=(103+105)/2=104
        context.assertNext(106.0) // SMA1=(106+108)/2=107, SMA2=(105+107)/2=106
        context.assertNext(108.0) // SMA1=(108+110)/2=109, SMA2=(107+109)/2=108
    }
}
