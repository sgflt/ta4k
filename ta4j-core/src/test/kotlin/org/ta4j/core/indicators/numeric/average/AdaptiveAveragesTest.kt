/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class AdaptiveAveragesTest {

    private val closes = doubleArrayOf(10.0, 11.0, 12.0, 11.0, 13.0, 14.0)

    private fun context(numFactory: NumFactory) =
        MarketEventTestContext().withNumFactory(numFactory).withCandlePrices(*closes)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun ewmaUsesExplicitDecay(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).closePrice().ewma(3, 0.8))
            .assertNext(10.0)
            .assertNext(10.2)
            .assertNext(10.56)
            .assertNext(10.648)
            .assertNext(11.1184)
            .assertNext(11.69472)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun mcGinleyDynamic(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).closePrice().mcGinley(3))
            .assertNext(10.0)
            .assertNext(10.227671151788357)
            .assertIsUnStable()
            .assertNext(10.539420167416202)
            .assertIsStable()
            .assertNext(10.668803935914045)
            .assertNext(11.021294157177447)
            .assertNext(11.402644898652527)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun vidyaFollowsCmo(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).closePrice().vidya(3))
            .assertNext(10.0)
            .assertNext(11.0)
            .assertNext(12.0)
            .assertNext(11.666666666666666)
            .assertNext(12.333333333333332)
            .assertNext(13.166666666666666)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun displacedAveragesLagTheirAverage(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).closePrice().dma(2, 1))
            .fastForward(3)
            .assertCurrent(10.5) // after closes 10, 11, 12: SMA(2) one bar earlier is (10 + 11) / 2
    }

    @Test
    fun ewmaRejectsInvalidDecay() {
        assertThatThrownBy { Indicators.closePrice().ewma(3, 1.0) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
