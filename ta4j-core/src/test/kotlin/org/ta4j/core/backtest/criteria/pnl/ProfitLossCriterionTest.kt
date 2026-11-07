/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class ProfitLossCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(110.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(105.0)
            .assertResults(500.0 + 250.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(95.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(70.0)
            .assertResults(-250.0 - 1500.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(110.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(105.0)
            .assertResults(-(500.0 + 250.0))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(95.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(70.0)
            .assertResults(250.0 + 1500.0)
    }
}
