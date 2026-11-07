/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class StandardErrorIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun usingBarCount5UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 40.0, 40.0, 50.0, 40.0, 30.0, 20.0, 10.0)

        val se = StandardErrorIndicator(closePrice(), 5)

        context.withIndicator(se, "se")
            .fastForwardUntilStable()

        context.onIndicator("se")
            .assertNext(5.0990) // Index 5: Actual value: 5.0990195135927845
            .assertNext(3.1623) // Index 6: Actual value: 3.162277660168379
            .assertNext(2.4495) // Index 7: Actual value: 2.4494897427831783
            .assertNext(2.4495) // Index 8: Actual value: 2.4494897427831783
            .assertNext(3.1623) // Index 9: Actual value: 3.162277660168379
            .assertNext(5.0990) // Index 10: Actual value: 5.0990195135927845
            .assertNext(7.0711) // Index 11: Actual value: 7.071067811865475
    }
}
