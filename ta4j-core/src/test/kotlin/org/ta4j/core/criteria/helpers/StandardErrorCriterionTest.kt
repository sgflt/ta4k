/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.criteria.helpers.StandardErrorCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactory

internal class StandardErrorCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateStandardErrorPnL(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val tradingTest = context.toTradingRecordContext()
            .withCriterion(StandardErrorCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).asap()
            .exit(1.0).after(2)

        tradingTest.assertResults(1.7677669529663687)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculateOneOpenPositionShouldReturnZero(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)

        val tradingTest = context.toTradingRecordContext()
            .withCriterion(StandardErrorCriterion(ProfitLossCriterion()))
            .enter(1.0).asap()

        tradingTest.assertResults(0.0)
    }
}