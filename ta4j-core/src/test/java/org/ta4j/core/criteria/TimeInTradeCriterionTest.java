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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.num.DoubleNumFactory;

class TimeInTradeCriterionTest {

  private TradingRecordTestContext context;


  @Test
  @DisplayName("Should calculate zero for no positions")
  void calculateWithNoPositions() {
    this.context = new TradingRecordTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCriterion(TimeInTradeCriterion.minutes())
        .withConstantTimeDelays();

    this.context.assertResults(0);
  }

  @Test
  @DisplayName("Should calculate correct time for two positions")
  void calculateWithTwoPositions() {
    this.context = new TradingRecordTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCriterion(TimeInTradeCriterion.minutes())
        .withConstantTimeDelays()
        .withTradeType(Trade.TradeType.BUY);

    this.context
        .operate(100).at(100.0)
        .forwardTime(2)
        .operate(100).at(110.0)
        .forwardTime(1)
        .operate(100).at(100.0)
        .forwardTime(2)
        .operate(100).at(105.0);

    this.context.assertResults(6);
  }

  @Test
  @DisplayName("Should calculate correct time for one position")
  void calculateWithOnePosition() {
    this.context = new TradingRecordTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCriterion(TimeInTradeCriterion.minutes())
        .withConstantTimeDelays()
        .withTradeType(Trade.TradeType.BUY);

    this.context
        .operate(100).at(100.0)
        .forwardTime(3)
        .operate(100).at(70.0);

    this.context.assertResults(4);
  }

  @Test
  @DisplayName("Should return zero for open position")
  void calculateOneOpenPositionShouldReturnZero() {
    this.context = new TradingRecordTestContext()
        .withNumFactory(DoubleNumFactory.getInstance())
        .withCriterion(TimeInTradeCriterion.minutes())
        .withConstantTimeDelays()
        .withTradeType(Trade.TradeType.BUY);

    this.context
        .operate(100).at(100.0)
        .forwardTime(5);

    this.context.assertResults(0);
  }
}
