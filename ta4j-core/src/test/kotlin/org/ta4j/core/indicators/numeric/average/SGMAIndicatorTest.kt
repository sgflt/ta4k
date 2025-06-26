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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

/**
 * Tests for [SGMAIndicator].
 */
class SGMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate SGMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 25.0, 15.0, 20.0, 35.0)
            .withIndicator(SGMAIndicator(numFactory, Indicators.closePrice(), 5, 2))

        // SGMA with window 5 should give more weight to center values
        // First value: only one data point
        context.assertNext(10.0)
        
        // Values should be smoothed but preserve trends
        context.advance() // 20.0
        context.advance() // 30.0
        context.advance() // 25.0
        context.advance() // 15.0 - now we have 5 values: [10, 20, 30, 25, 15]
        
        // The weighted average should give more weight to center (25)
        // With our quadratic weight function, center gets most weight
        context.assertNext(23.0) // [20, 30, 25, 15, 20] - weighted average
        context.assertNext(21.333333333333332) // [30, 25, 15, 20, 35] - weighted average
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 50.0, 50.0, 50.0, 50.0, 50.0)
            .withIndicator(SGMAIndicator(numFactory, Indicators.closePrice(), 3))

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
            .withIndicator(SGMAIndicator(numFactory, Indicators.closePrice(), 1, 0))

        // With barCount=1, SGMA should equal current price
        context.assertNext(10.0)
        context.assertNext(20.0)
        context.assertNext(30.0)
        context.assertNext(40.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val sgma = SGMAIndicator(numFactory, Indicators.closePrice(), 7)
        
        // Lag should equal bar count
        assertThat(sgma.lag).isEqualTo(7)
        
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
            .withIndicator(sgma)

        // Initially unstable
        assertThat(sgma.isStable).isFalse()
        
        // Should become stable after barCount bars
        repeat(6) { 
            context.advance()
            assertThat(sgma.isStable).isFalse()
        }
        
        context.advance() // 7th bar
        assertThat(sgma.isStable).isTrue()
        
        context.advance() // 8th bar - should remain stable
        assertThat(sgma.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should format toString correctly`(numFactory: NumFactory) {
        val sgma = SGMAIndicator(numFactory, Indicators.closePrice(), 5, 3)
        
        val toStringResult = sgma.toString()
        assertThat(toStringResult).contains("SGMA")
        assertThat(toStringResult).contains("barCount: 5")
        assertThat(toStringResult).contains("polynomialOrder: 3")
        assertThat(toStringResult).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should throw exception for even window size`(numFactory: NumFactory) {
        assertThatThrownBy {
            SGMAIndicator(numFactory, Indicators.closePrice(), 4, 2)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Window size must be odd.")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should throw exception for polynomial order greater than window`(numFactory: NumFactory) {
        assertThatThrownBy {
            SGMAIndicator(numFactory, Indicators.closePrice(), 3, 3)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Polynomial order must be less than window size.")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should smooth noisy data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 98.0, 103.0, 97.0, 104.0, 96.0)
            .withIndicator(SGMAIndicator(numFactory, Indicators.closePrice(), 5))

        // SGMA should smooth out the noise
        context.assertNext(100.0)
        context.assertNext(101.111111111111111) // Weighted average
        context.assertNext(100.153846153846154)  // Balanced around 100
        context.assertNext(100.714285714285714)
        context.assertNext(100.466666666666666)  // Should stay around 100 with noise filtered
        context.assertNext(100.066666666666666)
        context.assertNext(100.466666666666666)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should preserve trend`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 115.0, 120.0, 125.0)
            .withIndicator(SGMAIndicator(numFactory, Indicators.closePrice(), 3))

        // SGMA should preserve the upward trend
        context.assertNext(100.0)
        context.assertNext(103.333333333333333) // Weighted average
        context.assertNext(105.0)  // Weighted average
        context.assertNext(110.0)
        context.assertNext(115.0)
        context.assertNext(120.0)
    }
}
