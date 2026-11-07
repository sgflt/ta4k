/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.BacktestBarSeriesBuilder
import org.ta4j.core.indicators.helpers.FixedDecimalIndicator
import org.ta4j.core.num.NumFactory

class UnderIndicatorRuleTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun isSatisfied(numFactory: NumFactory) {
        val series = BacktestBarSeriesBuilder().withNumFactory(numFactory).build()
        val indicator = FixedDecimalIndicator(series, 0.0, 5.0, 8.0, 5.0, 1.0, 10.0, 20.0, 30.0)
        val rule = UnderIndicatorRule(indicator, numFactory.numOf(5))

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 5.0, 8.0, 5.0, 1.0, 10.0, 20.0, 30.0)

        context.withIndicator(indicator)

        // Test the values: 0, 5, 8, 5, 1, 10, 20, 30
        // UnderIndicatorRule should be satisfied when indicator < 5

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 0 < 5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 5 == 5 (not strictly less)

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 8 > 5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 5 == 5 (not strictly less)

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 1 < 5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 10 > 5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 20 > 5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 30 > 5
    }
}
