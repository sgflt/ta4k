/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class CMOIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CMO indicator correctly with 9 bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                21.27, 22.19, 22.08, 22.47, 22.48, 22.53, 22.23, 21.43, 21.24, 21.29,
                22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 24.75, 24.83, 23.95, 23.63,
                23.82, 23.87, 23.15, 23.19, 23.10, 22.65, 22.48, 22.87, 22.93, 22.91
            )
            .withIndicator(Indicators.extended(numFactory).closePrice().cmo(9))

        // Fast forward to when CMO becomes stable (after 9 bars)
        context.fastForward(5)
            .assertNext(85.1351)
            .assertNext(53.9326)
            .assertNext(6.2016)
            .assertNext(-1.083)
            .assertNext(0.7092)
            .assertNext(-1.4493)
            .assertNext(10.7266)
            .assertNext(-3.5857)
            .assertNext(4.7619)
            .assertNext(24.1983)
            .assertNext(47.644)
    }
}
