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
package org.ta4j.core.indicators.numeric.channels.donchian

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.num.NumFactory

class DonchianChannelUpperIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should require positive bar count`(numFactory: NumFactory) {
        assertThrows<IllegalArgumentException> {
            DonchianChannelUpperIndicator(numFactory, 0)
        }

        assertThrows<NegativeArraySizeException> {
            DonchianChannelUpperIndicator(numFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate highest high over specified period with bar count 1`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 9.0, 14.0, 11.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 1)
        context.withIndicator(indicator)

        // With bar count 1, it should return the high of the current bar
        context.assertNext(10.0)  // First bar high: 10.0
        context.assertNext(12.0)  // Second bar high: 12.0
        context.assertNext(8.0)   // Third bar high: 8.0
        context.assertNext(15.0)  // Fourth bar high: 15.0
        context.assertNext(9.0)   // Fifth bar high: 9.0
        context.assertNext(14.0)  // Sixth bar high: 14.0
        context.assertNext(11.0)  // Seventh bar high: 11.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate highest high over 3-period window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 9.0, 14.0, 11.0, 7.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        // Period 1: max(10.0) = 10.0
        context.assertNext(10.0)
        // Period 2: max(10.0, 12.0) = 12.0
        context.assertNext(12.0)
        // Period 3: max(10.0, 12.0, 8.0) = 12.0
        context.assertNext(12.0)
        // Period 4: max(12.0, 8.0, 15.0) = 15.0
        context.assertNext(15.0)
        // Period 5: max(8.0, 15.0, 9.0) = 15.0
        context.assertNext(15.0)
        // Period 6: max(15.0, 9.0, 14.0) = 15.0
        context.assertNext(15.0)
        // Period 7: max(9.0, 14.0, 11.0) = 14.0
        context.assertNext(14.0)
        // Period 8: max(14.0, 11.0, 7.0) = 14.0
        context.assertNext(14.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate highest high over 5-period window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(20.0, 25.0, 18.0, 30.0, 22.0, 28.0, 19.0, 24.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 5)
        context.withIndicator(indicator)

        // Period 1: max(20.0) = 20.0
        context.assertNext(20.0)
        // Period 2: max(20.0, 25.0) = 25.0
        context.assertNext(25.0)
        // Period 3: max(20.0, 25.0, 18.0) = 25.0
        context.assertNext(25.0)
        // Period 4: max(20.0, 25.0, 18.0, 30.0) = 30.0
        context.assertNext(30.0)
        // Period 5: max(20.0, 25.0, 18.0, 30.0, 22.0) = 30.0
        context.assertNext(30.0)
        // Period 6: max(25.0, 18.0, 30.0, 22.0, 28.0) = 30.0
        context.assertNext(30.0)
        // Period 7: max(18.0, 30.0, 22.0, 28.0, 19.0) = 30.0
        context.assertNext(30.0)
        // Period 8: max(30.0, 22.0, 28.0, 19.0, 24.0) = 30.0
        context.assertNext(30.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle ascending price pattern`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 10.0, 15.0, 20.0, 25.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        context.assertNext(5.0)   // max(5.0) = 5.0
        context.assertNext(10.0)  // max(5.0, 10.0) = 10.0
        context.assertNext(15.0)  // max(5.0, 10.0, 15.0) = 15.0
        context.assertNext(20.0)  // max(10.0, 15.0, 20.0) = 20.0
        context.assertNext(25.0)  // max(15.0, 20.0, 25.0) = 25.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle descending price pattern`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(25.0, 20.0, 15.0, 10.0, 5.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        context.assertNext(25.0)  // max(25.0) = 25.0
        context.assertNext(25.0)  // max(25.0, 20.0) = 25.0
        context.assertNext(25.0)  // max(25.0, 20.0, 15.0) = 25.0
        context.assertNext(20.0)  // max(20.0, 15.0, 10.0) = 20.0
        context.assertNext(15.0)  // max(15.0, 10.0, 5.0) = 15.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle equal prices`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.0, 15.0, 15.0, 15.0, 15.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        context.assertNext(15.0)  // max(15.0) = 15.0
        context.assertNext(15.0)  // max(15.0, 15.0) = 15.0
        context.assertNext(15.0)  // max(15.0, 15.0, 15.0) = 15.0
        context.assertNext(15.0)  // max(15.0, 15.0, 15.0) = 15.0
        context.assertNext(15.0)  // max(15.0, 15.0, 15.0) = 15.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag property`(numFactory: NumFactory) {
        val indicator3 = DonchianChannelUpperIndicator(numFactory, 3)
        assertThat(indicator3.lag).isEqualTo(3)

        val indicator10 = DonchianChannelUpperIndicator(numFactory, 10)
        assertThat(indicator10.lag).isEqualTo(10)

        val indicator1 = DonchianChannelUpperIndicator(numFactory, 1)
        assertThat(indicator1.lag).isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be unstable until enough bars received`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 9.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        // Should be unstable initially
        assertUnstable(indicator)

        context.advance()  // 1st bar
        assertUnstable(indicator)

        context.advance()  // 2nd bar
        assertUnstable(indicator)

        context.advance()  // 3rd bar - now should be stable
        assertStable(indicator)

        context.advance()  // 4th bar - should remain stable
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable immediately with bar count 1`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 1)
        context.withIndicator(indicator)

        // Should be unstable initially
        assertUnstable(indicator)

        context.advance()  // 1st bar - should be stable immediately
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle volatile prices correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 50.0, 200.0, 25.0, 150.0, 75.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 4)
        context.withIndicator(indicator)

        context.assertNext(100.0)  // max(100.0) = 100.0
        context.assertNext(100.0)  // max(100.0, 50.0) = 100.0
        context.assertNext(200.0)  // max(100.0, 50.0, 200.0) = 200.0
        context.assertNext(200.0)  // max(100.0, 50.0, 200.0, 25.0) = 200.0
        context.assertNext(200.0)  // max(50.0, 200.0, 25.0, 150.0) = 200.0
        context.assertNext(200.0)  // max(200.0, 25.0, 150.0, 75.0) = 200.0
    }

    @Test
    fun `should have meaningful toString representation`() {
        val indicator = DonchianChannelUpperIndicator(org.ta4j.core.num.DoubleNumFactory, 5)
        assertThat(indicator.toString()).startsWith("DCU(5)")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar scenario`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(42.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 1)
        context.withIndicator(indicator)

        context.assertNext(42.0)
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should track rolling maximum correctly when new high appears`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 25.0, 18.0, 12.0)

        val indicator = DonchianChannelUpperIndicator(numFactory, 3)
        context.withIndicator(indicator)

        context.assertNext(10.0)  // max(10.0) = 10.0
        context.assertNext(20.0)  // max(10.0, 20.0) = 20.0
        context.assertNext(20.0)  // max(10.0, 20.0, 15.0) = 20.0
        context.assertNext(25.0)  // max(20.0, 15.0, 25.0) = 25.0 - new high
        context.assertNext(25.0)  // max(15.0, 25.0, 18.0) = 25.0
        context.assertNext(25.0)  // max(25.0, 18.0, 12.0) = 25.0
    }
}
