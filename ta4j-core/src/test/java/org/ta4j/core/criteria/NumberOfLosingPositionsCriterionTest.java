/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-202 Ta4j Organization & respective
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.backtest.criteria.NumberOfLosingPositionsCriterion;
import org.ta4j.core.num.NumFactory;

class NumberOfLosingPositionsCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110, 100, 95, 105)
        .toTradingRecordContext()
        .withCriterion(new NumberOfLosingPositionsCriterion());

    context.assertResults(0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTwoLongPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110, 99, 95, 91)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new NumberOfLosingPositionsCriterion());

    context.enter(1).asap()
        .exit(1).after(3)
        .enter(1).asap()
        .exit(1).asap();

    context.assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOneLongPosition(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110, 100, 95, 105)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY)
        .withCriterion(new NumberOfLosingPositionsCriterion());

    context.enter(1).asap()
        .exit(1).after(4);

    context.assertResults(1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTwoShortPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110, 100, 95, 105)
        .toTradingRecordContext()
        .withTradeType(TradeType.SELL)
        .withCriterion(new NumberOfLosingPositionsCriterion());

    context.enter(1).asap()
        .exit(1).asap()
        .enter(1).after(3)
        .exit(1).asap();

    context.assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPositionShouldReturnZero(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110)
        .toTradingRecordContext()
        .withCriterion(new NumberOfLosingPositionsCriterion());

    context.enter(1).asap();
    context.assertResults(0);
  }
}
