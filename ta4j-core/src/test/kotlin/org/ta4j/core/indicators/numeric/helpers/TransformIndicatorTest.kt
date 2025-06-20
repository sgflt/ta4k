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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class TransformIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testArithmeticTransformations(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()

        val constantIndicator = ConstantNumericIndicator(numFactory.numOf(4))

        val transPlus = TransformIndicator.plus(constantIndicator, 10)
        val transMinus = TransformIndicator.minus(constantIndicator, 10)
        val transMultiply = TransformIndicator.multiply(constantIndicator, 10)
        val transDivide = TransformIndicator.divide(constantIndicator, 10)
        val transMax = TransformIndicator.max(constantIndicator, 10)
        val transMin = TransformIndicator.min(constantIndicator, 10)

        // Test plus: 4 + 10 = 14
        context.withIndicator(transPlus)
        context.advance()
        context.assertCurrent(14.0)

        // Test minus: 4 - 10 = -6
        val contextMinus = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transMinus)
        contextMinus.advance()
        contextMinus.assertCurrent(-6.0)

        // Test multiply: 4 * 10 = 40
        val contextMultiply = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transMultiply)
        contextMultiply.advance()
        contextMultiply.assertCurrent(40.0)

        // Test divide: 4 / 10 = 0.4
        val contextDivide = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transDivide)
        contextDivide.advance()
        contextDivide.assertCurrent(0.4)

        // Test max: max(4, 10) = 10
        val contextMax = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transMax)
        contextMax.advance()
        contextMax.assertCurrent(10.0)

        // Test min: min(4, 10) = 4
        val contextMin = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transMin)
        contextMin.advance()
        contextMin.assertCurrent(4.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testMathematicalTransformations(numFactory: NumFactory) {
        val constantIndicator = ConstantNumericIndicator(numFactory.numOf(4))
        val negativeConstantIndicator = ConstantNumericIndicator(numFactory.numOf(-4))

        val transAbs = TransformIndicator.abs(negativeConstantIndicator)
        val transPow = TransformIndicator.pow(constantIndicator, 2)
        val transSqrt = TransformIndicator.sqrt(constantIndicator)
        val transLog = TransformIndicator.log(constantIndicator)

        // Test abs: abs(-4) = 4
        val contextAbs = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transAbs)
        contextAbs.advance()
        contextAbs.assertCurrent(4.0)

        // Test pow: 4^2 = 16
        val contextPow = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transPow)
        contextPow.advance()
        contextPow.assertCurrent(16.0)

        // Test sqrt: sqrt(4) = 2
        val contextSqrt = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transSqrt)
        contextSqrt.advance()
        contextSqrt.assertCurrent(2.0)

        // Test log: ln(4) ≈ 1.3862943611198906
        val contextLog = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
            .withIndicator(transLog)
        contextLog.advance()
        contextLog.assertCurrent(1.3862943611198906)
    }
}