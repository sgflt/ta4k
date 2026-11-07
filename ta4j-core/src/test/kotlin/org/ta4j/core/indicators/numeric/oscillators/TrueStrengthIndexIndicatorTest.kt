/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class TrueStrengthIndexIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculatesTrueStrengthIndex(numFactory: NumFactory) {
        // last four values match upstream ta4j's reference test
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 2.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().tsi(4, 2))
            .assertNextNaN()
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(46.666666666666664)
            .assertNext(-3.111111111111111)
            .assertNext(-38.903703703703705)
            .assertNext(-62.35456790123457)
            .assertIsStable()
            .assertNext(-23.750189300411524)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun unstableDuringWarmUp(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().tsi(4, 2))
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(100.0)
            .assertIsUnStable()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun flatPricesGiveNaN(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().tsi(2, 2))
            .fastForward(5)
            .assertCurrentNaN()
    }

    @Test
    fun rejectsNonPositivePeriods() {
        assertThatThrownBy { Indicators.closePrice().tsi(0, 13) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { Indicators.closePrice().tsi(25, 0) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
