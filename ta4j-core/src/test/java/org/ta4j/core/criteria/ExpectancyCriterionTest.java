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
package org.ta4j.core.criteria;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.criteria.ExpectancyCriterion;
import org.ta4j.core.num.NumFactory;

class ExpectancyCriterionTest extends AbstractCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithProfitPositions(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectancyCriterion());

    // First trade: buy at 100, sell at 120 (profit: +20%)
    context.enter(1).at(100)
        .exit(1).at(120);

    // Second trade: buy at 130, sell at 160 (profit: +23%)
    context.enter(1).at(130)
        .exit(1).at(160);

    // All trades are profitable, expectancy should be 1.0
    context.assertResults(1.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithMixedPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectancyCriterion());

    // First trade: buy at 100, sell at 80 (loss: -20%)
    context.enter(1).at(100)
        .exit(1).at(80);

    // Second trade: buy at 130, sell at 160 (profit: +23%)
    context.enter(1).at(130)
        .exit(1).at(160);

    // One winning trade and one losing trade
    // Expectancy = (1 winning trade / 2 total trades) = 0.25
    context.assertResults(0.25);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithLossPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectancyCriterion());

    // First trade: buy at 100, sell at 95 (loss: -5%)
    context.enter(1).at(100)
        .exit(1).at(95);

    // Second trade: buy at 80, sell at 50 (loss: -37.5%)
    context.enter(1).at(80)
        .exit(1).at(50);

    // All trades are losses, expectancy should be 0
    context.assertResults(0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateProfitWithShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL)
        .withCriterion(new ExpectancyCriterion());

    // First trade: sell at 160, buy at 140 (profit: +12.5%)
    context.enter(1).at(160)
        .exit(1).at(140);

    // Second trade: sell at 120, buy at 60 (profit: +50%)
    context.enter(1).at(120)
        .exit(1).at(60);

    // All trades are profitable, expectancy should be 1.0
    context.assertResults(1.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateProfitWithMixedShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL)
        .withCriterion(new ExpectancyCriterion());

    // First trade: sell at 160, buy at 200 (loss: -25%)
    context.enter(1).at(160)
        .exit(1).at(200);

    // Second trade: sell at 120, buy at 60 (profit: +50%)
    context.enter(1).at(120)
        .exit(1).at(60);

    // One winning trade and one losing trade
    // Expectancy = (1 winning trade / 2 total trades) = 0.25
    context.assertResults(0.25);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new ExpectancyCriterion();
    assertTrue(criterion.betterThan(numFactory.numOf(2.0), numFactory.numOf(1.5)));
    assertFalse(criterion.betterThan(numFactory.numOf(1.5), numFactory.numOf(2.0)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectancyCriterion());

    // Open position without closing it
    context.enter(1).at(100);

    // Open position should return 0
    context.assertResults(0);
  }
}
