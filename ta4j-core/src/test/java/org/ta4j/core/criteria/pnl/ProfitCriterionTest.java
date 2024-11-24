/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.criteria.pnl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.analysis.cost.FixedTransactionCostModel;
import org.ta4j.core.criteria.AbstractCriterionTest;
import org.ta4j.core.num.NumFactory;

class ProfitCriterionTest extends AbstractCriterionTest {

  @ParameterizedTest
  @MethodSource("numFactories")
  void calculateComparingIncExcludingCosts(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new ProfitCriterion(true));

    context.operate(1).at(100)
        .operate(1).at(105)
        .operate(1).at(100)
        .operate(1).at(120)
        // exclude costs, i.e. costs are not contained:
        // [(105 - 100)] + [(120 - 100)] = 5 + 20 = +25 profit
        .assertResults(25)
    ;
  }


  @ParameterizedTest
  @MethodSource("numFactories")
  void calculateComparingIncludingCosts(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTransactionCostModel(new FixedTransactionCostModel(1))
        .withCriterion(new ProfitCriterion(false));

    context.operate(1).at(100)
        .operate(1).at(105)
        .operate(1).at(100)
        .operate(1).at(120)
        // include costs, i.e. profit - costs:
        // [(104 - 101)] + [(119 - 101)] = 3 + 18 = +21 profit
        // [(105 - 100)] + [(120 - 100)] = 5 + 20 = +25 profit - 4 = +21 profit
        .assertResults(21)
    ;
  }


  @ParameterizedTest
  @MethodSource("numFactories")
  void calculateProfitWithShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new ProfitCriterion(false));

    // Simulating short positions:
    context.operate(1).at(95)   // sell short
        .operate(1).at(100)     // buy to cover
        .operate(1).at(70)      // sell short
        .operate(1).at(100)     // buy to cover
        // First trade: loss of (100 - 95) = -5
        // Second trade: loss of (100 - 70) = -30
        // Total profit = 0
        .assertResults(0)
    ;
  }


  @ParameterizedTest
  @MethodSource("numFactories")
  void calculateProfitWithShortPositionsIncludingCosts(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.SELL)
        .withTransactionCostModel(new FixedTransactionCostModel(1))
        .withCriterion(new ProfitCriterion(false));

    context.operate(1).at(100)   // sell short
        .operate(1).at(95)       // buy to cover
        .operate(1).at(100)      // sell short
        .operate(1).at(70)       // buy to cover
        // First trade: profit of (100 - 95) = 5
        // Second trade: profit of (100 - 70) = 30
        // Total profit = 35 - 4
        .assertResults(31)
    ;
  }


  @ParameterizedTest
  @MethodSource("numFactories")
  void calculateWithMixedProfitAndLoss(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withCriterion(new ProfitCriterion(false));

    context.operate(1).at(100)
        .operate(1).at(105)     // +5 profit
        .operate(1).at(100)
        .operate(1).at(95)      // -5 loss (not accounted)
        .operate(1).at(100)
        .operate(1).at(110)     // +10 profit
        // Total profit = +15
        .assertResults(15)
    ;
  }


  @ParameterizedTest
  @MethodSource("numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new ProfitCriterion(true);
    assertTrue(criterion.betterThan(numFactory.numOf(2.0), numFactory.numOf(1.5)));
    assertFalse(criterion.betterThan(numFactory.numOf(1.5), numFactory.numOf(2.0)));
  }
}
