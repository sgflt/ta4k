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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.criteria.AbstractCriterionTest;
import org.ta4j.core.num.NumFactory;

class ProfitLossPercentageCriterionTest extends AbstractCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithWinningLongPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ProfitLossPercentageCriterion());

    // First trade: buy at 100, sell at 110 (profit: +10%)
    context.enter(1).at(100)
        .exit(1).at(110);

    // Second trade: buy at 100, sell at 105 (profit: +5%)
    context.enter(1).at(100)
        .exit(1).at(105);

    // Total percentage profit should be 10% + 5% = 15%
    context.assertResults(15);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingLongPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ProfitLossPercentageCriterion());

    // First trade: buy at 100, sell at 95 (loss: -5%)
    context.enter(1).at(100)
        .exit(1).at(95);

    // Second trade: buy at 100, sell at 70 (loss: -30%)
    context.enter(1).at(100)
        .exit(1).at(70);

    // Total percentage loss should be -5% + -30% = -35%
    context.assertResults(-35);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOneWinningAndOneLosingLongPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ProfitLossPercentageCriterion());

    // First trade: buy at 100, sell at 195 (profit: +95%)
    context.enter(1).at(100)
        .exit(1).at(195);

    // Second trade: buy at 100, sell at 70 (loss: -30%)
    context.enter(1).at(100)
        .exit(1).at(70);

    // Net percentage should be +95% + -30% = +65%
    context.assertResults(65);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithWinningShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL)
        .withCriterion(new ProfitLossPercentageCriterion());

    // First trade: sell at 100, buy at 90 (profit: +10%)
    context.enter(1).at(100)
        .exit(1).at(90);

    // Second trade: sell at 100, buy at 95 (profit: +5%)
    context.enter(1).at(100)
        .exit(1).at(95);

    // Total percentage profit should be 10% + 5% = 15%
    context.assertResults(15);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL)
        .withCriterion(new ProfitLossPercentageCriterion());

    // First trade: sell at 100, buy at 110 (loss: -10%)
    context.enter(1).at(100)
        .exit(1).at(110);

    // Second trade: sell at 100, buy at 105 (loss: -5%)
    context.enter(1).at(100)
        .exit(1).at(105);

    // Total percentage loss should be -10% + -5% = -15%
    context.assertResults(-15);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ProfitLossPercentageCriterion());

    // Open position without closing it
    context.enter(1).at(100);

    // Open position should return 0
    context.assertResults(0);
  }
}
