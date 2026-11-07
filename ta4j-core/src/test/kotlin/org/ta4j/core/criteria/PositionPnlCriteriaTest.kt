/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.analysis.cost.FixedTransactionCostModel
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.CommissionsCriterion
import org.ta4j.core.backtest.criteria.pnl.MaxConsecutiveLossCriterion
import org.ta4j.core.backtest.criteria.pnl.MaxConsecutiveProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.MaxPositionNetLossCriterion
import org.ta4j.core.backtest.criteria.pnl.MaxPositionNetProfitCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory

class PositionPnlCriteriaTest {

    /** Long positions entered at 100 and exited at the given prices. */
    private fun record(numFactory: NumFactory, criterion: AnalysisCriterion, vararg exits: Double) =
        TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(criterion)
            .apply { exits.forEach { enter(1.0).at(100.0).exit(1.0).at(it) } }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `max consecutive profit sums the best winning streak`(numFactory: NumFactory) {
        record(numFactory, MaxConsecutiveProfitCriterion(), 110.0, 120.0, 95.0, 115.0).assertResults(30.0)
        record(numFactory, MaxConsecutiveProfitCriterion(), 90.0, 95.0).assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `max consecutive loss sums the worst losing streak`(numFactory: NumFactory) {
        record(numFactory, MaxConsecutiveLossCriterion(), 95.0, 90.0, 101.0, 80.0).assertResults(-20.0)
        record(numFactory, MaxConsecutiveLossCriterion(), 95.0, 90.0, 101.0, 98.0).assertResults(-15.0)
        record(numFactory, MaxConsecutiveLossCriterion(), 110.0).assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `max position net profit and loss pick the best and worst single position`(numFactory: NumFactory) {
        record(numFactory, MaxPositionNetProfitCriterion(), 110.0, 120.0, 95.0, 115.0).assertResults(20.0)
        record(numFactory, MaxPositionNetLossCriterion(), 110.0, 95.0, 80.0, 115.0).assertResults(-20.0)
        record(numFactory, MaxPositionNetProfitCriterion(), 95.0, 80.0).assertResults(0.0)
        record(numFactory, MaxPositionNetLossCriterion(), 110.0).assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `criteria are zero without positions`(numFactory: NumFactory) {
        val empty = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)

        assertNumEquals(0.0, MaxConsecutiveProfitCriterion().calculate(empty))
        assertNumEquals(0.0, MaxConsecutiveLossCriterion().calculate(empty))
        assertNumEquals(0.0, MaxPositionNetProfitCriterion().calculate(empty))
        assertNumEquals(0.0, MaxPositionNetLossCriterion().calculate(empty))
        assertNumEquals(0.0, CommissionsCriterion().calculate(empty))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `commissions sum entry and exit costs including open position`(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(FixedTransactionCostModel(1.0))
            .withCriterion(CommissionsCriterion())

        context.enter(1.0).at(100.0).exit(1.0).at(110.0)
            .enter(1.0).at(100.0).exit(1.0).at(90.0)
        context.assertResults(4.0)

        context.enter(1.0).at(100.0)
        context.assertResults(5.0)
    }
}
