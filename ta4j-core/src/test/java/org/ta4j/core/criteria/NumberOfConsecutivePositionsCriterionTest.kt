package org.ta4j.core.criteria

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.backtest.criteria.NumberOfConsecutivePositionsCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory

class NumberOfConsecutivePositionsCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate zero for no positions")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val tradingRecord = BackTestTradingRecord(
            startingType = TradeType.BUY,
            numFactory = numFactory
        )

        assertNumEquals(0.0, NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS).calculate(tradingRecord))
        assertNumEquals(0.0, NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT).calculate(tradingRecord))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle consecutive winning long positions")
    fun calculateWithTwoLongPositionsWin(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS))

        context.enter(1.0).at(110.0)
            .exit(1.0).at(100.0)
            .enter(1.0).at(90.0)
            .exit(1.0).at(80.0)

        context.assertResults(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle consecutive losing long positions")
    fun calculateWithTwoLongPositionsLoss(numFactory: NumFactory) {
        val profitContext = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT))

        profitContext.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)
            .enter(1.0).at(120.0)
            .exit(1.0).at(130.0)

        profitContext.assertResults(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle single winning long position")
    fun calculateWithOneLongPositionWin(numFactory: NumFactory) {
        val profitContext = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT))

        profitContext.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)

        profitContext.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle single losing long position")
    fun calculateWithOneLongPositionLoss(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS))

        context.enter(1.0).at(110.0)
            .exit(1.0).at(90.0)

        context.assertResults(1.0)
    }
}