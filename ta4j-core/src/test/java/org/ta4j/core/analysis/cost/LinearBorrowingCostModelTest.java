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
package org.ta4j.core.analysis.cost;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.num.NumFactory;

class LinearBorrowingCostModelTest {

  private final CostModel borrowingModel = new LinearBorrowingCostModel(0.01);
  private TradingRecordTestContext context;


  @BeforeEach
  void setUp() {
    this.context = new TradingRecordTestContext()
        .withHoldingCostModel(this.borrowingModel)
        .withResolution(ChronoUnit.DAYS);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateZeroTest(final NumFactory numFactory) {
    this.context.withNumFactory(numFactory);

    final var price = numFactory.numOf(100);
    final var amount = numFactory.numOf(2);
    final var cost = this.borrowingModel.calculate(price, amount);

    assertNumEquals(numFactory.zero(), cost);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateBuyPosition(final NumFactory numFactory) {
    this.context.withNumFactory(numFactory);

    this.context
        .withTradeType(Trade.TradeType.BUY)
        .enter(1).at(100)
        .exit(1).at(110)
    ;
    final var position = this.context.getTradingRecord().getPositions().getFirst();

    final var costsFromPosition = position.getHoldingCost();
    final var costsFromModel = this.borrowingModel.calculate(position);

    assertNumEquals(costsFromModel, costsFromPosition);
    assertNumEquals(numFactory.one(), costsFromModel);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateSellPosition(final NumFactory numFactory) {
    this.context.withNumFactory(numFactory);

    this.context
        .withTradeType(Trade.TradeType.SELL)
        .enter(1).at(100)
        .forwardTime(2)
        .exit(1).at(110);

    final var position = this.context.getTradingRecord().getPositions().getFirst();

    final var costsFromPosition = position.getHoldingCost();
    final var costsFromModel = this.borrowingModel.calculate(position);

    assertNumEquals(costsFromModel, costsFromPosition);
    assertNumEquals(numFactory.numOf(3), costsFromModel);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOpenSellPosition(final NumFactory numFactory) {
    this.context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandleDuration(ChronoUnit.DAYS)
        .withCandlePrices(100, 100, 100, 100, 100)
        .toTradingRecordContext()
        .withHoldingCostModel(this.borrowingModel)
        .withResolution(ChronoUnit.DAYS);

    this.context
        .withTradeType(Trade.TradeType.SELL)
        .enter(1).asap()
        .forwardTime(4);

    final var position = this.context.getTradingRecord().getCurrentPosition();

    final var costsFromPosition = position.getHoldingCost();
    final var costsFromModel = this.borrowingModel.calculate(position);

    assertNumEquals(costsFromModel, costsFromPosition);
    assertNumEquals(numFactory.numOf(4.0000), costsFromModel);
  }
}
