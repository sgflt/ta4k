/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.bool.fractal.FractalHighIndicator
import org.ta4j.core.indicators.bool.fractal.FractalLowIndicator
import org.ta4j.core.num.NumFactory

internal class TrendLineTest {

    // swing lows (one bar each side): bar 1 = 10, bar 3 = 12, bar 5 = 13, confirmed one bar later
    private val risingLows = doubleArrayOf(12.0, 10.0, 13.0, 12.0, 14.0, 13.0, 15.0, 16.0)

    private fun support(numFactory: NumFactory, lookback: Int) = TrendLineSupportIndicator(
        FractalLowIndicator(Indicators.extended(numFactory).lowPrice(), 1, 1),
        lookback,
    )

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun supportLineIsPublishedPerBarWithoutRedrawingThePast(numFactory: NumFactory) {
        val line = support(numFactory, 8)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*risingLows)
            .withIndicator(line)

        val values = mutableListOf<Double>()
        repeat(risingLows.size) {
            context.advance()
            values += line.value.doubleValue()
        }
        assertThat(values.take(4)).allMatch { it.isNaN() }
        // through 10 and 12 (slope 1) while those are the only swings
        assertThat(values[4]).isCloseTo(13.0, org.assertj.core.data.Offset.offset(1e-9))
        assertThat(values[5]).isCloseTo(14.0, org.assertj.core.data.Offset.offset(1e-9))
        // the swing at 13 rules out lines through bar 3: 10 -> 13 (slope 0.75) is the only one left
        assertThat(values[6]).isCloseTo(13.75, org.assertj.core.data.Offset.offset(1e-9))
        assertThat(values[7]).isCloseTo(14.5, org.assertj.core.data.Offset.offset(1e-9))
        assertNumEquals(0.75, line.slope)
        assertThat(line.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun swingsLeaveTheWindow(numFactory: NumFactory) {
        val line = support(numFactory, 4)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*risingLows)
            .withIndicator(line)

        context.fastForward(7)
        // window bars 3..6 holds swings 12 and 13
        assertNumEquals(13.5, line.value)
        context.advance()
        // bar 3 left the window, one swing is not a line
        assertThat(line.value.isNaN).isTrue()
        assertThat(line.isStable).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun resistanceLineThroughFallingHighs(numFactory: NumFactory) {
        // swing highs 15, 14, 13 on bars 1, 3, 5 all lie on one line
        val line = TrendLineResistanceIndicator(
            FractalHighIndicator(Indicators.extended(numFactory).highPrice(), 1, 1),
            10,
        )
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 12.0, 14.0, 11.0, 13.0, 9.0)
            .withIndicator(line)
            .fastForward(7)
        assertNumEquals(-0.5, line.slope) // one point every two bars
        assertNumEquals(12.5, line.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fluentFactoryBuildsBothLines(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val support = factory.trendLineSupport(lookback = 8, surroundingBars = 1)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*risingLows)
            .withIndicators(support, factory.trendLineResistance(8, 1))
            .fastForward(risingLows.size)
        assertNumEquals(14.5, support.value)
    }
}
