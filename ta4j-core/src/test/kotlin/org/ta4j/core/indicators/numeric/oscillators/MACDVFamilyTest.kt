/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

internal class MACDVFamilyTest {

    private val closes = listOf(10.0, 11.0, 10.5, 12.0, 13.0, 12.5, 14.0, 15.5, 15.0, 17.0)
    private val volumes = listOf(100.0, 150.0, 80.0, 200.0, 120.0, 90.0, 210.0, 180.0, 60.0, 250.0)

    // range of 2 around each close, so ATR is never zero
    private val events = closes.indices.map { i ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = closes[i],
            highPrice = closes[i] + 1,
            lowPrice = closes[i] - 1,
            closePrice = closes[i],
            volume = volumes[i],
        )
    }

    private fun context(numFactory: NumFactory) = MarketEventTestContext()
        .withNumFactory(numFactory)
        .withMarketEvents(events)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun volatilityNormalizedMacdDividesSpreadByAtr(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val macdV = factory.volatilityNormalizedMacd(2, 3, 2)
        val close = factory.closePrice()
        val expected = close.ema(2).minus(close.ema(3)).dividedBy(factory.atr(2)).multipliedBy(100)
        val state = macdV.momentumState()
        val context = context(numFactory).withIndicators(macdV, expected, state)

        repeat(closes.size) {
            context.advance()
            assertNumEquals(expected.value, macdV.value)
        }
        assertThat(macdV.isStable).isTrue()
        assertThat(state.value).isEqualTo(MACDVMomentumProfile().classify(macdV.value))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun macdVUsesVolumeOverAtrWeights(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val macdV = factory.macdV(2, 3)
        val close = factory.closePrice()
        val volume = factory.volume()
        val shortWeight = volume.dividedBy(factory.atr(2))
        val longWeight = volume.dividedBy(factory.atr(3))
        val expected = close.multipliedBy(shortWeight).ema(2).dividedBy(shortWeight.ema(2))
            .minus(close.multipliedBy(longWeight).ema(3).dividedBy(longWeight.ema(3)))
        val context = context(numFactory).withIndicators(macdV, expected)

        context.fastForward(closes.size)
        assertNumEquals(expected.value, macdV.value)
        // rising closes keep the short average above the long one
        assertThat(macdV.value.isPositive).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun histogramIsSpreadMinusSignal(numFactory: NumFactory) {
        val macd = Indicators.extended(numFactory).macd(2, 3)
        val histogram = macd.histogram(3)
        val signal = macd.signalLine(3)
        context(numFactory).withIndicators(histogram, signal).fastForward(closes.size)
        assertNumEquals(macd.value - signal.value, histogram.value)
    }

    @Test
    fun momentumZones() {
        val profile = MACDVMomentumProfile()
        val f = DoubleNumFactory
        assertThat(profile.classify(f.numOf(151))).isEqualTo(MACDVMomentumState.HIGH_RISK)
        assertThat(profile.classify(f.numOf(150))).isEqualTo(MACDVMomentumState.RALLYING_OR_RETRACING)
        assertThat(profile.classify(f.numOf(50))).isEqualTo(MACDVMomentumState.RALLYING_OR_RETRACING)
        assertThat(profile.classify(f.numOf(0))).isEqualTo(MACDVMomentumState.RANGING)
        assertThat(profile.classify(f.numOf(-50))).isEqualTo(MACDVMomentumState.RANGING)
        assertThat(profile.classify(f.numOf(-150))).isEqualTo(MACDVMomentumState.REBOUNDING_OR_REVERSING)
        assertThat(profile.classify(f.numOf(-151))).isEqualTo(MACDVMomentumState.LOW_RISK)
        assertThat(profile.classify(NaN)).isEqualTo(MACDVMomentumState.UNDEFINED)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stateIsUndefinedWhileUnstable(numFactory: NumFactory) {
        val state = Indicators.extended(numFactory).volatilityNormalizedMacd(2, 3, 2).momentumState()
        val inRange = state.isIn(MACDVMomentumState.RANGING)
        context(numFactory).withIndicators(state, inRange).advance()
        assertThat(state.value).isEqualTo(MACDVMomentumState.UNDEFINED)
        assertThat(inRange.value).isFalse()
    }
}
