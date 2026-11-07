/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.billwilliams

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

internal class BillWilliamsIndicatorsTest {

    private val swings = doubleArrayOf(1.0, 2.0, 5.0, 3.0, 2.0, 4.0, 6.0, 3.0, 1.0)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fractalsConfirmTwoBarsAfterPivot(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val high = factory.fractalHigh()
        val low = factory.fractalLow()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*swings)
            .withIndicators(high, low)

        val highs = mutableListOf<Boolean>()
        val lows = mutableListOf<Boolean>()
        repeat(swings.size) {
            context.advance()
            highs += high.value
            lows += low.value
        }
        // pivot 5 (bar 3) confirmed on bar 5, pivot 6 (bar 7) on bar 9; pivot low 2 (bar 5) on bar 7
        assertThat(highs).containsExactly(false, false, false, false, true, false, false, false, true)
        assertThat(lows).containsExactly(false, false, false, false, false, false, true, false, false)
        assertNumEquals(6.0, high.pivotPrice)
        assertThat(low.pivotPrice.isNaN).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun recentSwingHighKeepsLastPivot(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*swings)
            .withIndicator(Indicators.extended(numFactory).recentSwingHigh())
            .fastForward(4)
            .assertCurrentNaN()
            .assertIsUnStable()
            .assertNext(5.0)
            .assertIsStable()
            .fastForward(3)
            .assertCurrent(5.0)
            .assertNext(6.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun alligatorLinesAreDisplacedSmoothedAverages(numFactory: NumFactory) {
        val close = Indicators.extended(numFactory).closePrice()
        val alligator = AlligatorFacade(close, 4, 3, 3, 2, 2, 1)
        val jaw = close.mma(4).previous(3)
        val teeth = close.mma(3).previous(2)
        val lips = close.mma(2).previous(1)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*swings)
            .withIndicators(alligator.jaw, alligator.teeth, alligator.lips, alligator.upperGator, alligator.lowerGator)
            .withIndicators(jaw, teeth, lips)

        context.fastForward(swings.size)
        assertNumEquals(jaw.value, alligator.jaw.value)
        assertNumEquals(teeth.value, alligator.teeth.value)
        assertNumEquals(lips.value, alligator.lips.value)
        assertNumEquals((jaw.value - teeth.value).abs(), alligator.upperGator.value)
        assertNumEquals(-(teeth.value - lips.value).abs(), alligator.lowerGator.value)
        assertThat(alligator.lowerGator.value.isPositive).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun marketFacilitationIndexIsRangePerVolume(numFactory: NumFactory) {
        val events = listOf(1000.0, 0.0).mapIndexed { i, volume ->
            val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = begin,
                endTime = begin.plus(Duration.ofDays(1)),
                openPrice = 11.0,
                highPrice = 12.0,
                lowPrice = 10.0,
                closePrice = 11.0,
                volume = volume,
            )
        }
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(Indicators.extended(numFactory).marketFacilitationIndex())
            .assertNext(0.002)
            .assertNextNaN()
    }
}
