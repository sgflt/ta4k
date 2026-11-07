/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.GENERAL_OFFSET
import org.ta4j.core.XlsTestsUtils
import org.ta4j.core.num.NumFactory

class PlusDIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Plus DI period 1 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 12, numFactory, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(PlusDIIndicator(numFactory, 1))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        assertThat(context.firstNumericIndicator!!.value.doubleValue())
            .isCloseTo(12.5, Offset.offset(GENERAL_OFFSET))

        // Verify all values match expected from XLS
        val plusDI = PlusDIIndicator(numFactory, 1)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(plusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, plusDI)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Plus DI period 3 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 12, numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(PlusDIIndicator(numFactory, 3))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        assertThat(context.firstNumericIndicator!!.value.doubleValue())
            .isCloseTo(22.8407, Offset.offset(GENERAL_OFFSET))

        // Verify all values match expected from XLS
        val plusDI = PlusDIIndicator(numFactory, 3)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(plusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, plusDI)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Plus DI period 13 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ADX.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ADX.xls", 12, numFactory, 13)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(PlusDIIndicator(numFactory, 13))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        assertThat(context.firstNumericIndicator!!.value.doubleValue())
            .isCloseTo(22.1399, Offset.offset(GENERAL_OFFSET))

        // Verify all values match expected from XLS
        val plusDI = PlusDIIndicator(numFactory, 13)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(plusDI)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, plusDI)
    }
}
