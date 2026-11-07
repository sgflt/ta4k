/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.criteria.helpers.VarianceCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactory

internal class VarianceCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateVariancePnL(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .withNumFactory(numFactory)

        val tradingRecordContext = context.toTradingRecordContext()
            .withCriterion(VarianceCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).after(1)
            .exit(1.0).after(2)

        tradingRecordContext.assertResults(6.25)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculateOneOpenPositionShouldReturnZero(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withCandlePrices(100.0, 105.0)
            .withNumFactory(numFactory)

        val tradingRecordContext = context.toTradingRecordContext()
            .withCriterion(VarianceCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()

        tradingRecordContext.assertResults(0.0)
    }
}