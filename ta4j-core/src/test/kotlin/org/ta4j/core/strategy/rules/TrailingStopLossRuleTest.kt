/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class TrailingStopLossRuleTest {

    private fun setUp(numFactory: NumFactory, vararg prices: Double): Triple<MarketEventTestContext, TrailingStopLossRule, PositionRuntimeContext> {
        val runtimeContext = PositionRuntimeContext(null)
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)
            .withIndicator(closePrice)
        return Triple(context, TrailingStopLossRule(closePrice, 10.0, runtimeContext), runtimeContext)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trail highest price for BUY position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 110.0, 120.0, 130.0, 117.0, 130.0, 116.99)

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // no position
        context.advance()
        assertThat(rule.isSatisfied).isFalse()

        positions.enter(numFactory, 114.0, TradeType.BUY)
        context.advance() // 120 -> stop 108
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 130 -> stop 117
        assertThat(rule.isSatisfied).isFalse()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(117))
        context.advance() // 117 reaches stop
        assertThat(rule.isSatisfied).isTrue()

        context.advance() // 130
        positions.exit(130.0)
        positions.enter(numFactory, 128.0, TradeType.BUY)
        assertThat(rule.isSatisfied).isFalse() // best 130 -> stop 117
        context.advance() // 116.99
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trail lowest price for SELL position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 90.0, 80.0, 70.0, 77.0, 120.0, 132.01)

        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isFalse()

        positions.enter(numFactory, 84.0, TradeType.SELL)
        context.advance() // 80 -> stop 88
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 70 -> stop 77
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 77 reaches stop
        assertThat(rule.isSatisfied).isTrue()

        context.advance() // 120
        positions.exit(120.0)
        positions.enter(numFactory, 128.0, TradeType.SELL)
        assertThat(rule.isSatisfied).isFalse() // best 120 -> stop 132
        context.advance() // 132.01
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `stop only moves in favour of the position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 110.0, 105.0)
        positions.enter(numFactory, 100.0, TradeType.BUY)

        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(90))
        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(99))
        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(99))
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `tracking starts at entry price and resets for a new position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 95.0, 130.0, 100.0)

        positions.enter(numFactory, 100.0, TradeType.BUY)
        context.advance() // 95 is below entry, best stays 100
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(90))
        context.advance() // best 130
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(117))

        positions.exit(130.0)
        positions.enter(numFactory, 100.0, TradeType.BUY)
        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(90))
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fluent api creates trailing stop loss`(numFactory: NumFactory) {
        val runtimeContext = PositionRuntimeContext.withOpenPosition(numFactory, 100.0)
        val rule = Indicators.extended(numFactory).closePrice().trailingStopLossRule(5, runtimeContext)

        assertThat(rule).isInstanceOf(TrailingStopLossRule::class.java)
        assertThat(rule.lossPercentage).isEqualTo(numFactory.numOf(5))
        assertThat(rule.toString()).contains("TrailingStopLossRule[5.0%]")
    }
}
