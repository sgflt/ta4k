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
package org.ta4j.core.indicators

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.oscillators.AccelerationDecelerationIndicator
import org.ta4j.core.num.NumFactory

internal class AccelerationDecelerationIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext()
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithSma2AndSma3(numFactory: NumFactory) {
        val shortBarCount = 2
        val longBarCount = 3

        context.withNumFactory(numFactory)
            .withIndicator(AccelerationDecelerationIndicator(numFactory, shortBarCount, longBarCount))

        val shortSma1 = (0 + 1.0) / shortBarCount
        val longSma1 = (0 + 0 + 1.0) / longBarCount
        val awesome1 = shortSma1 - longSma1
        val awesomeSma1 = (0.0 + awesome1) / shortBarCount
        val acceleration1 = awesome1 - awesomeSma1
        context.assertNext(acceleration1)

        val shortSma2 = (1.0 + 2.0) / shortBarCount
        val longSma2 = (0 + 1.0 + 2.0) / longBarCount
        val awesome2 = shortSma2 - longSma2
        val awesomeSma2 = (awesome2 + awesome1) / shortBarCount
        val acceleration2 = awesome2 - awesomeSma2
        context.assertNext(acceleration2)

        val shortSma3 = (2.0 + 3.0) / shortBarCount
        val longSma3 = (1.0 + 2.0 + 3.0) / longBarCount
        val awesome3 = shortSma3 - longSma3
        val awesomeSma3 = (awesome3 + awesome2) / shortBarCount
        val acceleration3 = awesome3 - awesomeSma3
        context.assertNext(acceleration3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun withSma1AndSma2(numFactory: NumFactory) {
        context.withNumFactory(numFactory)
            .withIndicator(AccelerationDecelerationIndicator(numFactory, 1, 2))
            .fastForward(5)
            .assertCurrent(0.0)
    }
}