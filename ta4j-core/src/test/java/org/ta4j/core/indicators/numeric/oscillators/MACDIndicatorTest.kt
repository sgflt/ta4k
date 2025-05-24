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

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.oscilators.MACDIndicator
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

class MACDIndicatorTest {

    @Test
    fun `should throw IllegalArgumentException when short period is greater than long period`() {
        val numFactory = DoubleNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()

        assertThatThrownBy {
            MACDIndicator(numFactory, closePrice, shortBarCount = 10, longBarCount = 5)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Long term period count must be greater than short term period count")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate MACD with periods 5 and 10`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val macd = MACDIndicator(numFactory, closePrice, shortBarCount = 5, longBarCount = 10)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(macd)
            .withCandlePrices(
                37.08,
                36.7,
                36.11,
                35.85,
                35.71,
                36.04,
                36.41,
                37.67,
                38.01,
                37.79,
                36.83,
                37.10,
                38.01,
                38.50,
                38.99
            )

        context.assertNext(0.0)
        context.assertNext(-0.05757)
        context.assertNext(-0.17488)
        context.assertNext(-0.26766)
        context.assertNext(-0.32326)
        context.assertNext(-0.28399)
        context.assertNext(-0.18930)
        context.assertNext(0.06472)
        context.assertNext(0.25087)
        context.assertNext(0.30387)
        context.assertNext(0.16891)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should provide access to underlying EMA indicators`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val macd = MACDIndicator(numFactory, closePrice, shortBarCount = 5, longBarCount = 10)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(macd)
            .withCandlePrices(37.08, 36.7, 36.11, 35.85, 35.71, 36.04, 36.41, 37.67, 38.01, 37.79, 36.83)

        // Advance to bar 5 (index 5)
        context.fastForward(6)

        // Test EMA values at bar 5
        assertNumEquals(36.4098, macd.longTermEma.value)
        assertNumEquals(36.1258, macd.shortTermEma.value)

        // Advance to bar 10 (index 10)
        context.fastForward(5)

        // Test EMA values at bar 10
        assertNumEquals(37.0118, macd.longTermEma.value)
        assertNumEquals(37.1807, macd.shortTermEma.value)
    }
}
