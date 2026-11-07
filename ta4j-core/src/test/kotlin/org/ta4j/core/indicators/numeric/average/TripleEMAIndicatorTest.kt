/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class TripleEMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun tripleEMAUsingBarCount5UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                0.73, 0.72, 0.86, 0.72, 0.62, 0.76, 0.84, 0.69, 0.65, 0.71, 0.53, 0.73, 0.77, 0.67, 0.68
            )

        val tripleEma = TripleEMAIndicator(closePrice(), 5)

        context.withIndicator(tripleEma)
            .assertNext(0.73)
            .assertNext(0.7229)
            .assertNext(0.8185)
            .fastForward(4)
            .assertCurrent(0.8027)
            .assertNext(0.7328)
            .assertNext(0.6725)
            .fastForward(4)
            .assertCurrent(0.7386)
            .assertNext(0.6994)
            .assertNext(0.6876)
    }
}
