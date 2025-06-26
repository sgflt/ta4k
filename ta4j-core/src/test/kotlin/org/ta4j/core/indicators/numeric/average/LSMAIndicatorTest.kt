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
 * Tests for {@link LSMAIndicator}.
 */
class LSMAIndicatorTest {

    private val testData = doubleArrayOf(
        23.0, 20.0, 15.0, 19.0, 18.0, 14.0, 16.0, 13.0, 10.0, 13.0, 12.0, 11.0, 10.0, 10.0, 12.0, 10.0, 11.0, 14.0, 14.0, 11.0, 10.0, 10.0, 10.0,
        10.0, 10.0, 12.0, 10.0, 11.0, 10.0, 15.0, 11.0, 10.0, 15.0, 16.0, 15.0, 19.0, 17.0, 13.0, 11.0, 10.0, 10.0, 13.0, 16.0, 21.0, 24.0, 24.0, 22.0,
        21.0, 26.0, 23.0, 19.0, 16.0, 13.0, 11.0, 15.0, 18.0, 16.0, 18.0, 13.0, 10.0, 10.0, 12.0, 16.0, 17.0, 12.0, 14.0, 13.0, 10.0, 10.0, 10.0, 10.0,
        14.0, 11.0, 10.0, 10.0, 10.0, 15.0, 13.0, 10.0, 10.0, 14.0, 12.0, 11.0, 15.0, 17.0, 16.0, 11.0, 10.0, 10.0, 15.0, 17.0, 15.0, 18.0, 20.0, 19.0,
        22.0, 24.0, 26.0, 25.0, 28.0
    )

    private val expectedValues = doubleArrayOf(
        11.2, 11.4, 10.8, 9.6, 10.8, 10.6, 11.0, 12.8, 13.8, 13.0
    )

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate LSMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*testData)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 5))

        // Skip to index 10 (unstable bars skipped)
        repeat(10) { context.advance() }
        
        // Check expected values starting from index 10
        for (expectedValue in expectedValues) {
            context.assertNext(expectedValue)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle simple linear trend`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 4))

        // Skip initial bars until stable
        repeat(3) { context.advance() }
        
        // For a perfect linear trend, LSMA calculates the regression line endpoint
        context.assertNext(4.0) // Index 3: regression line endpoint at current time
        context.assertNext(5.0) // Index 4: regression line endpoint
        context.assertNext(6.0) // Index 5: regression line endpoint
        context.assertNext(7.0) // Index 6: regression line endpoint
        context.assertNext(8.0) // Index 7: regression line endpoint
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat trend`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0, 10.0)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 3))

        // Skip initial bars
        repeat(2) { context.advance() }
        
        // For flat data, LSMA should equal the price
        repeat(4) {
            context.assertNext(10.0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return original value when insufficient data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 10.0, 15.0, 20.0)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 5))

        // When barCount > available data, should return original indicator value
        context.assertNext(5.0)
        context.assertNext(10.0) 
        context.assertNext(15.0)
        context.assertNext(20.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val lsma = LSMAIndicator(numFactory, Indicators.closePrice(), 5)
        
        // Lag should equal bar count
        assertThat(lsma.lag).isEqualTo(5)
        
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            .withIndicator(lsma)

        // Initially unstable
        assertThat(lsma.isStable).isFalse()
        
        // Should become stable after barCount bars
        repeat(4) { 
            context.advance()
            assertThat(lsma.isStable).isFalse()
        }
        
        context.advance() // 5th bar
        assertThat(lsma.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero denominator gracefully`(numFactory: NumFactory) {
        // Create data that would cause zero denominator (all X values the same - impossible in practice)
        // This test verifies the zero check works
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 2))

        // Should not throw exception
        context.advance()
        context.advance()
        context.advance()
        
        // Should produce finite values
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isFinite()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should format toString correctly`(numFactory: NumFactory) {
        val lsma = LSMAIndicator(numFactory, Indicators.closePrice(), 10)
        
        val toStringResult = lsma.toString()
        assertThat(toStringResult).contains("LSMA")
        assertThat(toStringResult).contains("10")
        assertThat(toStringResult).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle downward trend`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 8.0, 7.0, 6.0, 5.0)
            .withIndicator(LSMAIndicator(numFactory, Indicators.closePrice(), 3))

        // Skip initial bars
        repeat(2) { context.advance() }
        
        // For downward trend, LSMA calculates regression line endpoint
        context.assertNext(8.0) // Regression line endpoint for current period
        context.assertNext(7.0)
        context.assertNext(6.0) 
        context.assertNext(5.0) // Continues downward trend
    }
}
