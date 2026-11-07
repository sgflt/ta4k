/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators

internal class BollingerBandsMiddleIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0
        )
    }

    @ParameterizedTest(name = "Middle bollinger band derived from SMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandsMiddleUsingSMA() {
        val sma = Indicators.closePrice().sma(3)
        val bbmSMA = BollingerBandsMiddleIndicator(sma)

        testContext.withIndicators(bbmSMA, sma)
            .fastForwardUntilStable()
            .assertIndicatorEquals(sma, bbmSMA)
    }
}