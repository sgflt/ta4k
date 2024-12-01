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

import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.num.NumFactory;

class MaximumDrawdownCriterionTest {


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoTrades(final NumFactory numFactory) {
    assertNumEquals(0d, new MaximumDrawdownCriterion(numFactory).calculate(new BackTestTradingRecord()));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOnlyGains(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withConstantTimeDelays()
        .withCriterion(new MaximumDrawdownCriterion(numFactory));

    // Execute trades
    context.operate(1).at(2.0)    // Buy at 2.0
        .operate(1).at(3.0)    // Sell at 3.0 (gain)
        .operate(1).at(3.0)    // Buy at 3.0
        .operate(1).at(20.0);  // Sell at 20.0 (gain)

    context.assertResults(0.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithGainsAndLosses(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withConstantTimeDelays()
        .withCriterion(new MaximumDrawdownCriterion(numFactory));

    // Execute trades with gains and losses
    context.operate(1).at(1.0)    // Buy at 1.0
        .operate(1).at(2.0)    // Sell at 2.0 (gain)
        .operate(1).at(6.0)    // Buy at 6.0
        .operate(1).at(5.0)    // Sell at 5.0 (loss)
        .operate(1).at(20.0)   // Buy at 20.0
        .operate(1).at(3.0);   // Sell at 3.0 (major loss)

    context.assertResults(0.875);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithSimpleTrades(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY)
        .withConstantTimeDelays()
        .withCriterion(new MaximumDrawdownCriterion(numFactory));

    // Execute sequence of trades
    context.operate(1).at(1.0)    // Buy at 1.0
        .operate(1).at(10.0)   // Sell at 10.0 (gain)
        .operate(1).at(10.0)   // Buy at 10.0
        .operate(1).at(5.0)    // Sell at 5.0 (loss)
        .operate(1).at(5.0)    // Buy at 5.0
        .operate(1).at(6.0)    // Sell at 6.0 (small gain)
        .operate(1).at(6.0)    // Buy at 6.0
        .operate(1).at(1.0);   // Sell at 1.0 (major loss)

    context.assertResults(0.9);
  }
}
