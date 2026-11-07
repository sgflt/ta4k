/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.XLSIndicatorTest
import org.ta4j.core.num.NumFactory

internal class EMAIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext()
            .withCandlePrices(
                64.75, 63.79, 63.73, 63.73, 63.55, 63.19,
                63.91, 63.85, 62.95, 63.37, 61.33, 61.51
            )
    }

    @ParameterizedTest(name = "First value equals first data value [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun firstValueShouldBeEqualsToFirstDataValue(factory: NumFactory) {
        context.withNumFactory(factory)
            .withIndicator(Indicators.closePrice().ema(1))
            .assertNext(64.75)
    }

    @ParameterizedTest(name = "EMA with barCount 10 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testEmaWithBarCount10(factory: NumFactory) {
        context.withNumFactory(factory)
            .withIndicator(Indicators.closePrice().ema(10))
            .fastForwardUntilStable()
            .assertCurrent(63.6948)
            .assertNext(63.2648)
            .assertNext(62.9457)
    }

    @ParameterizedTest(name = "External data test [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testExternalData(numFactory: NumFactory) {
        val xls = XLSIndicatorTest(this.javaClass, "EMA.xls", 6, numFactory)
        val xlsContext = MarketEventTestContext()
        xlsContext.withMarketEvents(xls.getMarketEvents())

        val indicator = Indicators.closePrice().ema(1)
        val expectedIndicator = xls.getIndicator(1)
        xlsContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
            .assertCurrent(329.0)
    }

    @ParameterizedTest(name = "External data with bar count 3 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testExternalDataBarCount3(numFactory: NumFactory) {
        val xls = XLSIndicatorTest(this.javaClass, "EMA.xls", 6, numFactory)
        val xlsContext = MarketEventTestContext()
        xlsContext.withMarketEvents(xls.getMarketEvents())

        val indicator = Indicators.closePrice().ema(3)
        val expectedIndicator = xls.getIndicator(3)
        xlsContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
            .assertCurrent(327.7748)
    }

    @ParameterizedTest(name = "External data with bar count 13 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testExternalDataBarCount13(numFactory: NumFactory) {
        val xls = XLSIndicatorTest(this.javaClass, "EMA.xls", 6, numFactory)
        val xlsContext = MarketEventTestContext()
        xlsContext.withMarketEvents(xls.getMarketEvents())

        val indicator = Indicators.closePrice().ema(13)
        val expectedIndicator = xls.getIndicator(13)
        xlsContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
            .assertCurrent(327.4076)
    }
}
