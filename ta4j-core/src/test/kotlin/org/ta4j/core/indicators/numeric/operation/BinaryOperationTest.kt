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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class BinaryOperationTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSumIndicatorToIndicator(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val left = closePrice()
        val right = closePrice()
        val result = BinaryOperation.sum(left, right)
        
        context.withIndicator(result)
            .assertNext(2.0) // 1 + 1 = 2
            .assertNext(4.0) // 2 + 2 = 4
            .assertNext(6.0) // 3 + 3 = 6
            .assertNext(8.0) // 4 + 4 = 8
            .assertNext(10.0) // 5 + 5 = 10
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSumIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.sum(indicator, 10)
        
        context.withIndicator(result)
            .assertNext(11.0) // 1 + 10 = 11
            .assertNext(12.0) // 2 + 10 = 12
            .assertNext(13.0) // 3 + 10 = 13
            .assertNext(14.0) // 4 + 10 = 14
            .assertNext(15.0) // 5 + 10 = 15
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testProductIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.product(indicator, 2.5)
        
        context.withIndicator(result)
            .assertNext(2.5) // 1 * 2.5 = 2.5
            .assertNext(5.0) // 2 * 2.5 = 5.0
            .assertNext(7.5) // 3 * 2.5 = 7.5
            .assertNext(10.0) // 4 * 2.5 = 10.0
            .assertNext(12.5) // 5 * 2.5 = 12.5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDifferenceIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(10.0, 15.0, 20.0, 25.0, 30.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.difference(indicator, 5)
        
        context.withIndicator(result)
            .assertNext(5.0) // 10 - 5 = 5
            .assertNext(10.0) // 15 - 5 = 10
            .assertNext(15.0) // 20 - 5 = 15
            .assertNext(20.0) // 25 - 5 = 20
            .assertNext(25.0) // 30 - 5 = 25
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testQuotientIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(10.0, 15.0, 20.0, 25.0, 30.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.quotient(indicator, 5)
        
        context.withIndicator(result)
            .assertNext(2.0) // 10 / 5 = 2
            .assertNext(3.0) // 15 / 5 = 3
            .assertNext(4.0) // 20 / 5 = 4
            .assertNext(5.0) // 25 / 5 = 5
            .assertNext(6.0) // 30 / 5 = 6
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testMaxIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.max(indicator, 3)
        
        context.withIndicator(result)
            .assertNext(3.0) // max(1, 3) = 3
            .assertNext(3.0) // max(2, 3) = 3
            .assertNext(3.0) // max(3, 3) = 3
            .assertNext(4.0) // max(4, 3) = 4
            .assertNext(5.0) // max(5, 3) = 5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testMinIndicatorToNumber(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val indicator = closePrice()
        val result = BinaryOperation.min(indicator, 3)
        
        context.withIndicator(result)
            .assertNext(1.0) // min(1, 3) = 1
            .assertNext(2.0) // min(2, 3) = 2
            .assertNext(3.0) // min(3, 3) = 3
            .assertNext(3.0) // min(4, 3) = 3
            .assertNext(3.0) // min(5, 3) = 3
    }
}