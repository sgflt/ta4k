/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class RunningTotalIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculate(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        val runningTotal = Indicators.extended(numFactory).closePrice().runningTotal(3)
        context.withIndicator(runningTotal)

        // Expected values: running total with window size 3
        // Bar 0: 1 (only 1 bar available)
        // Bar 1: 1+2 = 3 (only 2 bars available)  
        // Bar 2: 1+2+3 = 6 (3 bars available, sum of last 3)
        // Bar 3: 2+3+4 = 9 (3 bars available, sum of last 3)
        // Bar 4: 3+4+5 = 12 (3 bars available, sum of last 3)
        // Bar 5: 4+5+6 = 15 (3 bars available, sum of last 3)

        context.assertNext(1.0)
        context.assertNext(3.0)
        context.assertNext(6.0)
        context.assertNext(9.0)
        context.assertNext(12.0)
        context.assertNext(15.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithDifferentWindowSize(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0)

        val runningTotal = Indicators.extended(numFactory).closePrice().runningTotal(2)
        context.withIndicator(runningTotal)

        // Expected values with window size 2:
        // Bar 0: 10 (only 1 bar available)
        // Bar 1: 10+20 = 30 (2 bars available)
        // Bar 2: 20+30 = 50 (sum of last 2)
        // Bar 3: 30+40 = 70 (sum of last 2)

        context.assertNext(10.0)
        context.assertNext(30.0)
        context.assertNext(50.0)
        context.assertNext(70.0)
    }
}
