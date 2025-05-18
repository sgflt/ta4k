/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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

package org.ta4j.core.indicators.numeric.momentum

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.num.NumFactory

class StochasticRSIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stochasticRSI(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07,
                50.01, 50.14, 50.22, 50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62,
                50.90, 50.82, 50.86, 51.20, 51.30, 51.10
            )

        val srsi = StochasticRSIIndicator(numFactory, barCount = 14)
        context.withIndicator(srsi)

        context.fastForwardUntilStable()

        context.assertNext(1.0)
        context.assertNext(1.0)
        context.assertNext(0.9460)
        context.assertNext(1.0)
        context.assertNext(0.8365)
        context.assertNext(0.8610)
        context.assertNext(1.0)
        context.assertNext(0.9186)
        context.assertNext(0.9305)
        context.assertNext(1.0)
        context.assertNext(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun edgeCaseZeroDenominator(numFactory: NumFactory) {
        // Test when max RSI equals min RSI (denominator would be zero)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0,
                100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0
            )

        val srsi = StochasticRSIIndicator(numFactory, barCount = 14)
        context.withIndicator(srsi)

        context.fastForwardUntilStable()

        // Value should be 0 in this case (or some other defined value for division by zero)
        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun withAlternateConstructors(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07,
                50.01, 50.14, 50.22, 50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62
            )

        // Test constructor with BarSeries
        val srsi1 = StochasticRSIIndicator(numFactory, barCount = 14)

        // Test constructor with underlying indicator
        val closePrice = ClosePriceIndicator(numFactory)
        val srsi2 = StochasticRSIIndicator(numFactory, closePrice, 14)

        // Test constructor with RSI indicator
        val rsi = RSIIndicator(closePrice, 14)
        val srsi3 = StochasticRSIIndicator(numFactory, rsi, 14)

        // Add all indicators to context with names for reference
        context.withIndicator(srsi1, "srsi1")
            .withIndicator(srsi2, "srsi2")
            .withIndicator(srsi3, "srsi3")

        // Fast forward past unstable period
        context.fastForwardUntilStable()

        // All three should produce the same value
        val value1 = srsi1.value

        // Check that all indicators produce the same value
        context.onIndicator("srsi1").assertCurrent(value1.doubleValue())
        context.onIndicator("srsi2").assertCurrent(value1.doubleValue())
        context.onIndicator("srsi3").assertCurrent(value1.doubleValue())
    }
}
