/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.indicators

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator
import org.ta4j.core.num.NumFactory

class RSIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return 100 when only gains occur`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                50.0, 51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0,
                60.0, 61.0, 62.0, 63.0, 64.0, 65.0
            )
            .withIndicator(RSIIndicator(Indicators.extended(numFactory).closePrice(), 1))

        context.fastForwardUntilStable()
        context.assertNext(100.0)
        context.assertNext(100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return 0 when only losses occur`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                60.0, 59.0, 58.0, 57.0, 56.0, 55.0, 54.0, 53.0, 52.0, 51.0,
                50.0, 49.0, 48.0, 47.0, 46.0, 45.0
            )
            .withIndicator(RSIIndicator(Indicators.extended(numFactory).closePrice(), 1))

        context.fastForwardUntilStable()
        context.assertNext(0.0)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return 50 when no movement occurs`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0,
                50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0
            )
            .withIndicator(RSIIndicator(Indicators.extended(numFactory).closePrice(), 4))

        context.fastForwardUntilStable()
        repeat(10) {
            context.assertNext(50.0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate RSI with bar count 14 using close price`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07,
                50.01, 50.14, 50.22, 50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62,
                50.90, 50.82, 50.86, 51.20, 51.30, 51.10
            )
            .withIndicator(RSIIndicator(Indicators.extended(numFactory).closePrice(), 14))

        context.fastForwardUntilStable()
        context.assertNext(65.7572)
        context.assertNext(68.4746)
        context.assertNext(64.7836)
        context.assertNext(72.0777)
        context.assertNext(60.7800)
        context.assertNext(63.6439)
        context.assertNext(72.3433)
        context.assertNext(67.3822)
        context.assertNext(68.5438)
        context.assertNext(76.2770)
        context.assertNext(77.9908)
        context.assertNext(67.4895)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate RSI with realistic data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                44.34, 44.09, 44.15, 43.61, 44.33, 44.83, 45.85, 46.08, 45.89, 46.03,
                46.83, 46.69, 46.45, 46.59, 46.3, 46.28, 46.28, 46.00, 46.03, 46.41
            )
            .withIndicator(RSIIndicator(Indicators.extended(numFactory).closePrice(), 14))

        context.fastForwardUntilStable()

        // Test that RSI values are within expected range
        val rsiValue = context.firstNumericIndicator!!.value.doubleValue()
        assert(rsiValue >= 0.0 && rsiValue <= 100.0) { "RSI should be between 0 and 100, but was $rsiValue" }
    }
}
