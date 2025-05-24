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

class DonchianChannelLowerIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate lowest low over specified period`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 6.0, 11.0, 9.0)

        context.fastForwardUntilStable()
            .assertCurrent(8.0)  // lowest of [10, 12, 8]
            .assertNext(8.0)     // lowest of [12, 8, 15]
            .assertNext(6.0)     // lowest of [8, 15, 6]
            .assertNext(6.0)     // lowest of [15, 6, 11]
            .assertNext(6.0)     // lowest of [6, 11, 9]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single period correctly`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(5.0, 3.0, 7.0, 2.0, 8.0)

        context.assertNext(5.0)  // lowest of [5]
            .assertNext(3.0)      // lowest of [3]
            .assertNext(7.0)      // lowest of [7]
            .assertNext(2.0)      // lowest of [2]
            .assertNext(8.0)      // lowest of [8]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle decreasing price sequence`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 4)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(20.0, 18.0, 15.0, 12.0, 10.0, 8.0)

        context.fastForwardUntilStable()
            .assertCurrent(12.0)  // lowest of [20, 18, 15, 12]
            .assertNext(10.0)     // lowest of [18, 15, 12, 10]
            .assertNext(8.0)      // lowest of [15, 12, 10, 8]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle increasing price sequence`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(5.0, 8.0, 12.0, 15.0, 18.0)

        context.fastForwardUntilStable()
            .assertCurrent(5.0)   // lowest of [5, 8, 12]
            .assertNext(8.0)      // lowest of [8, 12, 15]
            .assertNext(12.0)     // lowest of [12, 15, 18]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle equal prices`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0)

        context.fastForwardUntilStable()
            .assertCurrent(10.0)  // lowest of [10, 10, 10]
            .assertNext(10.0)     // lowest of [10, 10, 10]
            .assertNext(10.0)     // lowest of [10, 10, 10]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability behavior`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 5)

        assertThat(indicator.lag).isEqualTo(5)
        assertUnstable(indicator)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        // Should become stable after 5 bars
        context.fastForward(4)
        assertUnstable(indicator)

        context.advance()
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should track lowest value correctly in volatile market`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 4)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(15.0, 8.0, 22.0, 5.0, 18.0, 3.0, 25.0)

        context.fastForwardUntilStable()
            .assertCurrent(5.0)   // lowest of [15, 8, 22, 5]
            .assertNext(5.0)      // lowest of [8, 22, 5, 18]
            .assertNext(3.0)      // lowest of [22, 5, 18, 3]
            .assertNext(3.0)      // lowest of [5, 18, 3, 25]
    }

    @Test
    fun `should require positive bar count`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory

        assertThrows<IllegalArgumentException> {
            DonchianChannelLowerIndicator(numFactory, 0)
        }

        assertThrows<NegativeArraySizeException> {
            DonchianChannelLowerIndicator(numFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `toString should include bar count and current value`(numFactory: NumFactory) {
        val indicator = DonchianChannelLowerIndicator(numFactory, 5)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(10.0, 8.0, 12.0, 6.0, 15.0)

        context.fastForwardUntilStable()

        val toString = indicator.toString()
        assertThat(toString).contains("DCL(5) => 6.0")
        assertThat(toString).contains(indicator.value.toString())
    }
}
