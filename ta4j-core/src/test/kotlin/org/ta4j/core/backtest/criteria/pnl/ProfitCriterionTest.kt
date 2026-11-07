/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.analysis.cost.FixedTransactionCostModel
import org.ta4j.core.num.NumFactory

class ProfitCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateComparingIncExcludingCosts(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitCriterion(true))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(120.0)
            // exclude costs, i.e. costs are not contained:
            // [(105 - 100)] + [(120 - 100)] = 5 + 20 = +25 profit
            .assertResults(25.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateComparingIncludingCosts(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(FixedTransactionCostModel(1.0))
            .withCriterion(ProfitCriterion(false))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(120.0)
            // include costs, i.e. profit - costs:
            // [(104 - 101)] + [(119 - 101)] = 3 + 18 = +21 profit
            // [(105 - 100)] + [(120 - 100)] = 5 + 20 = +25 profit - 4 = +21 profit
            .assertResults(21.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitCriterion(false))

        // Simulating short positions:
        context.enter(1.0).at(95.0)   // sell short
            .exit(1.0).at(100.0)      // buy to cover
            .enter(1.0).at(70.0)      // sell short
            .exit(1.0).at(100.0)      // buy to cover
            // First trade: loss of (100 - 95) = -5
            // Second trade: loss of (100 - 70) = -30
            // Total profit = 0
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositionsIncludingCosts(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withTransactionCostModel(FixedTransactionCostModel(1.0))
            .withCriterion(ProfitCriterion(false))

        context.enter(1.0).at(100.0)   // sell short
            .exit(1.0).at(95.0)        // buy to cover
            .enter(1.0).at(100.0)      // sell short
            .exit(1.0).at(70.0)        // buy to cover
            // First trade: profit of (100 - 95) = 5
            // Second trade: profit of (100 - 70) = 30
            // Total profit = 35 - 4
            .assertResults(31.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithMixedProfitAndLoss(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitCriterion(false))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)      // +5 profit
            .enter(1.0).at(100.0)
            .exit(1.0).at(95.0)       // -5 loss (not accounted)
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)      // +10 profit
            // Total profit = +15
            .assertResults(15.0)
    }
}
