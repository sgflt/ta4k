/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class DifferenceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveBarDifference(numFactory: NumFactory) {
        val closePriceIndicator = Indicators.closePrice()
        val closePriceDifference = DifferenceIndicator(closePriceIndicator)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 15.0, 14.0, 13.0, 16.0, 18.0, 17.0, 19.0)
            .withIndicator(closePriceDifference)
            .assertNextNaN()  // First value is NaN (no previous value)
            .assertNext(1.0)  // 11 - 10 = 1
            .assertNext(1.0)  // 12 - 11 = 1
            .assertNext(3.0)  // 15 - 12 = 3
            .assertNext(-1.0) // 14 - 15 = -1
            .assertNext(-1.0) // 13 - 14 = -1
            .assertNext(3.0)  // 16 - 13 = 3
            .assertNext(2.0)  // 18 - 16 = 2
            .assertNext(-1.0) // 17 - 18 = -1
    }
}