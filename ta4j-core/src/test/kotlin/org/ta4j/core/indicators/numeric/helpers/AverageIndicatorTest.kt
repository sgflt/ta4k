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
package org.ta4j.core.indicators.numeric.helpers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

class AverageIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should average two constant indicators correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)

        val one = ConstantNumericIndicator(numFactory.numOf(1.0))
        val two = ConstantNumericIndicator(numFactory.numOf(2.0))
        val averageIndicator = AverageIndicator(numFactory, one, two)

        context
            .withIndicator(averageIndicator)
            .assertNext(1.5)  // (1 + 2) / 2 = 1.5
            .assertNext(1.5)
            .assertNext(1.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should average three constant indicators correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)

        val one = ConstantNumericIndicator(numFactory.numOf(1.0))
        val two = ConstantNumericIndicator(numFactory.numOf(2.0))
        val three = ConstantNumericIndicator(numFactory.numOf(3.0))
        val averageIndicator = AverageIndicator(numFactory, one, two, three)

        context
            .withIndicator(averageIndicator)
            .assertNext(2.0)  // (1 + 2 + 3) / 3 = 2
            .assertNext(2.0)
            .assertNext(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should average price indicators correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highPrice = Indicators.extended(numFactory).highPrice()
        val averageIndicator = AverageIndicator(numFactory, closePrice, highPrice)

        context
            .withIndicator(averageIndicator)
            .assertNext(10.0)  // (10 + 10) / 2 = 10
            .assertNext(20.0)  // (20 + 20) / 2 = 20
            .assertNext(30.0)  // (30 + 30) / 2 = 30
            .assertNext(40.0)  // (40 + 40) / 2 = 40
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should average different price indicators correctly`(numFactory: NumFactory) {
        // For this test, we'll use candlePrices which assumes OHLC are the same
        // Since we need different high/low values, we'll test with simple price indicators
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val typicalPrice = Indicators.extended(numFactory).typicalPrice()
        val averageIndicator = AverageIndicator(numFactory, closePrice, typicalPrice)

        context
            .withIndicator(averageIndicator)
            .assertNext(10.0)  // (10 + 10) / 2 = 10
            .assertNext(20.0)  // (20 + 20) / 2 = 20
            .assertNext(30.0)  // (30 + 30) / 2 = 30
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should require at least one indicator`(numFactory: NumFactory) {
        assertThrows<IllegalArgumentException> {
            AverageIndicator(numFactory)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should average moving averages correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma2 = closePrice.sma(2)
        val sma3 = closePrice.sma(3)
        val averageIndicator = AverageIndicator(numFactory, sma2, sma3)

        context
            .withIndicator(averageIndicator)
            .fastForward(2) // Skip first 2 bars to let SMA3 stabilize
            .assertNext(22.5)  // SMA2: (20+30)/2=25, SMA3: (10+20+30)/3=20, Avg: 22.5
            .assertNext(32.5)  // SMA2: (30+40)/2=35, SMA3: (20+30+40)/3=30, Avg: 32.5
            .assertNext(42.5)  // SMA2: (40+50)/2=45, SMA3: (30+40+50)/3=40, Avg: 42.5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag property`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma5 = closePrice.sma(5)  // lag = 5
        val sma10 = closePrice.sma(10) // lag = 10
        val ema3 = closePrice.ema(3)   // lag = 3

        val averageIndicator = AverageIndicator(numFactory, closePrice, sma5, sma10, ema3)

        // Should return the maximum lag among all indicators
        assertThat(averageIndicator.lag).isEqualTo(10)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable when all indicators are stable`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 60.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma3 = closePrice.sma(3)
        val averageIndicator = AverageIndicator(numFactory, closePrice, sma3)

        context.withIndicator(averageIndicator)

        // Initially not stable because SMA3 needs 3 bars
        TestUtils.assertUnstable(averageIndicator)

        context.fastForward(3) // After 3 bars, SMA3 should be stable
        TestUtils.assertStable(averageIndicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful string representation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val highPrice = Indicators.extended(numFactory).highPrice()
        val averageIndicator = AverageIndicator(numFactory, closePrice, highPrice)

        val stringRep = averageIndicator.toString()
        assertThat(stringRep).contains("AVG")
        assertThat(stringRep).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single indicator correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val averageIndicator = AverageIndicator(numFactory, closePrice)

        context
            .withIndicator(averageIndicator)
            .assertNext(10.0)  // Average of single value is the value itself
            .assertNext(20.0)
            .assertNext(30.0)
    }
}