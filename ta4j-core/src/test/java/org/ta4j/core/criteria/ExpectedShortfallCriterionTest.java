/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.backtest.criteria.ExpectedShortfallCriterion;
import org.ta4j.core.backtest.strategy.BackTestTradingRecord;
import org.ta4j.core.num.NumFactory;

class ExpectedShortfallCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordMixedReturns(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100.0, 102.0, 98.0, 97.0, 103.0, 95.0, 105.0, 100.0, 100., 10., 10., 10., 10., 10.)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(numFactory, 0.95));


    // First position: Small loss (Buy 100, Sell 98)
    context.enter(1).asap()
        .exit(1).after(2);

    // Second position: Profit (Buy 97, Sell 103)
    context.enter(1).asap()
        .exit(1).asap();

    // Third position: Loss (Buy 105, Sell 10)
    context.enter(1).after(2)
        .exit(1).after(3);

    context.assertResults(-0.9);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordOnlyLosses(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100.0, 95.0, 90.0, 85.0, 80.0)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(numFactory, 0.95));

    // First position: Loss  100 -> 95
    context.enter(1).asap()
        .exit(1).asap();

    // Second position: Loss  90 -> 85
    context.enter(1).asap()
        .exit(1).asap();

    context.assertResults(-0.05555);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordOnlyProfits(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100.0, 105.0, 110.0, 115.0, 120.0)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(numFactory, 0.95));

    // First position: Profit
    context.enter(1).asap()
        .exit(1).asap();

    // Second position: Profit
    context.enter(1).asap()
        .exit(1).asap();

    context.assertResults(0.04545);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordIncludingBreakeven(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100.0, 105.0, 100.0, 100.0, 100.0, 95.0)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(numFactory, 0.95));

    // First position: Profit
    context.enter(1).asap()
        .exit(1).asap();

    // Second position: Breakeven
    context.enter(1).asap()
        .exit(1).asap();

    // Third position: Loss
    context.enter(1).asap()
        .exit(1).asap();

    context.assertResults(-0.05);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithGainPositions(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 106, 107, 115)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(numFactory, 0.95));

    // First trade: buy at 100, sell at 106 (gain: +6%)
    context.enter(1).asap()
        .exit(1).asap();

    // Second trade: buy at 107, sell at 115 (gain: +7.5%)
    context.enter(1).asap()
        .exit(1).asap();

    // Only gains, so expected shortfall should be 0.06
    context.assertResults(0.06);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoBarsShouldReturnZero(final NumFactory numFactory) {
    final var criterion = new ExpectedShortfallCriterion(numFactory, 0.95);
    final var tradingRecord = new BackTestTradingRecord(TradeType.BUY, numFactory);
    assertNumEquals(numFactory.numOf(0), criterion.calculate(tradingRecord));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new ExpectedShortfallCriterion(numFactory, 0.95);
    assertTrue(criterion.betterThan(numFactory.numOf(-0.1), numFactory.numOf(-0.2)));
    assertFalse(criterion.betterThan(numFactory.numOf(-0.1), numFactory.numOf(0.0)));
  }
}
