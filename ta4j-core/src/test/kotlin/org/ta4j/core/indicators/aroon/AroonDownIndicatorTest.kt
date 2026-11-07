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

class AroonDownIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun init() {
        context = MarketEventTestContext()
        // barcount before
        context.withCandlePrices(
            167.15,  // 0 -> unstable
            168.20,  // 1 -> unstable
            166.41,  // 0 -> unstable
            166.18,  // 0 -> unstable
            166.33,  // 1 -> unstable
            165.00,  // 0 -> (5 - 0) / 5 * 100 = 100
            167.63,  // 1 -> (5 - 1) / 5 * 100 = 80
            171.97,  // 2 -> (5 - 2) / 5 * 100 = 60
            171.31,  // 3 -> (5 - 3) / 5 * 100 = 40
            169.55,  // 4 -> (5 - 4) / 5 * 100 = 20
            169.57,  // 5 -> (5 - 5) / 5 * 100 = 0
            170.27,
            170.80,
            172.20,
            175.00,
            172.06,
            170.50,
            170.26,
            169.34,
            170.36
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun upDownAndHigh(numFactory: NumFactory) {
        val aroonDownIndicator = Indicators.aroonDown(5)
        context.withIndicator(aroonDownIndicator)
            .withNumFactory(numFactory)
            .fastForwardUntilStable()
            .assertCurrent(100.0)
            .assertNext(80.0)
            .assertNext(60.0)
            .assertNext(40.0)
            .assertNext(20.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(40.0)
            .assertNext(20.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(80.0)
    }
}