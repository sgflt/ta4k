/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.criteria.helpers.StandardDeviationCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactory

class StandardDeviationCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateStandardDeviationPnL(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val tradingTest = context.toTradingRecordContext()
            .withCriterion(StandardDeviationCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).after(1)
            .exit(1.0).after(2)

        tradingTest.assertResults(2.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(StandardDeviationCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()
            .assertResults(0.0)
    }
}