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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.NumFactory;

class ReturnOverMaxDrawdownCriterionTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateBasicScenario(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 95, 100, 90, 95, 80, 120);

    // For sequence: 100->105->95->100->90->95->80->120
    // Maximum drawdown occurs from peak of 105 to bottom of 80: (105-80)/105 = 23.8%
    // Final portfolio value is 1.2 (20% gain)
    // Return over drawdown = 0.20/0.238 = 0.84
    marketContext.toTradingRecordContext()
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .enter(100).asap()
        .exit(100).after(7)
        .assertResults(0.84);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithGain(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(1, 2, 3, 6, 8, 20);

    // Only gains, no drawdown should result in NaN as we can't divide by zero drawdown
    marketContext.toTradingRecordContext()
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .enter(100).asap()
        .exit(100).after(4)
        .assertResults(Double.NaN);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoPositions(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105);

    marketContext.toTradingRecordContext()
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .assertResults(0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithLongPosition(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 95);

    // Long position loses 5%
    // Return = -5%
    // Max Drawdown = 5%
    // Return/Drawdown = -1.0
    marketContext.toTradingRecordContext()
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .enter(100).asap()
        .exit(100).asap()
        .assertResults(-1.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithShortPosition(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 95);

    // Short position gains 5% when price falls
    // Return = +5%
    // Max Drawdown from cashflow perspective = 4.76%
    // Return/Drawdown = 1.05
    marketContext.toTradingRecordContext()
        .withTradeType(TradeType.SELL)
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .enter(100).asap()
        .exit(100).asap()
        .assertResults(1.05);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testNoDrawDownForTradingRecord(final NumFactory numFactory) {
    final var marketContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 95, 100) ;

    marketContext.toTradingRecordContext()
        .withCriterion(new ReturnOverMaxDrawdownCriterion(numFactory))
        .enter(1).asap()
        .exit(1).asap()
        .enter(1).asap()
        .exit(1).asap()
        .assertResults(Double.NaN);
  }
}
