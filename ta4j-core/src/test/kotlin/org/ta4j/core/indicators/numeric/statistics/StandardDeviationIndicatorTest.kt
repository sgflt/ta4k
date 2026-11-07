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
}