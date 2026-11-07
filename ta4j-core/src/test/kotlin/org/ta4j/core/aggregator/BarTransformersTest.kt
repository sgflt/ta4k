/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.aggregator

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.series.Bar
import org.ta4j.core.events.TickReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

internal class BarTransformersTest {

    private fun bars(numFactory: NumFactory, vararg ohlcv: List<Double>): List<Bar> = ohlcv.mapIndexed { i, v ->
        val begin = Instant.EPOCH.plus(Duration.ofMinutes(i.toLong()))
        AggregatedBar(
            timeFrame = TimeFrame.MINUTES_1,
            timePeriod = Duration.ofMinutes(1),
            beginTime = begin,
            endTime = begin.plus(Duration.ofMinutes(1)),
            openPrice = numFactory.numOf(v[0]),
            highPrice = numFactory.numOf(v[1]),
            lowPrice = numFactory.numOf(v[2]),
            closePrice = numFactory.numOf(v[3]),
            volume = numFactory.numOf(v[4]),
        )
    }

    private fun AbstractBarTransformer.collect(): MutableList<Bar> {
        val out = mutableListOf<Bar>()
        addBarListener { out += it }
        return out
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun tickBarsEveryNSourceBars(numFactory: NumFactory) {
        val aggregator = TickBarAggregator(numFactory, 2)
        val out = aggregator.collect()
        bars(
            numFactory,
            listOf(10.0, 12.0, 9.0, 11.0, 5.0),
            listOf(11.0, 14.0, 10.0, 13.0, 7.0),
            listOf(13.0, 13.0, 8.0, 9.0, 1.0),
        ).forEach(aggregator::onBar)

        assertThat(out).hasSize(1)
        val bar = out.single()
        assertNumEquals(10.0, bar.openPrice)
        assertNumEquals(14.0, bar.highPrice)
        assertNumEquals(9.0, bar.lowPrice)
        assertNumEquals(13.0, bar.closePrice)
        assertNumEquals(12.0, bar.volume)
        assertThat(bar.beginTime).isEqualTo(Instant.EPOCH)
        assertThat(bar.endTime).isEqualTo(Instant.EPOCH.plus(Duration.ofMinutes(2)))
        assertThat(bar.timeFrame).isEqualTo(TimeFrame("tick-2"))
    }

    @Test
    fun tickBarsFromTicksUseMidPrice() {
        val aggregator = TickBarAggregator(DoubleNumFactory, 3)
        val out = aggregator.collect()
        listOf(100.0 to 99.0, 102.0 to 101.0, 98.0 to 97.0).forEachIndexed { i, (ask, bid) ->
            aggregator.onTick(TickReceived(Instant.EPOCH.plusSeconds(i.toLong()), ask, bid))
        }
        val bar = out.single()
        assertNumEquals(99.5, bar.openPrice)
        assertNumEquals(101.5, bar.highPrice)
        assertNumEquals(97.5, bar.lowPrice)
        assertNumEquals(97.5, bar.closePrice)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun volumeBarsCapAtThresholdAndCarryRemainder(numFactory: NumFactory) {
        val aggregator = VolumeBarAggregator(numFactory, 100)
        val out = aggregator.collect()
        bars(
            numFactory,
            listOf(10.0, 11.0, 9.0, 10.0, 40.0),
            listOf(10.0, 12.0, 10.0, 11.0, 50.0),
            listOf(11.0, 13.0, 11.0, 12.0, 30.0), // 120: closes at 100, carries 20
            listOf(12.0, 15.0, 12.0, 14.0, 250.0), // 270: two bars of 100, carries 70
        ).forEach(aggregator::onBar)

        assertThat(out).hasSize(3)
        out.forEach { assertNumEquals(100.0, it.volume) }
        assertNumEquals(10.0, out[0].openPrice)
        assertNumEquals(12.0, out[0].closePrice)
        assertNumEquals(12.0, out[1].openPrice) // carried remainder opens at the overshooting close
        assertNumEquals(14.0, out[1].closePrice)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun amountBarsUseCloseTimesVolume(numFactory: NumFactory) {
        val aggregator = AmountBarAggregator(numFactory, 1000)
        val out = aggregator.collect()
        bars(
            numFactory,
            listOf(10.0, 10.0, 10.0, 10.0, 60.0), // 600
            listOf(10.0, 10.0, 10.0, 10.0, 40.0), // 1000
        ).forEach(aggregator::onBar)
        assertThat(out).hasSize(1)
        assertNumEquals(100.0, out.single().volume)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun heikinAshiSmoothsBars(numFactory: NumFactory) {
        val transformer = HeikinAshiBarTransformer()
        val out = transformer.collect()
        bars(
            numFactory,
            listOf(10.0, 12.0, 9.0, 11.0, 1.0),
            listOf(11.0, 13.0, 10.0, 12.0, 1.0),
        ).forEach(transformer::onBar)

        assertNumEquals(10.5, out[0].openPrice)
        assertNumEquals(10.5, out[0].closePrice)
        assertNumEquals(12.0, out[0].highPrice)
        assertNumEquals(9.0, out[0].lowPrice)
        assertNumEquals(10.5, out[1].openPrice)
        assertNumEquals(11.5, out[1].closePrice)
        assertNumEquals(13.0, out[1].highPrice)
        assertNumEquals(10.0, out[1].lowPrice)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun transformersChain(numFactory: NumFactory) {
        val ticks = TickBarAggregator(numFactory, 2)
        val out = ticks.andThen(HeikinAshiBarTransformer()).collect()
        bars(
            numFactory,
            listOf(10.0, 12.0, 9.0, 11.0, 1.0),
            listOf(11.0, 13.0, 10.0, 12.0, 1.0),
        ).forEach(ticks::onBar)
        // tick bar O10 H13 L9 C12 -> HA close 11, open 11
        assertNumEquals(11.0, out.single().closePrice)
        assertNumEquals(11.0, out.single().openPrice)
    }

    @Test
    fun rejectsNonPositiveThreshold() {
        assertThatThrownBy { VolumeBarAggregator(DoubleNumFactory, 0) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
