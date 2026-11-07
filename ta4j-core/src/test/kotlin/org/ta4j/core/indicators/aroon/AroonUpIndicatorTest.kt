/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.aroon

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class AroonUpIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun init() {
        context = MarketEventTestContext()
        // barcount before
        context.withCandlePrices(
            169.87,
            169.36,
            169.29,
            168.38,
            167.70,
            168.43,
            170.18,
            172.15,
            172.92,
            172.39,
            172.48,
            173.31,
            173.49,
            173.89,
            174.17,
            173.17,
            172.28,
            172.34,
            172.07,
            172.56
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun upAndSlowDown(numFactory: NumFactory) {
        val arronUp = Indicators.aroonUp(5)

        context.withNumFactory(numFactory)
            .withIndicators(arronUp)
            .fastForwardUntilStable()
            .assertCurrent(0.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(80.0)
            .assertNext(60.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(80.0)
            .assertNext(60.0)
            .assertNext(40.0)
            .assertNext(20.0)
            .assertNext(0.0)
    }
}