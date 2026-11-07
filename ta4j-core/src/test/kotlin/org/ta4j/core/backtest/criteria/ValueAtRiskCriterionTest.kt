/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class ValueAtRiskCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithGainPositions(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 106.0, 107.0, 108.0, 115.0)
            .toTradingRecordContext()
            .withCriterion(ValueAtRiskCriterion(numFactory, 0.95))
            .enter(1.0).after(1)
            .exit(1.0).after(2)
            .enter(1.0).after(1)
            .exit(1.0).after(2)
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithASimplePosition(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 104.0, 90.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(ValueAtRiskCriterion(numFactory, 0.95))
            .enter(1.0).after(2)
            .exit(1.0).after(1)
            .assertResults(90.0 / 104.0 - 1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(ValueAtRiskCriterion(numFactory, 0.95) )
            .enter(1.0).after(2)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(3)
            .assertResults(80.0 / 100.0 - 1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoTrades(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(ValueAtRiskCriterion(numFactory, 0.95))

        context.assertResults(0.0)
    }
}
