/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class BollingerBandWidthIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            10.0,
            12.0,
            15.0,
            14.0,
            17.0,
            20.0,
            21.0,
            20.0,
            20.0,
            19.0,
            20.0,
            17.0,
            12.0,
            12.0,
            9.0,
            8.0,
            9.0,
            10.0,
            9.0,
            10.0
        )
    }

    @ParameterizedTest(name = "Bollinger bands width [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandWidthUsingSMAAndStandardDeviation(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val bbf = Indicators.bollingerBands(5, 2)
        val bandwidth = bbf.bandwidth

        testContext.withIndicators(bandwidth)
            .fastForwardUntilStable()
            .assertCurrent(79.4662)
            .assertNext(78.1946)
            .assertNext(70.1055)
            .assertNext(62.6298)
            .assertNext(30.9505)
            .assertNext(14.1421)
            .assertNext(14.1421)
            .assertNext(27.1633)
            .assertNext(76.3989)
            .assertNext(95.1971)
            .assertNext(126.1680)
            .assertNext(120.9357)
            .assertNext(74.8331)
            .assertNext(63.1906)
            .assertNext(31.4270)
            .assertNext(36.3766)
    }
}
