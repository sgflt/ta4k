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
package org.ta4j.core.indicators.numeric.adx

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.XlsTestsUtils
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDIIndicator
import org.ta4j.core.num.NumFactory

class MinusDIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Minus DI period 1 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 13, numFactory, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(MinusDIIndicator(numFactory, 1))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        context.assertCurrent(0.0)

        // Verify all values match expected from XLS
        val minusDI = MinusDIIndicator(numFactory, 1)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(minusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, minusDI)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Minus DI period 3 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 13, numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(MinusDIIndicator(numFactory, 3))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        context.assertCurrent(21.0711)

        // Verify all values match expected from XLS
        val minusDI = MinusDIIndicator(numFactory, 3)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(minusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, minusDI)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Minus DI period 13 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 13, numFactory, 13)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(MinusDIIndicator(numFactory, 13))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        context.assertCurrent(20.9020)

        // Verify all values match expected from XLS
        val minusDI = MinusDIIndicator(numFactory, 13)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(minusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, minusDI)
    }
}
