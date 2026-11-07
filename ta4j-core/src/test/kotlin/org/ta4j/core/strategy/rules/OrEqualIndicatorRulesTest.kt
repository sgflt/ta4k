/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class OrEqualIndicatorRulesTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `over or equal rule is satisfied when value is greater than or equal to threshold`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(6.0, 5.0, 4.99)
            .withIndicator(closePrice)
        val rule = closePrice.isGreaterThanOrEqualRule(5)

        context.advance()
        assertThat(rule.isSatisfied).isTrue()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `under or equal rule is satisfied when value is less than or equal to threshold`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(4.0, 5.0, 5.01)
            .withIndicator(closePrice)
        val rule = closePrice.isLessThanOrEqualRule(5)

        context.advance()
        assertThat(rule.isSatisfied).isTrue()
        context.advance()
        assertThat(rule.isSatisfied).isTrue()
        context.advance()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `or equal rules compare two indicators`(numFactory: NumFactory) {
        val five = ConstantNumericIndicator(numFactory.numOf(5))
        val alsoFive = ConstantNumericIndicator(numFactory.numOf(5))
        val six = ConstantNumericIndicator(numFactory.numOf(6))

        assertThat(OverOrEqualIndicatorRule(five, alsoFive).isSatisfied).isTrue()
        assertThat(OverOrEqualIndicatorRule(five, six).isSatisfied).isFalse()
        assertThat(UnderOrEqualIndicatorRule(five, alsoFive).isSatisfied).isTrue()
        assertThat(UnderOrEqualIndicatorRule(six, five).isSatisfied).isFalse()
        assertThat(OverIndicatorRule(five, alsoFive).isSatisfied).isFalse()
        assertThat(UnderIndicatorRule(five, alsoFive).isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `string representation names the rule`(numFactory: NumFactory) {
        val five = ConstantNumericIndicator(numFactory.numOf(5))

        assertThat(OverOrEqualIndicatorRule(five, 5).toString()).startsWith("OverOrEqualRule[")
        assertThat(UnderOrEqualIndicatorRule(five, 5).toString()).startsWith("UnderOrEqualRule[")
        assertThat(OverIndicatorRule(five, 5).toString()).startsWith("OverRule[")
    }
}
