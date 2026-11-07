/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class TrailingStopGainRuleTest {

    private fun setUp(numFactory: NumFactory, vararg prices: Double): Triple<MarketEventTestContext, TrailingStopGainRule, PositionRuntimeContext> {
        val runtimeContext = PositionRuntimeContext(null)
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)
            .withIndicator(closePrice)
        return Triple(context, TrailingStopGainRule(closePrice, 10.0, runtimeContext), runtimeContext)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should lock in gain for BUY position after activation`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 110.0, 120.0, 130.0, 117.0, 130.0, 116.99)

        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isFalse()

        positions.enter(numFactory, 114.0, TradeType.BUY) // activation at 125.4
        context.advance() // 120, not armed
        assertThat(rule.isSatisfied).isFalse()
        assertThat(rule.stopPrice).isNull()
        context.advance() // 130 armed -> stop 117
        assertThat(rule.isSatisfied).isFalse()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(117))
        context.advance() // 117
        assertThat(rule.isSatisfied).isTrue()

        context.advance() // 130
        positions.exit(130.0)
        positions.enter(numFactory, 128.0, TradeType.BUY) // activation at 140.8
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 116.99, never armed
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger before activation for BUY position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 104.0, 96.0, 93.0)
        positions.enter(numFactory, 100.0, TradeType.BUY)

        while (context.advance()) {
            assertThat(rule.isSatisfied).isFalse()
            assertThat(rule.stopPrice).isNull()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should lock in gain for SELL position after activation`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 90.0, 80.0, 70.0, 77.0, 120.0, 132.01)

        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isFalse()

        positions.enter(numFactory, 84.0, TradeType.SELL) // activation at 75.6
        context.advance() // 80, not armed
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 70 armed -> stop 77
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 77
        assertThat(rule.isSatisfied).isTrue()

        context.advance() // 120
        positions.exit(120.0)
        positions.enter(numFactory, 128.0, TradeType.SELL) // activation at 115.2
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 132.01, never armed
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger before activation for SELL position`(numFactory: NumFactory) {
        val (context, rule, positions) = setUp(numFactory, 100.0, 96.0, 104.0, 107.0)
        positions.enter(numFactory, 100.0, TradeType.SELL)

        while (context.advance()) {
            assertThat(rule.isSatisfied).isFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fluent api creates trailing stop gain`(numFactory: NumFactory) {
        val runtimeContext = PositionRuntimeContext.withOpenPosition(numFactory, 100.0)
        val rule = Indicators.extended(numFactory).closePrice().trailingStopGainRule(10, runtimeContext)

        assertThat(rule).isInstanceOf(TrailingStopGainRule::class.java)
        assertThat(rule.gainPercentage).isEqualTo(numFactory.numOf(10))
    }
}
