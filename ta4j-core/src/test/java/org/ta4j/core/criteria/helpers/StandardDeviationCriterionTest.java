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
package org.ta4j.core.criteria.helpers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.backtest.criteria.helpers.StandardDeviationCriterion;
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.num.NumFactory;

class StandardDeviationCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateStandardDeviationPnL(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105);

    final var tradingTest = context.toTradingRecordContext()
        .withCriterion(new StandardDeviationCriterion(new ProfitLossCriterion()))
        .enter(1).asap()
        .exit(1).after(2)
        .enter(1).after(1)
        .exit(1).after(2);

    tradingTest.assertResults(2.5);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOneOpenPosition(final NumFactory numFactory) {
    new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105)
        .toTradingRecordContext()
        .withCriterion(new StandardDeviationCriterion(new ProfitLossCriterion()))
        .enter(1).asap()
        .assertResults(0);
  }
}
