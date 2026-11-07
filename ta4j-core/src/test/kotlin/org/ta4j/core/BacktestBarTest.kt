/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import java.time.ZoneId
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.backtest.BacktestBar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

class BacktestBarTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun addTrades(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,  // Will be set on first trade
            highPrice = NaN,  // Will be updated with trades
            lowPrice = NaN,   // Will be updated with trades
            closePrice = NaN, // Will be updated with trades
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        bar.addTrade(numFactory.numOf(3.0), numFactory.numOf(200.0))
        bar.addTrade(numFactory.numOf(4.0), numFactory.numOf(201.0))
        bar.addTrade(numFactory.numOf(2.0), numFactory.numOf(198.0))

        assertThat(bar.trades).isEqualTo(3)
        assertThat(bar.amount).isEqualTo(numFactory.numOf(3 * 200 + 4 * 201 + 2 * 198))
        assertThat(bar.openPrice).isEqualTo(numFactory.numOf(200))
        assertThat(bar.closePrice).isEqualTo(numFactory.numOf(198))
        assertThat(bar.lowPrice).isEqualTo(numFactory.numOf(198))
        assertThat(bar.highPrice).isEqualTo(numFactory.numOf(201))
        assertThat(bar.volume).isEqualTo(numFactory.numOf(9))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun timePeriod(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        assertThat(bar.endTime.minus(bar.timePeriod)).isEqualTo(beginTime.toInstant())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun beginTime(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        assertThat(bar.beginTime).isEqualTo(beginTime.toInstant())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun inPeriod(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        assertThat(bar.inPeriod(null)).isFalse()

        assertThat(bar.inPeriod(beginTime.withDayOfMonth(24).toInstant())).isFalse()
        assertThat(bar.inPeriod(beginTime.withDayOfMonth(26).toInstant())).isFalse()
        assertThat(bar.inPeriod(beginTime.withMinute(30).toInstant())).isTrue()

        assertThat(bar.inPeriod(beginTime.toInstant())).isTrue()
        assertThat(bar.inPeriod(endTime.toInstant())).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun equals(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar1 = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        val bar2 = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        assertThat(bar1).isEqualTo(bar2)
        assertThat(bar1).isNotSameAs(bar2)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun hashCode2(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault())
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault())

        val bar1 = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        val bar2 = BacktestBar(
            timeFrame = TimeFrame.DAY,
            beginTime = beginTime.toInstant(),
            endTime = endTime.toInstant(),
            openPrice = NaN,
            highPrice = NaN,
            lowPrice = NaN,
            closePrice = NaN,
            volume = numFactory.zero(),
            amount = numFactory.zero(),
            trades = 0
        )

        assertThat(bar1.hashCode()).isEqualTo(bar2.hashCode())
    }
}
