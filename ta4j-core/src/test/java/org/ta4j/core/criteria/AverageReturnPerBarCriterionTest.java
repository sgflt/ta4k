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

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.backtest.criteria.AverageReturnPerBarCriterion;
import org.ta4j.core.num.NumFactory;

class AverageReturnPerBarCriterionTest {

  private final MarketEventTestContext context = new MarketEventTestContext();


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithGainPositions(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100d, 105d, 110d, 100d, 95d, 105d)
        .toTradingRecordContext()
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
        .enter(1).asap()
        .exit(1).after(2)
        .enter(1).asap()
        .exit(1).after(2);

    tradingContext.assertResults(Math.pow((110. / 100.) * (105. / 100.), (1. / 4.)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithASimplePosition(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100d, 105d, 110d)
        .toTradingRecordContext()
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
        .enter(1).asap()
        .exit(1).after(2);

    final var expectedReturn = numFactory.numOf(110d).div(numFactory.numOf(100))
        .pow(numFactory.numOf(1. / 2));
    tradingContext.assertResults(expectedReturn.doubleValue());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithLossPositions(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100d, 95d, 100d, 80d, 85d, 70d)
        .toTradingRecordContext()
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
        .enter(1).asap()
        .exit(1).asap()
        .enter(1).asap()
        .exit(1).after(3);

    final var expectedReturn = numFactory.numOf(95. / 100. * 70. / 100.).pow(numFactory.numOf(1. / 4.));
    tradingContext.assertResults(expectedReturn.doubleValue());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLosingShortPositions(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100., 105., 90.)
        .toTradingRecordContext()
        .withTradeType(TradeType.SELL)
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
        .enter(1).asap()
        .exit(1).after(2);

    final var expectedReturn = numFactory.numOf((100. - 90.) / 100 + 1).pow(numFactory.numOf(1. / 2.));
    tradingContext.assertResults(expectedReturn.doubleValue());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoBarsShouldReturnOne(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100d, 95d, 100d, 80d, 85d, 70d)
        .toTradingRecordContext()
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS));

    tradingContext.assertResults(1.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithOnePosition(final NumFactory numFactory) {
    final var tradingContext = this.context
        .withNumFactory(numFactory)
        .withCandlePrices(100d, 105d)
        .toTradingRecordContext()
        .withCriterion(new AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
        .enter(1).asap()
        .exit(1).asap();

    final var expectedReturn = numFactory.numOf(105d / 100).pow(numFactory.numOf(1.0));
    tradingContext.assertResults(expectedReturn.doubleValue());
  }
}
