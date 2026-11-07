/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextKeys
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

internal class LossTriggeredCooldownRuleTest {

    private val start = Instant.parse("2026-11-02T10:00:00Z")
    private val exitTime = start.plus(Duration.ofMinutes(1))

    /** Trading record with a clock the test controls. */
    private class RecordWithClock(val record: BackTestTradingRecord) : RuntimeContext by record {
        var now: Instant = Instant.EPOCH

        override fun <T> getValue(resolver: RuntimeValueResolver<T?>): T? = resolver.resolve(this)

        override fun getValue(key: String): Any? =
            if (key == RuntimeContextKeys.CURRENT_TIME) now else record.getValue(key)
    }

    private fun contextWithClosedPosition(numFactory: NumFactory, entry: Double, exit: Double): RecordWithClock {
        val record = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)
        record.enter(start, numFactory.numOf(entry), numFactory.one())
        record.exit(exitTime, numFactory.numOf(exit), numFactory.one())
        return RecordWithClock(record)
    }

    private fun RecordWithClock.minutesAfterExit(minutes: Long) = apply { now = exitTime.plus(Duration.ofMinutes(minutes)) }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun blocksEntriesUntilCooldownElapsedAfterLoss(numFactory: NumFactory) {
        val context = contextWithClosedPosition(numFactory, 100.0, 90.0)
        val rule = Rule.cooldownAfterLoss(3, TimeFrame.MINUTES_1, context)

        assertThat(rule.isSatisfied).isFalse() // clock before exit
        assertThat(context.minutesAfterExit(0).let { rule.isSatisfied }).isFalse()
        assertThat(context.minutesAfterExit(2).let { rule.isSatisfied }).isFalse()
        assertThat(context.minutesAfterExit(3).let { rule.isSatisfied }).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun allowsEntriesAfterProfit(numFactory: NumFactory) {
        val context = contextWithClosedPosition(numFactory, 100.0, 110.0).minutesAfterExit(0)
        assertThat(LossTriggeredCooldownRule(3, TimeFrame.MINUTES_1, context).isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun allowsEntriesWithoutClosedPositions(numFactory: NumFactory) {
        val context = RecordWithClock(BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory))
        assertThat(LossTriggeredCooldownRule(3, TimeFrame.MINUTES_1, context).isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun resetRuleEndsCooldownEarly(numFactory: NumFactory) {
        val context = contextWithClosedPosition(numFactory, 100.0, 90.0).minutesAfterExit(1)
        val rule = LossTriggeredCooldownRule(3, TimeFrame.MINUTES_1, context, resetRule = BooleanRule.TRUE)
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun ignoresLossesOfOtherTradeType(numFactory: NumFactory) {
        val context = contextWithClosedPosition(numFactory, 100.0, 90.0).minutesAfterExit(1)
        val shortsOnly = LossTriggeredCooldownRule(3, TimeFrame.MINUTES_1, context, tradeType = TradeType.SELL)
        val longsOnly = LossTriggeredCooldownRule(3, TimeFrame.MINUTES_1, context, tradeType = TradeType.BUY)
        assertThat(shortsOnly.isSatisfied).isTrue()
        assertThat(longsOnly.isSatisfied).isFalse()
    }
}
