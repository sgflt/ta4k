/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.XlsTestsUtils
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
