/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class ROCIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateRateOfChange(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0, 2.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val roc = closePrice.roc(3)

        context.withIndicator(roc)

        // First 3 bars, indicator is unstable
        TestUtils.assertUnstable(roc)
        context.fastForwardUntilStable()

        // 4th bar: (4.0 - 1.0) / 1.0 * 100 = 300.0
        context.assertCurrent(300.0)

        // 5th bar: (3.0 - 2.0) / 2.0 * 100 = 50.0
        context.assertNext(50.0)

        // 6th bar: (2.0 - 3.0) / 3.0 * 100 = -33.333...
        context.assertNext(-33.333333333333336)

        // 7th bar: (1.0 - 4.0) / 4.0 * 100 = -75.0
        context.assertNext(-75.0)

        // 8th bar: (2.0 - 3.0) / 3.0 * 100 = -33.333...
        context.assertNext(-33.333333333333336)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testRocWithZeroValue(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 2.0, 3.0, 4.0, 5.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val roc = closePrice.roc(3)

        context.withIndicator(roc)

        // First 3 bars, indicator is unstable
        TestUtils.assertUnstable(roc)
        context.fastForwardUntilStable()

        // 4th bar: (4.0 - 0.0) / 0.0 * 100 = NaN
        // Division by zero should result in NaN
        context.assertCurrentNaN()

        // 5th bar: (5.0 - 2.0) / 2.0 * 100 = 150.0
        context.assertNext(150.0)
    }
}
