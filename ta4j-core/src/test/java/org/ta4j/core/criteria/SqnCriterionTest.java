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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.Trade;
import org.ta4j.core.num.NumFactory;

class SqnCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithWinningLongPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    final var tradingContext = context.toTradingRecordContext()
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap()
        .exit(1.0).after(2)
        .enter(1.0).asap()
        .exit(1.0).after(2);

    tradingContext.assertResults(4.242640687119286);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingLongPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 95, 100, 80, 85, 70);

    final var tradingContext = context.toTradingRecordContext()
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap()
        .exit(1.0).asap()
        .enter(1.0).asap()
        .exit(1.0).after(3);

    tradingContext.assertResults(-1.9798989873223332);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOneWinningAndOneLosingLongPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 195, 100, 80, 85, 70);

    final var tradingContext = context.toTradingRecordContext()
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap()
        .exit(1.0).asap()
        .enter(1.0).asap()
        .exit(1.0).after(3);

    tradingContext.assertResults(0.7353910524340095);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithWinningShortPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 90, 100, 95, 95, 100);

    final var tradingContext = context.toTradingRecordContext()
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap()
        .exit(1.0).asap()
        .enter(1.0).asap()
        .exit(1.0).asap();

    tradingContext.assertResults(4.242640687119286);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingShortPositions(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 110, 100, 105, 95, 105);

    final var tradingContext = context.toTradingRecordContext()
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap()
        .exit(1.0).asap()
        .enter(1.0).asap()
        .exit(1.0).asap();

    tradingContext.assertResults(-4.242640687119286);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory factory) {
    final var criterion = new SqnCriterion();
    assertThat(criterion.betterThan(factory.numOf(50), factory.numOf(45))).isTrue();
    assertThat(criterion.betterThan(factory.numOf(45), factory.numOf(50))).isFalse();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPositionShouldReturnZero(final NumFactory factory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(factory)
        .withCandlePrices(100, 105);

    final var tradingContext = context.toTradingRecordContext()
        .withCriterion(new SqnCriterion())
        .enter(1.0).asap();

    tradingContext.assertResults(0);
  }
}
