package org.ta4j.core.criteria;

import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter;
import org.ta4j.core.backtest.criteria.NumberOfConsecutivePositionsCriterion;
import org.ta4j.core.backtest.strategy.BackTestTradingRecord;
import org.ta4j.core.num.NumFactory;

class NumberOfConsecutivePositionsCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should calculate zero for no positions")
  void calculateWithNoPositions(final NumFactory numFactory) {
    final var tradingRecord = new BackTestTradingRecord(TradeType.BUY, numFactory);

    assertNumEquals(0, new NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS).calculate(tradingRecord));
    assertNumEquals(0, new NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT).calculate(tradingRecord));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should handle consecutive winning long positions")
  void calculateWithTwoLongPositionsWin(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS));

    context.enter(1).at(110)
        .exit(1).at(100)
        .enter(1).at(90)
        .exit(1).at(80);

    context.assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should handle consecutive losing long positions")
  void calculateWithTwoLongPositionsLoss(final NumFactory numFactory) {
    final var profitContext = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT));

    profitContext.enter(1).at(100)
        .exit(1).at(120)
        .enter(1).at(120)
        .exit(1).at(130);

    profitContext.assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should handle single winning long position")
  void calculateWithOneLongPositionWin(final NumFactory numFactory) {
    final var profitContext = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT));

    profitContext.enter(1).at(100)
        .exit(1).at(120);

    profitContext.assertResults(1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should handle single losing long position")
  void calculateWithOneLongPositionLoss(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new NumberOfConsecutivePositionsCriterion(PositionFilter.LOSS));

    context.enter(1).at(110)
        .exit(1).at(90);

    context.assertResults(1);
  }
}
