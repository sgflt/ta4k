/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class LossIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate loss using close price correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 7.0, 4.0, 3.0, 3.0, 5.0, 3.0, 2.0)

        val lossIndicator = Indicators.extended(numFactory).closePrice().loss()

        context
            .withIndicator(lossIndicator)
            .assertNext(0.0)  // First value - no previous, so loss = 0
            .assertNext(0.0)  // 2 - 1 = 1 (gain, so loss = 0)
            .assertNext(0.0)  // 3 - 2 = 1 (gain, so loss = 0)
            .assertNext(0.0)  // 4 - 3 = 1 (gain, so loss = 0)
            .assertNext(1.0)  // 3 - 4 = -1 (loss = absolute value = 1)
            .assertNext(0.0)  // 4 - 3 = 1 (gain, so loss = 0)
            .assertNext(0.0)  // 7 - 4 = 3 (gain, so loss = 0)
            .assertNext(3.0)  // 4 - 7 = -3 (loss = absolute value = 3)
            .assertNext(1.0)  // 3 - 4 = -1 (loss = absolute value = 1)
            .assertNext(0.0)  // 3 - 3 = 0 (no change, so loss = 0)
            .assertNext(0.0)  // 5 - 3 = 2 (gain, so loss = 0)
            .assertNext(2.0)  // 3 - 5 = -2 (loss = absolute value = 2)
            .assertNext(1.0)  // 2 - 3 = -1 (loss = absolute value = 1)
    }
}
