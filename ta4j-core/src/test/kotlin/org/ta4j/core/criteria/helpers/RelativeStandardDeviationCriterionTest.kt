/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.criteria.helpers.RelativeStandardDeviationCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactory

internal class RelativeStandardDeviationCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateStandardDeviationPnL(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(RelativeStandardDeviationCriterion(ProfitLossCriterion()))

        context.enter(1.0).asap()  // buy at 100
            .exit(1.0).after(2)   // sell at 110
            .enter(1.0).asap()  // buy at 100
            .exit(1.0).after(2)  // sell at 105

        context.assertResults(0.3333333333333333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun oneOpenPositionShouldReturnNaN(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(RelativeStandardDeviationCriterion(ProfitLossCriterion()))

        context.enter(1.0).asap()
            .assertResults(0.0)
    }
}