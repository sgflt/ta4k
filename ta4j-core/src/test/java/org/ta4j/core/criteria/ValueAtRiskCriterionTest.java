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

import org.junit.jupiter.api.Test;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.num.DoubleNumFactory;

class ValueAtRiskCriterionTest {

  @Test
  void calculateOnlyWithGainPositions() {
    new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCandlePrices(100d, 105d, 106d, 107d, 108d, 115d)
        .toTradingRecordContext()
        .withSeriesRelatedCriterion(series -> new ValueAtRiskCriterion(series.numFactory(), 0.95))
        .enter(1).after(1)
        .exit(1).after(2)
        .enter(1).after(1)
        .exit(1).after(2)
        .assertResults(0);
  }


  @Test
  void calculateWithASimplePosition() {
    new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCandlePrices(100d, 104d, 90d, 100d, 95d, 105d)
        .toTradingRecordContext()
        .withSeriesRelatedCriterion(series -> new ValueAtRiskCriterion(series.numFactory(), 0.95))
        .enter(1).after(2)
        .exit(1).after(1)
        .assertResults(90. / 104. - 1.);
  }


  @Test
  void calculateOnlyWithLossPositions() {
    new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCandlePrices(0, 100d, 95d, 100d, 80d, 85d, 70d)
        .toTradingRecordContext()
        .withSeriesRelatedCriterion(series -> new ValueAtRiskCriterion(series.numFactory(), 0.95))
        .enter(1).after(2)
        .exit(1).after(1)
        .enter(1).after(1)
        .exit(1).after(3)
        .assertResults(80. / 100. - 1.);
  }


  @Test
  void calculateWithNoTrades() {
    final var context = new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCandlePrices(0, 100d, 95d, 100d, 80d, 85d, 70d)
        .toTradingRecordContext()
        .withSeriesRelatedCriterion(series -> new ValueAtRiskCriterion(series.numFactory(), 0.95));

    context.assertResults(0);
  }
}
