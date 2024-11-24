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
import org.ta4j.core.criteria.AbstractCriterionTest;
import org.ta4j.core.num.NumFactory;

class AverageProfitCriterionTest extends AbstractCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithProfitPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new AverageProfitCriterion());

    // First trade: buy at 100, sell at 110 (profit: +10)
    context.operate(1).at(100)
        .operate(1).at(110);

    // Second trade: buy at 100, sell at 105 (profit: +5)
    context.operate(1).at(100)
        .operate(1).at(105);

    // Average profit should be (10 + 5) / 2 = 7.5
    context.assertResults(7.5);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithLossPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new AverageProfitCriterion());

    // First trade: buy at 100, sell at 95 (loss: -5)
    context.operate(1).at(100)
        .operate(1).at(95);

    // Second trade: buy at 100, sell at 70 (loss: -30)
    context.operate(1).at(100)
        .operate(1).at(70);

    // No profits, so average profit should be 0
    context.assertResults(0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateProfitWithShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new AverageProfitCriterion());

    // First trade: sell at 100, buy at 85 (profit: +15)
    context.operate(1).at(100)
        .operate(1).at(85);

    // Second trade: sell at 80, buy at 95 (loss: -15)
    context.operate(1).at(80)
        .operate(1).at(95);

    // Average profit should be (15 + 0) / 2 = 15
    context.assertResults(15);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new AverageProfitCriterion();
    assertTrue(criterion.betterThan(numFactory.numOf(2.0), numFactory.numOf(1.5)));
    assertFalse(criterion.betterThan(numFactory.numOf(1.5), numFactory.numOf(2.0)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new AverageProfitCriterion());

    // Open position without closing it
    context.operate(1).at(100);

    // Open position should return 0
    context.assertResults(0);
  }
}
