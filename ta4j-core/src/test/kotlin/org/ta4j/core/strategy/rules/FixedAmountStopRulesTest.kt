/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.Rule

class FixedAmountStopRulesTest {

    private fun price(numFactory: NumFactory, value: Double): NumericIndicator =
        ConstantNumericIndicator(numFactory.numOf(value))

    private fun satisfied(
        numFactory: NumFactory,
        tradeType: TradeType,
        currentPrice: Double,
        rule: (NumericIndicator, PositionRuntimeContext) -> Rule,
    ): Boolean {
        val runtimeContext = PositionRuntimeContext(null).apply { enter(numFactory, 100.0, tradeType) }
        return rule(price(numFactory, currentPrice), runtimeContext).isSatisfied
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fixed amount stop loss triggers at entry minus amount for BUY and plus amount for SELL`(numFactory: NumFactory) {
        val stopLoss = { price: NumericIndicator, ctx: PositionRuntimeContext -> price.fixedAmountStopLossRule(5, ctx) }

        assertThat(satisfied(numFactory, TradeType.BUY, 95.01, stopLoss)).isFalse()
        assertThat(satisfied(numFactory, TradeType.BUY, 95.0, stopLoss)).isTrue()
        assertThat(satisfied(numFactory, TradeType.SELL, 104.99, stopLoss)).isFalse()
        assertThat(satisfied(numFactory, TradeType.SELL, 105.0, stopLoss)).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fixed amount stop gain triggers at entry plus amount for BUY and minus amount for SELL`(numFactory: NumFactory) {
        val stopGain = { price: NumericIndicator, ctx: PositionRuntimeContext -> price.fixedAmountStopGainRule(5, ctx) }

        assertThat(satisfied(numFactory, TradeType.BUY, 104.99, stopGain)).isFalse()
        assertThat(satisfied(numFactory, TradeType.BUY, 105.0, stopGain)).isTrue()
        assertThat(satisfied(numFactory, TradeType.SELL, 95.01, stopGain)).isFalse()
        assertThat(satisfied(numFactory, TradeType.SELL, 95.0, stopGain)).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `trailing fixed amount stop loss trails highest price`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 105.01, 105.0)
            .withIndicator(closePrice)
        val positions = PositionRuntimeContext(null).apply { enter(numFactory, 100.0) }
        val rule = closePrice.trailingFixedAmountStopLossRule(5, positions)

        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(95))
        context.advance()
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(105))
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `trailing fixed amount stop gain arms after gain and triggers on retracement for BUY`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(104.0, 106.0, 101.01, 101.0)
            .withIndicator(closePrice)
        val positions = PositionRuntimeContext(null).apply { enter(numFactory, 100.0) }
        val rule = closePrice.trailingFixedAmountStopGainRule(5, positions)

        context.advance() // 104, activation at 105
        assertThat(rule.isSatisfied).isFalse()
        assertThat(rule.stopPrice).isNull()
        context.advance() // 106 armed -> stop 101
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(101))
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `trailing fixed amount stop gain arms after gain and triggers on retracement for SELL`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(96.0, 94.0, 98.99, 99.0)
            .withIndicator(closePrice)
        val positions = PositionRuntimeContext(null).apply { enter(numFactory, 100.0, TradeType.SELL) }
        val rule = closePrice.trailingFixedAmountStopGainRule(5, positions)

        context.advance() // 96, activation at 95
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // 94 armed -> stop 99
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(99))
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fixed amount rules reject non positive amounts`(numFactory: NumFactory) {
        val positions = PositionRuntimeContext(null)

        assertThatThrownBy { price(numFactory, 100.0).fixedAmountStopLossRule(-1, positions) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Stop amount must be positive")
        assertThatThrownBy { price(numFactory, 100.0).trailingFixedAmountStopGainRule(0, positions) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
