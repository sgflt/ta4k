/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY THEORY OF LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric.operation

import java.time.temporal.ChronoUnit
import kotlin.math.E
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class UnaryOperationTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrt(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 4.0, 9.0, 16.0, 25.0)
        
        val indicator = closePrice()
        val result = UnaryOperation.sqrt(indicator)
        
        context.withIndicator(result)
            .assertNext(1.0) // sqrt(1) = 1
            .assertNext(2.0) // sqrt(4) = 2
            .assertNext(3.0) // sqrt(9) = 3
            .assertNext(4.0) // sqrt(16) = 4
            .assertNext(5.0) // sqrt(25) = 5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testAbs(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(-5.0, -2.0, 0.0, 3.0, -7.0)
        
        val indicator = closePrice()
        val result = UnaryOperation.abs(indicator)
        
        context.withIndicator(result)
            .assertNext(5.0) // abs(-5) = 5
            .assertNext(2.0) // abs(-2) = 2
            .assertNext(0.0) // abs(0) = 0
            .assertNext(3.0) // abs(3) = 3
            .assertNext(7.0) // abs(-7) = 7
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testPow(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(2.0, 3.0, 4.0, 5.0, 6.0)
        
        val indicator = closePrice()
        val result = UnaryOperation.pow(indicator, 2)
        
        context.withIndicator(result)
            .assertNext(4.0) // 2^2 = 4
            .assertNext(9.0) // 3^2 = 9
            .assertNext(16.0) // 4^2 = 16
            .assertNext(25.0) // 5^2 = 25
            .assertNext(36.0) // 6^2 = 36
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testPowWithDecimal(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(4.0, 9.0, 16.0, 25.0, 36.0)
        
        val indicator = closePrice()
        val result = UnaryOperation.pow(indicator, 0.5)
        
        context.withIndicator(result)
            .assertNext(2.0) // 4^0.5 = 2
            .assertNext(3.0) // 9^0.5 = 3
            .assertNext(4.0) // 16^0.5 = 4
            .assertNext(5.0) // 25^0.5 = 5
            .assertNext(6.0) // 36^0.5 = 6
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testPowWithZero(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(2.0, 3.0, 4.0, 5.0, 6.0)
        
        val indicator = closePrice()
        val result = UnaryOperation.pow(indicator, 0)
        
        context.withIndicator(result)
            .assertNext(1.0) // 2^0 = 1
            .assertNext(1.0) // 3^0 = 1
            .assertNext(1.0) // 4^0 = 1
            .assertNext(1.0) // 5^0 = 1
            .assertNext(1.0) // 6^0 = 1
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testLog(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, E, 10.0, 100.0, E * E)
        
        val indicator = closePrice()
        val result = UnaryOperation.log(indicator)
        
        context.withIndicator(result)
            .assertNext(0.0) // ln(1) = 0
            .assertNext(1.0) // ln(e) = 1
            .assertNext(kotlin.math.ln(10.0)) // ln(10)
            .assertNext(kotlin.math.ln(100.0)) // ln(100)
            .assertNext(2.0) // ln(e^2) = 2
    }
}