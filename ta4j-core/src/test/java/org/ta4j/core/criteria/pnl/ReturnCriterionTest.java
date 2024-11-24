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
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.criteria.AbstractCriterionTest;
import org.ta4j.core.num.NumFactory;

class ReturnCriterionTest extends AbstractCriterionTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithWinningLongPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY);

    // First trade: buy at 100, sell at 110 (return: 1.10)
    context.operate(1).at(100)
        .operate(1).at(110);

    // Second trade: buy at 100, sell at 105 (return: 1.05)
    context.operate(1).at(100)
        .operate(1).at(105);

    // Total return with base percentage: 1.10 * 1.05
    context.withCriterion(new ReturnCriterion())
        .assertResults(1.10 * 1.05);

    // Total return without base percentage: (1.10 * 1.05) - 1
    context.withCriterion(new ReturnCriterion(false))
        .assertResults(1.10 * 1.05 - 1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingLongPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY);

    // First trade: buy at 100, sell at 95 (return: 0.95)
    context.operate(1).at(100)
        .operate(1).at(95);

    // Second trade: buy at 100, sell at 70 (return: 0.70)
    context.operate(1).at(100)
        .operate(1).at(70);

    // Total return with base percentage: 0.95 * 0.70
    context.withCriterion(new ReturnCriterion())
        .assertResults(0.95 * 0.70);

    // Total return without base percentage: (0.95 * 0.70) - 1
    context.withCriterion(new ReturnCriterion(false))
        .assertResults(0.95 * 0.70 - 1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateReturnWithWinningShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.SELL);

    // First trade: sell at 100, buy at 95 (return: 1.05)
    context.operate(1).at(100)
        .operate(1).at(95);

    // Second trade: sell at 100, buy at 70 (return: 1.30)
    context.operate(1).at(100)
        .operate(1).at(70);

    // Total return with base percentage: 1.05 * 1.30
    context.withCriterion(new ReturnCriterion())
        .assertResults(1.05 * 1.30);

    // Total return without base percentage: (1.05 * 1.30) - 1
    context.withCriterion(new ReturnCriterion(false))
        .assertResults(1.05 * 1.30 - 1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateReturnWithLosingShortPositions(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.SELL);

    // First trade: sell at 100, buy at 105 (return: 0.95)
    context.operate(1).at(100)
        .operate(1).at(105);

    // Second trade: sell at 100, buy at 130 (return: 0.70)
    context.operate(1).at(100)
        .operate(1).at(130);

    // Total return with base percentage: 0.95 * 0.70
    context.withCriterion(new ReturnCriterion())
        .assertResults(0.95 * 0.70);

    // Total return without base percentage: (0.95 * 0.70) - 1
    context.withCriterion(new ReturnCriterion(false))
        .assertResults(0.95 * 0.70 - 1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoPositions() {
    final var tradingRecord = new BackTestTradingRecord();

    final var withBase = new ReturnCriterion();
    assertNumEquals(1d, withBase.calculate(tradingRecord));

    final var withoutBase = new ReturnCriterion(false);
    assertNumEquals(0d, withoutBase.calculate(tradingRecord));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOpenedPosition(final NumFactory numFactory) {
    var position = new Position(Trade.TradeType.BUY);

    // Test with base percentage
    final var withBase = new ReturnCriterion();
    assertNumEquals(1d, withBase.calculate(position));

    // Add entry operation
    final var now = Instant.now(this.clock);
    position.operate(now, numFactory.numOf(100), numFactory.numOf(1));
    assertNumEquals(1d, withBase.calculate(position));

    // Test without base percentage
    position = new Position(Trade.TradeType.BUY);
    final var withoutBase = new ReturnCriterion(false);
    assertNumEquals(0d, withoutBase.calculate(position));

    // Add entry operation
    position.operate(now, numFactory.numOf(100), numFactory.numOf(1));
    assertNumEquals(0d, withoutBase.calculate(position));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new ReturnCriterion();
    assertTrue(criterion.betterThan(numFactory.numOf(2.0), numFactory.numOf(1.5)));
    assertFalse(criterion.betterThan(numFactory.numOf(1.5), numFactory.numOf(2.0)));
  }
}
