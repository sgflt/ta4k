/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.XLSIndicatorTest
import org.ta4j.core.num.NumFactory

internal class ADXIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
    }

    @ParameterizedTest(name = "ADX (1,1) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData11(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.marketEvents)
        val indicator = ADXIndicator(numFactory, 1, 1)

        testContext.withIndicator(indicator)
            .fastForward(xlsTest.marketEvents.size - 1)
            .assertCurrent(100.0)
    }

    @ParameterizedTest(name = "ADX (3,2) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData32(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.marketEvents)
        val indicator = ADXIndicator(numFactory, 3, 2)
        val expectedIndicator = xlsTest.getIndicator(3, 2)

        testContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
    }

    @ParameterizedTest(name = "ADX (13,8) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData138(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.marketEvents)
        val indicator = ADXIndicator(numFactory, 13, 8)
        val expectedIndicator = xlsTest.getIndicator(13, 8)

        testContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
    }
}
