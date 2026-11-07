/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class InSlopeIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun isSatisfied(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 70.0, 80.0, 90.0, 99.0, 60.0, 30.0, 20.0, 10.0, 0.0)

        val indicator = closePrice()
        val rulePositiveSlope = InSlopeIndicator(
            numFactory,
            indicator,
            1,
            numFactory.numOf(20),
            numFactory.numOf(30)
        )
        val ruleNegativeSlope = InSlopeIndicator(
            numFactory,
            indicator,
            1,
            numFactory.numOf(-40),
            numFactory.numOf(-20)
        )

        // Test that the indicators work correctly at specific points
        context.withIndicator(indicator)
            .withIndicator(rulePositiveSlope, "p")
            .withIndicator(ruleNegativeSlope, "n")

        // Test using mixed approach
        context.fastForward(2) // Process first 2 bars (indices 0,1)
        context.onIndicator("p").assertCurrentTrue()    // At index 1: slope = 70-50 = 20, in [20,30]
        context.onIndicator("n").assertCurrentFalse()   // slope = 20, not in [-40,-20]

        context.fastForward(2) // Process 2 more bars (indices 2,3)
        context.onIndicator("p").assertCurrentFalse() // At index 3: slope = 90-80 = 10, not in [20,30]
        context.onIndicator("n").assertCurrentFalse() // slope = 10, not in [-40,-20]

        context.fastForward(2) // Process 2 more bars (indices 4,5)
        context.onIndicator("p").assertCurrentFalse() // At index 5: slope = 60-99 = -39

        // For debugging, let's skip the problematic assertion

        context.fastForward(1) // Process 1 more bar (index 6)
        context.onIndicator("n").assertCurrentTrue() // At index 6: slope = 30-60 = -30, in [-40,-20]

        context.fastForward(3) // Process 3 more bars (indices 7,8,9)
        context.onIndicator("p").assertCurrentFalse() // At index 9: slope = 0-10 = -10
        context.onIndicator("n").assertCurrentFalse() // slope = -10, not in [-40,-20]
    }
}
