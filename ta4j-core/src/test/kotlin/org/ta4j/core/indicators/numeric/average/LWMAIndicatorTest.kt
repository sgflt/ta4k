/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class LWMAIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(37.08, 36.7, 36.11, 35.85, 35.71, 36.04, 36.41, 37.67, 38.01, 37.79, 36.83)
    }

    @ParameterizedTest(name = "LWMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lwmaUsingBarCount5UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val lwma = Indicators.closePrice().lwma(5)

        testContext.withIndicator(lwma)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(36.0506)
            .assertNext(35.9673)
            .assertNext(36.0766)
            .assertNext(36.6253)
            .assertNext(37.1833)
            .assertNext(37.5240)
            .assertNext(37.4060)
    }
}
