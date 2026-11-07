/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

class RiskRewardRatioRuleTest {

    private fun c(numFactory: NumFactory, value: Double) = ConstantNumericIndicator(numFactory.numOf(value))

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `bullish setup meets minimum risk reward`(numFactory: NumFactory) {
        val price = c(numFactory, 150.0)
        val stop = c(numFactory, 120.0)
        val target = c(numFactory, 240.0)

        val rule = price.riskRewardRatioRule(stop, target, 3)
        assertThat(rule.riskReward).isEqualTo(numFactory.numOf(3))
        assertThat(rule.isSatisfied).isTrue()
        assertThat(price.riskRewardRatioRule(stop, target, 4).isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `bearish setup meets minimum risk reward`(numFactory: NumFactory) {
        val rule = c(numFactory, 100.0).riskRewardRatioRule(
            stop = c(numFactory, 110.0),
            target = c(numFactory, 80.0),
            minRiskReward = 2,
            tradeType = TradeType.SELL
        )

        assertThat(rule.riskReward).isEqualTo(numFactory.numOf(2))
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `invalid setups are never satisfied`(numFactory: NumFactory) {
        val price = c(numFactory, 100.0)

        // stop above price for a long
        assertThat(price.riskRewardRatioRule(c(numFactory, 101.0), c(numFactory, 200.0), 1).isSatisfied).isFalse()
        // target below price for a long
        assertThat(price.riskRewardRatioRule(c(numFactory, 90.0), c(numFactory, 99.0), 0.1).isSatisfied).isFalse()
        // stop below price for a short
        assertThat(
            price.riskRewardRatioRule(c(numFactory, 90.0), c(numFactory, 50.0), 0.1, TradeType.SELL).isSatisfied
        ).isFalse()
        // unavailable stop
        val rule = price.riskRewardRatioRule(ConstantNumericIndicator(NaN), c(numFactory, 200.0), 1)
        assertThat(rule.riskReward).isNull()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `rejects non positive minimum`(numFactory: NumFactory) {
        assertThatThrownBy { c(numFactory, 100.0).riskRewardRatioRule(c(numFactory, 90.0), c(numFactory, 120.0), 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
