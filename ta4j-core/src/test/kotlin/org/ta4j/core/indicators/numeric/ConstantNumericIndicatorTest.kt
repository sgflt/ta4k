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
package org.ta4j.core.indicators.numeric

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class ConstantNumericIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return constant value for any index`(numFactory: NumFactory) {
        val constantValue = numFactory.numOf(30.33)
        val constantIndicator = ConstantNumericIndicator(constantValue)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(constantIndicator)

        // Assert that the constant indicator returns the same value for all indices
        context
            .assertNext(30.33)
            .assertNext(30.33)
            .assertNext(30.33)
            .assertNext(30.33)
            .assertNext(30.33)

        // Test that direct value access also returns constant value
        assertThat(constantIndicator.value).isEqualTo(constantValue)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero and negative constant values`(numFactory: NumFactory) {
        // Test zero indicator separately
        val zeroIndicator = ConstantNumericIndicator(numFactory.zero())
        val zeroContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)
            .withIndicator(zeroIndicator)

        zeroContext
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)

        // Test negative indicator separately
        val negativeIndicator = ConstantNumericIndicator(numFactory.numOf(-15.75))
        val negativeContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0)
            .withIndicator(negativeIndicator)

        negativeContext
            .assertNext(-15.75)
            .assertNext(-15.75)
            .assertNext(-15.75)
    }
}
