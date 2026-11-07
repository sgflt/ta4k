/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class SigmaIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun test(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 15.0, 8.0, 23.0, 9.0, 5.0, 1.0)

        val zScore = SigmaIndicator(closePrice(), 5)

        context.withIndicator(zScore)
            .fastForwardUntilStable()
            .assertCurrent(1.2649)
            .assertNext(1.2649)
            .assertNext(1.2649)
            .assertNext(1.2649)
            .assertNext(1.2649)
            .assertNext(1.2649)
            .assertNext(1.6696)
            .assertNext(-0.686)
            .assertNext(1.6116)
            .assertNext(-0.6447)
            .assertNext(-0.9802)
            .assertNext(-0.9857)
    }
}
