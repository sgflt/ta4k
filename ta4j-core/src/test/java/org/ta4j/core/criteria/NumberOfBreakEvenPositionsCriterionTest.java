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
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;

class NumberOfBreakEvenPositionsCriterionTest {

  @Test
  void calculateWithNoPositions() {
    new TradingRecordTestContext()
        .withCriterion(new NumberOfBreakEvenPositionsCriterion())
        .assertResults(0);
  }


  @Test
  void calculateWithTwoLongPositions() {
    new TradingRecordTestContext()
        .withCriterion(new NumberOfBreakEvenPositionsCriterion())
        .enter(1).at(100)
        .exit(1).at(100)
        .enter(1).at(105)
        .exit(1).at(105)
        .assertResults(2);
  }


  @Test
  void calculateWithOneLongPosition() {
    new TradingRecordTestContext()
        .withCriterion(new NumberOfBreakEvenPositionsCriterion())
        .enter(1).at(100)
        .exit(1).at(100)
        .assertResults(1);
  }


  @Test
  void calculateWithTwoShortPositions() {
    new TradingRecordTestContext()
        .withTradeType(Trade.TradeType.SELL)
        .withCriterion(new NumberOfBreakEvenPositionsCriterion())
        .enter(1).at(100)
        .exit(1).at(100)
        .enter(1).at(105)
        .exit(1).at(105)
        .assertResults(2);
  }


  @Test
  void calculateOneOpenPositionShouldReturnZero() {
    new TradingRecordTestContext()
        .withCriterion(new NumberOfBreakEvenPositionsCriterion())
        .enter(1).at(100)
        .assertResults(0);
  }
}
