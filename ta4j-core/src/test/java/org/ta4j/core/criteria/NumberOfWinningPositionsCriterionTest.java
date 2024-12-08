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
import org.ta4j.core.Trade;
import org.ta4j.core.num.NumFactory;

class NumberOfWinningPositionsCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoPositions(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    context.toTradingRecordContext()
        .withCriterion(new NumberOfWinningPositionsCriterion())
        .assertResults(0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTwoLongPositions(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    context.toTradingRecordContext()
        .withCriterion(new NumberOfWinningPositionsCriterion())
        .enter(1).asap()
        .exit(1).after(2)
        .enter(1).asap()
        .exit(1).after(2)
        .assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOneLongPosition(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    context.toTradingRecordContext()
        .withCriterion(new NumberOfWinningPositionsCriterion())
        .enter(1).asap()
        .exit(1).after(2)
        .assertResults(1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTwoShortPositions(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(110, 105, 110, 100, 95, 105);

    context.toTradingRecordContext()
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new NumberOfWinningPositionsCriterion())
        .enter(1).asap()
        .exit(1).after(1)
        .enter(1).asap()
        .exit(1).after(2)
        .assertResults(2);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var criterion = new NumberOfWinningPositionsCriterion();
    assertTrue(criterion.betterThan(numFactory.numOf(6), numFactory.numOf(3)));
    assertFalse(criterion.betterThan(numFactory.numOf(4), numFactory.numOf(7)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testCalculateOneOpenPositionShouldReturnZero(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    context.toTradingRecordContext()
        .withCriterion(new NumberOfWinningPositionsCriterion())
        .enter(1).asap()
        .assertResults(0);
  }
}
