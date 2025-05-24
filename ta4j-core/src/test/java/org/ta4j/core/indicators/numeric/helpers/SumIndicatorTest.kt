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
package org.ta4j.core.indicators.numeric.helpers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class SumIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should sum two price indicators correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 20.0, 25.0, 30.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highPrice = Indicators.extended(numFactory).highPrice()
        val sumIndicator = SumIndicator(numFactory, closePrice, highPrice)

        context
            .withIndicator(sumIndicator)
            .assertNext(20.0)  // 10 + 10 = 20
            .assertNext(30.0)  // 15 + 15 = 30
            .assertNext(40.0)  // 20 + 20 = 40
            .assertNext(50.0)  // 25 + 25 = 50
            .assertNext(60.0)  // 30 + 30 = 60
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should sum three indicators correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 10.0, 15.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highPrice = Indicators.extended(numFactory).highPrice()
        val lowPrice = Indicators.extended(numFactory).lowPrice()
        val sumIndicator = SumIndicator(numFactory, closePrice, highPrice, lowPrice)

        context
            .withIndicator(sumIndicator)
            .assertNext(15.0)  // 5 + 5 + 5 = 15
            .assertNext(30.0)  // 10 + 10 + 10 = 30
            .assertNext(45.0)  // 15 + 15 + 15 = 45
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should sum single indicator correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(7.0, 14.0, 21.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sumIndicator = SumIndicator(numFactory, closePrice)

        context
            .withIndicator(sumIndicator)
            .assertNext(7.0)
            .assertNext(14.0)
            .assertNext(21.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should sum moving averages correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma2 = closePrice.sma(2)
        val sma3 = closePrice.sma(3)
        val sumIndicator = SumIndicator(numFactory, sma2, sma3)

        context
            .withIndicator(sumIndicator)
            .fastForward(2) // Skip first 2 bars to let SMA3 stabilize
            .assertNext(45.0)  // SMA2: (20+30)/2=25, SMA3: (10+20+30)/3=20, Sum: 45
            .assertNext(65.0)  // SMA2: (30+40)/2=35, SMA3: (20+30+40)/3=30, Sum: 65
            .assertNext(85.0)  // SMA2: (40+50)/2=45, SMA3: (30+40+50)/3=40, Sum: 85
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle negative values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 5.0, 15.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val diff = closePrice.difference()
        val sumIndicator = SumIndicator(numFactory, closePrice, diff)

        context
            .withIndicator(sumIndicator)
            .assertNext(20.0)  // 10 + 10 = 20 (first difference is price itself)
            .assertNext(0.0)   // 5 + (-5) = 0
            .assertNext(25.0)  // 15 + 10 = 25
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag property`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma5 = closePrice.sma(5)  // lag = 5
        val sma10 = closePrice.sma(10) // lag = 10
        val ema3 = closePrice.ema(3)   // lag = 3

        val sumIndicator = SumIndicator(numFactory, closePrice, sma5, sma10, ema3)

        // Should return the maximum lag among all summands
        assertThat(sumIndicator.lag).isEqualTo(10)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable when all summands are stable`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 60.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma3 = closePrice.sma(3)
        val sumIndicator = SumIndicator(numFactory, closePrice, sma3)

        context.withIndicator(sumIndicator)

        // Initially not stable because SMA3 needs 3 bars
        TestUtils.assertUnstable(sumIndicator)

        context.fastForward(3) // After 3 bars, SMA3 should be stable
        TestUtils.assertStable(sumIndicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful string representation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val highPrice = Indicators.extended(numFactory).highPrice()
        val sumIndicator = SumIndicator(numFactory, closePrice, highPrice)

        val stringRep = sumIndicator.toString()
        assertThat(stringRep).contains("SUM")
        assertThat(stringRep).contains("=>")
    }
}
