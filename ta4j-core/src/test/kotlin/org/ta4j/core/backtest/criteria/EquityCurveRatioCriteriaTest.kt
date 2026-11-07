/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.BacktestBar
import org.ta4j.core.backtest.criteria.ratio.Annualization
import org.ta4j.core.backtest.criteria.ratio.CalmarRatioCriterion
import org.ta4j.core.backtest.criteria.ratio.MaximumDrawdownBarLengthCriterion
import org.ta4j.core.backtest.criteria.ratio.OmegaRatioCriterion
import org.ta4j.core.backtest.criteria.ratio.SharpeRatioCriterion
import org.ta4j.core.backtest.criteria.ratio.SortinoRatioCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.trading.live.LiveTradingRecord

/**
 * Daily closes 100, 100, 110, 104.5, 104.5, 104.5; long from the close of bar 0 to the close of bar 3.
 * Equity: 1, 1, 1.1, 1.045, 1.045 (flat), 1.045 (flat); returns 0, 0.1, -0.05, 0, 0.
 */
internal class EquityCurveRatioCriteriaTest {

    private val start = Instant.parse("2026-11-02T00:00:00Z")

    private fun record(numFactory: NumFactory): BackTestTradingRecord {
        val record = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)
        listOf(100.0, 100.0, 110.0, 104.5, 104.5, 104.5).forEachIndexed { day, close ->
            val price = numFactory.numOf(close)
            val bar = BacktestBar(
                timeFrame = TimeFrame.DAY,
                beginTime = start.plus(Duration.ofDays(day.toLong())),
                endTime = start.plus(Duration.ofDays(day + 1L)),
                openPrice = price,
                highPrice = price,
                lowPrice = price,
                closePrice = price,
                volume = numFactory.one(),
                amount = price,
                trades = 1
            )
            record.onBar(bar)
            when (day) {
                0 -> record.enter(bar.endTime, price, numFactory.one())
                3 -> record.exit(bar.endTime, price, numFactory.one())
            }
        }
        return record
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun equityCurveIncludesFlatBars(numFactory: NumFactory) {
        val curve = record(numFactory).equityCurve
        assertThat(curve).hasSize(6)
        listOf(1.0, 1.0, 1.1, 1.045, 1.045, 1.045).forEachIndexed { i, expected ->
            assertNumEquals(expected, curve[i].equity)
        }
        assertThat(curve.map { it.invested }).containsExactly(false, true, true, true, false, false)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun sharpeRatio(numFactory: NumFactory) {
        val record = record(numFactory)
        assertNumEquals(0.18257418583505486, SharpeRatioCriterion(annualization = Annualization.PERIOD).calculate(record))
        assertNumEquals(3.4892692644735703, SharpeRatioCriterion().calculate(record))
        assertNumEquals(
            0.18013519350625956,
            SharpeRatioCriterion(annualRiskFreeRate = 0.05, annualization = Annualization.PERIOD).calculate(record)
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun sortinoRatio(numFactory: NumFactory) {
        val record = record(numFactory)
        assertNumEquals(0.44721359549995593, SortinoRatioCriterion(annualization = Annualization.PERIOD).calculate(record))
        assertNumEquals(8.5469292731366, SortinoRatioCriterion().calculate(record))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun omegaRatio(numFactory: NumFactory) {
        assertNumEquals(2.0, OmegaRatioCriterion().calculate(record(numFactory)))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calmarRatio(numFactory: NumFactory) {
        // CAGR of 4.5 % over 5 days, divided by the 5 % drawdown from 1.1 to 1.045
        assertNumEquals(478.2817864523336, CalmarRatioCriterion().calculate(record(numFactory)))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun maximumDrawdownBarLength(numFactory: NumFactory) {
        assertNumEquals(1, MaximumDrawdownBarLengthCriterion().calculate(record(numFactory)))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun emptyRecordGivesZero(numFactory: NumFactory) {
        val record = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)
        assertNumEquals(0, SharpeRatioCriterion().calculate(record))
        assertNumEquals(0, SortinoRatioCriterion().calculate(record))
        assertNumEquals(0, OmegaRatioCriterion().calculate(record))
        assertNumEquals(0, CalmarRatioCriterion().calculate(record))
        assertNumEquals(0, MaximumDrawdownBarLengthCriterion().calculate(record))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun liveRecordDoesNotKeepEquityCurveByDefault(numFactory: NumFactory) {
        assertThat(LiveTradingRecord(numFactory = numFactory).equityCurve).isEmpty()
    }
}
