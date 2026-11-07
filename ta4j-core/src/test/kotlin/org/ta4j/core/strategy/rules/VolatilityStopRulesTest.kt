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

class VolatilityStopRulesTest {

    private fun constant(numFactory: NumFactory, value: Double): NumericIndicator =
        ConstantNumericIndicator(numFactory.numOf(value))

    private fun openPosition(numFactory: NumFactory, tradeType: TradeType = TradeType.BUY) =
        PositionRuntimeContext(null).apply { enter(numFactory, 100.0, tradeType) }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `volatility stops use volatility times coefficient as distance from entry`(numFactory: NumFactory) {
        val volatility = constant(numFactory, 2.0) // distance 2 * 1.5 = 3
        val long = openPosition(numFactory)
        val short = openPosition(numFactory, TradeType.SELL)

        assertThat(constant(numFactory, 97.01).volatilityStopLossRule(volatility, 1.5, long).isSatisfied).isFalse()
        assertThat(constant(numFactory, 97.0).volatilityStopLossRule(volatility, 1.5, long).isSatisfied).isTrue()
        assertThat(constant(numFactory, 103.0).volatilityStopLossRule(volatility, 1.5, short).isSatisfied).isTrue()
        assertThat(constant(numFactory, 102.99).volatilityStopGainRule(volatility, 1.5, long).isSatisfied).isFalse()
        assertThat(constant(numFactory, 103.0).volatilityStopGainRule(volatility, 1.5, long).isSatisfied).isTrue()
        assertThat(constant(numFactory, 97.0).volatilityStopGainRule(volatility, 1.5, short).isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `volatility trailing stop gain arms after volatility distance gain`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(102.0, 104.0, 101.01, 101.0)
            .withIndicator(closePrice)
        val rule = closePrice.volatilityTrailingStopGainRule(constant(numFactory, 2.0), 1.5, openPosition(numFactory))

        context.advance() // 102, activation at 103
        assertThat(rule.stopPrice).isNull()
        context.advance() // 104 armed -> stop 101
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(101))
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `atr stop loss is inactive while atr is unstable and follows current atr`(numFactory: NumFactory) {
        val indicators = Indicators.extended(numFactory)
        val closePrice = indicators.closePrice()
        val atr = indicators.atr(1)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 98.0)
            .withIndicators(closePrice, atr)
        val rule = closePrice.atrStopLossRule(atr, 0.5, openPosition(numFactory))

        context.advance() // ATR unstable, price at entry must not trigger
        assertThat(atr.isStable).isFalse()
        assertThat(rule.stopPrice).isNull()
        assertThat(rule.isSatisfied).isFalse()

        context.advance() // TR 2 -> stop 99
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(99))
        assertThat(rule.isSatisfied).isFalse()

        context.advance() // TR 4 -> stop 98
        assertThat(rule.isSatisfied).isTrue()
        assertThat(rule).isInstanceOf(AverageTrueRangeStopLossRule::class.java)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `atr trailing stop loss trails best price with current atr distance`(numFactory: NumFactory) {
        val indicators = Indicators.extended(numFactory)
        val closePrice = indicators.closePrice()
        val atr = indicators.atr(1)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 104.0)
            .withIndicators(closePrice, atr)
        val rule = closePrice.atrTrailingStopLossRule(atr, 1, openPosition(numFactory))

        context.advance()
        context.advance() // best 110, TR 10 -> stop 100
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(100))
        context.advance() // best 110, TR 6 -> stop 104
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `volatility rules reject non positive coefficient and describe themselves`(numFactory: NumFactory) {
        val positions = PositionRuntimeContext(null)
        val price = constant(numFactory, 100.0)

        assertThatThrownBy { price.volatilityStopLossRule(constant(numFactory, 1.0), 0, positions) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("coefficient must be positive")
        assertThat(price.atrStopGainRule(Indicators.extended(numFactory).atr(14), 2, positions).toString())
            .startsWith("AverageTrueRangeStopGainRule[")
    }
}
