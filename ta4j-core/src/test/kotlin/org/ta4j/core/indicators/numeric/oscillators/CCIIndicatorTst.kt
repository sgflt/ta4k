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
package org.ta4j.core.indicators.numeric.oscillators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

class CCIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CCI correctly with simple data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 3))

        // Test data: Create bars with H=L=C to get specific typical prices
        // Bar 1: TP = 10, Bar 2: TP = 11, Bar 3: TP = 12
        // SMA after 3 bars = (10 + 11 + 12) / 3 = 11
        // Mean Deviation = (|10-11| + |11-11| + |12-11|) / 3 = 2/3 ≈ 0.6667
        // CCI = (12 - 11) / (0.015 * 0.6667) = 1 / 0.01 = 100
        context.withCandlePrices(10.0, 11.0, 12.0)
            .fastForward(2) // First two bars to build up data
            .assertNext(100.0) // Third bar should give CCI = 100
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero mean deviation gracefully`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 2))

        // Using identical typical prices to create zero mean deviation
        context.withCandlePrices(10.0, 10.0, 10.0)
            .fastForward(1) // First bar
            .assertNext(0.0) // Second bar: zero mean deviation should result in CCI = 0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CCI with custom bar count`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 5))

        context.withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0)
            .fastForwardUntilStable() // Fill the 5-period window
            .assertNext(111.1111) // Expected CCI for the 6th bar
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be unstable until enough bars are processed`(numFactory: NumFactory) {
        val indicator = CCIIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)

        assertThat(indicator.isStable).isFalse()

        context.withCandlePrices(10.0, 11.0, 12.0)
            .fastForward(2) // Process 2 bars

        assertThat(indicator.isStable).isFalse()

        context.advance() // Process 3rd bar

        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag equal to bar count`(numFactory: NumFactory) {
        val indicator3 = CCIIndicator(numFactory, 3)
        val indicator20 = CCIIndicator(numFactory, 20)

        assertThat(indicator3.lag).isEqualTo(3)
        assertThat(indicator20.lag).isEqualTo(20)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate negative CCI when price is below average`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 3))

        // Descending prices: typical prices will be 15, 14, 13
        // SMA = (15 + 14 + 13) / 3 = 14
        // Mean Deviation = (|15-14| + |14-14| + |13-14|) / 3 = 2/3
        // CCI = (13 - 14) / (0.015 * 2/3) = -1 / 0.01 = -100
        context.withCandlePrices(15.0, 14.0, 13.0)
            .fastForward(2)
            .assertNext(-100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle varying price movements`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 4))

        context.withCandlePrices(10.0, 12.0, 8.0, 15.0, 11.0)
            .fastForward(3) // Fill 4-period window
            .fastForwardUntilStable()

        // Should have some CCI value calculated without throwing exceptions
        assertThat(context.firstNumericIndicator?.value?.isNaN).isFalse()
    }

    @Test
    fun `should throw exception for non-positive bar count`() {
        val numFactory = DoubleNumFactory

        assertThrows<IllegalArgumentException> {
            CCIIndicator(numFactory, 0)
        }

        assertThrows<IllegalArgumentException> {
            CCIIndicator(numFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce consistent results with typical CCI range`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 20))

        // Use realistic price data that should produce CCI values in typical range
        val prices = doubleArrayOf(
            50.0, 51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0,
            60.0, 59.0, 58.0, 57.0, 56.0, 55.0, 54.0, 53.0, 52.0, 51.0,
            50.0, 49.0, 48.0
        )

        context.withCandlePrices(*prices)
            .fastForward(19) // Fill 20-period window
            .fastForwardUntilStable()

        val cci = context.firstNumericIndicator?.value?.doubleValue() ?: 0.0

        // CCI typically ranges from -200 to +200, with most values between -100 and +100
        assertThat(cci).isBetween(-300.0, 300.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CCI with precise decimal values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 2))

        // Test with precise calculations
        // Bar 1: TP = 10.0, Bar 2: TP = 12.0
        // SMA = (10 + 12) / 2 = 11
        // Mean Deviation = (|10-11| + |12-11|) / 2 = 2/2 = 1
        // CCI = (12 - 11) / (0.015 * 1) = 1 / 0.015 = 66.666...
        context.withCandlePrices(10.0, 12.0)
            .fastForward(1)
            .assertNext(66.6666)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should match original CCI calculation with historical data`(numFactory: NumFactory) {
        val typicalPrices = doubleArrayOf(
            23.98, 23.92, 23.79, 23.67, 23.54, 23.36, 23.65, 23.72, 24.16,
            23.91, 23.81, 23.92, 23.74, 24.68, 24.94, 24.93, 25.10, 25.12,
            25.20, 25.06, 24.50, 24.31, 24.57, 24.62, 24.49, 24.37, 24.41,
            24.35, 23.75, 24.09
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(CCIIndicator(numFactory, 20))
            .withCandlePrices(*typicalPrices)

        context.fastForwardUntilStable()
            .assertCurrent(101.9185) // Index 19
            .assertNext(31.1946) // Index 20
            .assertNext(6.5578) // Index 21
            .assertNext(33.6078) // Index 22
            .assertNext(34.9686) // Index 23
            .assertNext(13.6027) // Index 24
            .assertNext(-10.6789) // Index 25
            .assertNext(-11.471) // Index 26
            .assertNext(-29.2567) // Index 27
            .assertNext(-128.6) // Index 28
            .assertNext(-72.7273) // Index 29
    }
}
