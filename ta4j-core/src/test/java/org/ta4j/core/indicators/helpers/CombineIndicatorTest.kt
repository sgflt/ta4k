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

package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.operation.CombineIndicator
import org.ta4j.core.num.NumFactory

class CombineIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testAllDefaultMathCombineFunctions(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()

        val constantIndicator = ConstantNumericIndicator(numFactory.numOf(4))
        val constantIndicatorTwo = ConstantNumericIndicator(numFactory.numOf(2))

        val combinePlus = CombineIndicator.plus(constantIndicator, constantIndicatorTwo)
        val combineMinus = CombineIndicator.minus(constantIndicator, constantIndicatorTwo)
        val combineMultiply = CombineIndicator.multiply(constantIndicator, constantIndicatorTwo)
        val combineDivide = CombineIndicator.divide(constantIndicator, constantIndicatorTwo)
        val combineMax = CombineIndicator.max(constantIndicator, constantIndicatorTwo)
        val combineMin = CombineIndicator.min(constantIndicator, constantIndicatorTwo)

        context.withIndicator(combinePlus)

        context.advance()
        context.assertCurrent(6.0) // 4 + 2

        // Test other indicators individually to avoid complexity with multiple indicators
        val contextMinus = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(combineMinus)
        contextMinus.advance()
        contextMinus.assertCurrent(2.0) // 4 - 2

        val contextMultiply = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(combineMultiply)
        contextMultiply.advance()
        contextMultiply.assertCurrent(8.0) // 4 * 2

        val contextDivide = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(combineDivide)
        contextDivide.advance()
        contextDivide.assertCurrent(2.0) // 4 / 2

        val contextMax = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(combineMax)
        contextMax.advance()
        contextMax.assertCurrent(4.0) // max(4, 2)

        val contextMin = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(combineMin)
        contextMin.advance()
        contextMin.assertCurrent(2.0) // min(4, 2)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDifferenceIndicator(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-2.0, 0.0, 1.0, 2.53, 5.87, 6.0, 10.0)

        val constantIndicator = ConstantNumericIndicator(numFactory.numOf(6))
        val closePrice = Indicators.extended(numFactory).closePrice()
        val differenceIndicator = CombineIndicator.minus(constantIndicator, closePrice)

        context.withIndicator(differenceIndicator)

        context.assertNext(8.0)    // 6 - (-2.0) = 8
        context.assertNext(6.0)    // 6 - 0.0 = 6
        context.assertNext(5.0)    // 6 - 1.0 = 5
        context.assertNext(3.47)   // 6 - 2.53 = 3.47
        context.assertNext(0.13)   // 6 - 5.87 = 0.13
        context.assertNext(0.0)    // 6 - 6.0 = 0
        context.assertNext(-4.0)   // 6 - 10.0 = -4
    }
}