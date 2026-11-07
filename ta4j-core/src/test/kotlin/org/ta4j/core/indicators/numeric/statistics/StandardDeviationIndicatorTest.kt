/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class StandardDeviationIndicatorTest {
    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 0.0, 9.0)
    }

    @ParameterizedTest(name = "STDEV [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun standardDeviationUsingBarCount4UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val sdv = Indicators.closePrice().stddev(4)

        testContext.withIndicator(sdv)
            .fastForwardUntilStable()
            .assertCurrent(1.291)
            .assertNext(0.81649)
            .assertNext(0.57735)
            .assertNext(0.81649)
            .assertNext(0.81649)
            .assertNext(0.81649)
            .assertNext(2.1602)
            .assertNext(3.7416)
    }

    @ParameterizedTest(name = "Flat window gives zero [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun flatWindowAfterSwingsGivesZero(numFactory: NumFactory) {
        // removing the swings from the rolling sums must not leave a negative variance (NaN after sqrt)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 14.0, 10.0, 14.0, 12.0, 12.0, 12.0, 12.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().stddev(3))
            .fastForward(8)
            .assertCurrent(0.0)
    }
}
