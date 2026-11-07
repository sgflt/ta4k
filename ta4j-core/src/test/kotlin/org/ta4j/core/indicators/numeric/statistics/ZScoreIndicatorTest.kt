/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import kotlin.math.sqrt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class ZScoreIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `z score of price against rolling mean and sample deviation`(numFactory: NumFactory) {
        val zScore = Indicators.extended(numFactory).closePrice().zScore(3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 13.0, 13.0, 13.0, 13.0, 13.0)
            .withIndicator(zScore)

        context.advance()
        context.advance()
        context.advance() // window 10, 10, 13: mean 11, sample sd sqrt(3)
        assertNumEquals(2.0 / sqrt(3.0), zScore.value)
        context.advance() // window 10, 13, 13: mean 12, sample sd sqrt(3)
        assertNumEquals(1.0 / sqrt(3.0), zScore.value)
        context.advance() // constant window, sd 0
        assertThat(zScore.value.isNaN).isTrue()

        context.fastForwardToTheEnd()
        assertThat(zScore.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `z score divides custom deviation by custom standard deviation`(numFactory: NumFactory) {
        val zScore = ZScoreIndicator(
            ConstantNumericIndicator(numFactory.numOf(-6)),
            ConstantNumericIndicator(numFactory.numOf(4))
        )

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0)
            .withIndicator(zScore)
            .advance()

        assertNumEquals(-1.5, zScore.value)
    }
}
