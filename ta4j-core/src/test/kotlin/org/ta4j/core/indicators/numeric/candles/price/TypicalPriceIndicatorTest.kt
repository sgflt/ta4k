/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class TypicalPriceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveTypicalPrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()

        val typicalPriceIndicator = Indicators.extended(numFactory).typicalPrice()
        context.withIndicator(typicalPriceIndicator)

        while (context.advance()) {
            val bar = context.barSeries.bar
            val expectedTypicalPrice = (bar.highPrice + bar.lowPrice + bar.closePrice) / numFactory.numOf(3)
            context.assertCurrent(expectedTypicalPrice.doubleValue())
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateTypicalPriceWithSpecificValues(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                // Close prices
                10.0, 20.0, 15.0, 30.0, 25.0
            )

        // Manually create bars with different H, L, C values for more realistic testing
        // Using close price for all OHLC to simplify, but the calculation will be (H+L+C)/3
        val typicalPriceIndicator = Indicators.extended(numFactory).typicalPrice()
        context.withIndicator(typicalPriceIndicator)

        // Since we're using close price for all OHLC, typical price should equal close price
        // (C+C+C)/3 = C
        context.assertNext(10.0)
        context.assertNext(20.0)
        context.assertNext(15.0)
        context.assertNext(30.0)
        context.assertNext(25.0)
    }
}