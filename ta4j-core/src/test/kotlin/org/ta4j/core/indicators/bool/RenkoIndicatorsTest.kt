/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class RenkoIndicatorsTest {

    // bricks of 1: start 10, +2 bricks, +1 brick, then -3 bricks
    private val closes = doubleArrayOf(10.0, 12.2, 13.0, 12.5, 10.0)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun renkoUpCountsBullishBricks(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*closes)
            .withIndicator(Indicators.extended(numFactory).closePrice().renkoUp(1, 3))
            .assertNextFalse()
            .assertNextFalse() // 2 bricks
            .assertNextTrue() // 3 bricks
            .assertNextTrue() // no new brick
            .assertNextFalse() // reversal resets
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun renkoDownAndXCountBearishBricks(numFactory: NumFactory) {
        val close = Indicators.extended(numFactory).closePrice()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*closes)
            .withIndicator(close.renkoDown(1, 3), "down")
            .withIndicator(close.renkoX(1, 3), "x")
            .apply { fastForward(3) }
            .apply { onIndicator("down").assertCurrentFalse(); onIndicator("x").assertCurrentTrue() }
            .apply { fastForward(2) }
            .apply { onIndicator("down").assertCurrentTrue(); onIndicator("x").assertCurrentTrue() }
    }
}
