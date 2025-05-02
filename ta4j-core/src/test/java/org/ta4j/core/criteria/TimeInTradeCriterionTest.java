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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.criteria.TimeInTradeCriterion;
import org.ta4j.core.num.DoubleNumFactory;

class TimeInTradeCriterionTest {

  private TradingRecordTestContext context;


  @Test
  @DisplayName("Should calculate zero for no positions")
  void calculateWithNoPositions() {
    this.context = new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.INSTANCE)
        .toTradingRecordContext()
        .withCriterion(TimeInTradeCriterion.minutes());

    this.context.assertResults(0);
  }


  @Test
  @DisplayName("Should calculate correct time for two positions")
  void calculateWithTwoPositions() {
    this.context = new MarketEventTestContext()
        .withNumFactory(DoubleNumFactory.INSTANCE)
        .withCandleDuration(ChronoUnit.MINUTES)
        .withCandlePrices(100.0, 0, 110.0, 100.0, 0, 0, 105.0)
        .toTradingRecordContext()
        .withCriterion(TimeInTradeCriterion.minutes())
        .withTradeType(TradeType.BUY);

    this.context
        .enter(100).after(1)
        .exit(100).after(2)
        .enter(100).after(1)
        .exit(100).after(3);

    this.context.assertResults(5);
  }


  @Test
  @DisplayName("Should calculate correct time for one position")
  void calculateWithOnePosition() {
    this.context = new MarketEventTestContext()
        .withCandleDuration(ChronoUnit.MINUTES)
        .withCandlePrices(100.0, 0, 0, 70.0)
        .withNumFactory(DoubleNumFactory.INSTANCE)
        .toTradingRecordContext()
        .withCriterion(TimeInTradeCriterion.minutes())
        .withTradeType(TradeType.BUY);

    this.context
        .enter(100).after(1)
        .exit(100).after(3);

    this.context.assertResults(3);
  }


  @Test
  @DisplayName("Should return zero for open position")
  void calculateOneOpenPositionShouldReturnZero() {
    this.context = new MarketEventTestContext()
        .withCandleDuration(ChronoUnit.MINUTES)
        .withCandlePrices(100.0, 0, 0, 70.0)
        .withNumFactory(DoubleNumFactory.INSTANCE)
        .toTradingRecordContext()
        .withCriterion(TimeInTradeCriterion.minutes())
        .withTradeType(TradeType.BUY);

    this.context
        .enter(100).after(1)
        .forwardTime(3);

    this.context.assertResults(0);
  }
}
