/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
