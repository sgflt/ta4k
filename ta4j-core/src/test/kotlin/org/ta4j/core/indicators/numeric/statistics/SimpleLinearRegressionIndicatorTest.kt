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
package org.ta4j.core.indicators.numeric.statistics

import org.apache.commons.math3.stat.regression.SimpleRegression
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class SimpleLinearRegressionIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun notComputedLinearRegression(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 30.0, 40.0, 30.0, 20.0, 30.0, 50.0, 60.0, 70.0, 80.0)

        val reg = SimpleLinearRegressionIndicator(closePrice(), 1)

        context.withIndicator(reg)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateLinearRegressionWithLessThan2ObservationsReturnsZero(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 30.0, 40.0, 30.0, 20.0, 30.0, 50.0, 60.0, 70.0, 80.0)

        val reg = SimpleLinearRegressionIndicator(closePrice(), 1)

        context.withIndicator(reg)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateLinearRegressionOn4Observations(numFactory: NumFactory) {
        val data = listOf(10.0, 20.0, 30.0, 40.0, 30.0, 40.0, 30.0, 20.0, 30.0, 50.0, 60.0, 70.0, 80.0)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*data.toDoubleArray())

        val reg = SimpleLinearRegressionIndicator(closePrice(), 4)

        context.withIndicator(reg)

        val origReg = buildSimpleRegression(10.0, 20.0, 30.0, 40.0)
        context.fastForward(4)
            .assertCurrent(origReg.predict(3.0))

        origReg.removeData(0.0, 10.0)
        origReg.addData(4.0, 30.0)
        context.assertNext(origReg.predict(3.0))

        origReg.removeData(1.0, 20.0)
        origReg.addData(5.0, 40.0)
        context.assertNext(origReg.predict(3.0))

        origReg.removeData(2.0, 30.0)
        origReg.addData(6.0, 30.0)
        context.assertNext(origReg.predict(3.0))

        origReg.removeData(3.0, 40.0)
        origReg.addData(7.0, 20.0)
        context.assertNext(origReg.predict(3.0))

        origReg.removeData(4.0, 30.0)
        origReg.addData(8.0, 30.0)
        context.assertNext(origReg.predict(3.0))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateLinearRegression(numFactory: NumFactory) {
        val values = doubleArrayOf(1.0, 2.0, 1.3, 3.75, 2.25, 1.8) // Added extra data point
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*values)

        val reg = SimpleLinearRegressionIndicator(closePrice(), 5)

        val origReg = buildSimpleRegression(1.0, 2.0, 1.3, 3.75, 2.25) // Use first 5 values for regression
        context.withIndicator(reg)
            .fastForward(5)
            .assertCurrent(origReg.predict(4.0))
    }

    /**
     * @param values values
     * @return a simple linear regression based on provided values
     */
    private fun buildSimpleRegression(vararg values: Double): SimpleRegression {
        val simpleReg = SimpleRegression()
        for (i in values.indices) {
            simpleReg.addData(i.toDouble(), values[i])
        }
        return simpleReg
    }
}
