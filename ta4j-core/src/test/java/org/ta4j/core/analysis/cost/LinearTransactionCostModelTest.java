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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.analysis.cost.CostModel;
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

class LinearTransactionCostModelTest {

  private CostModel transactionModel;


  @BeforeEach
  void setUp() {
    this.transactionModel = new LinearTransactionCostModel(0.01);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateSingleTradeCost(final NumFactory numFactory) {
    final var price = numFactory.numOf(100);
    final var amount = numFactory.numOf(2);
    final var cost = this.transactionModel.calculate(price, amount);

    assertNumEquals(numFactory.numOf(2), cost);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateBuyPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTransactionCostModel(this.transactionModel)
        .withTradeType(TradeType.BUY);

    context.enter(1).at(100)
        .exit(1).at(110);

    final var position = context.getTradingRecord().getPositions().getFirst();
    final var costFromBuy = position.getEntry().getCost();
    final var costFromSell = position.getExit().getCost();
    final var costsFromModel = this.transactionModel.calculate(position);

    assertNumEquals(costsFromModel, costFromBuy.plus(costFromSell));
    assertNumEquals(costsFromModel, numFactory.numOf(2.1));
    assertNumEquals(costFromBuy, numFactory.numOf(1));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateSellPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTransactionCostModel(this.transactionModel)
        .withTradeType(TradeType.SELL);

    context.enter(1).at(100)
        .exit(1).at(110);

    final var position = context.getTradingRecord().getPositions().getFirst();
    final var costFromSell = position.getEntry().getCost();
    final var costFromBuy = position.getExit().getCost();
    final var costsFromModel = this.transactionModel.calculate(position);

    assertNumEquals(costsFromModel, costFromSell.plus(costFromBuy));
    assertNumEquals(costsFromModel, numFactory.numOf(2.1));
    assertNumEquals(costFromSell, numFactory.numOf(1));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOpenPosition(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTransactionCostModel(this.transactionModel);

    context.enter(1).at(100);

    final var position = context.getTradingRecord().getCurrentPosition();
    final var costsFromModel = this.transactionModel.calculate(position);

    assertNumEquals(costsFromModel, numFactory.numOf(1));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testStrategyExecution(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(1, 2, 3, 4)
        .toTradingRecordContext()
        .withTransactionCostModel(new LinearTransactionCostModel(0.0026));

    context
        .enter(25).after(1)
        .exit(25).after(1)
        .enter(25).after(1)
        .exit(25).after(1);

    final var firstPositionBuy = numFactory.one()
        .plus(numFactory.one().multipliedBy(numFactory.numOf(0.0026)));
    final var firstPositionSell = numFactory.two()
        .minus(numFactory.two().multipliedBy(numFactory.numOf(0.0026)));
    final var firstPositionProfit = firstPositionSell.minus(firstPositionBuy)
        .multipliedBy(numFactory.numOf(25));

    final var secondPositionBuy = numFactory.three()
        .plus(numFactory.three().multipliedBy(numFactory.numOf(0.0026)));
    final var secondPositionSell = numFactory.numOf(4)
        .minus(numFactory.numOf(4).multipliedBy(numFactory.numOf(0.0026)));
    final var secondPositionProfit = secondPositionSell.minus(secondPositionBuy)
        .multipliedBy(numFactory.numOf(25));

    final var overallProfit = firstPositionProfit.plus(secondPositionProfit);

    assertNumEquals(
        overallProfit, context.getTradingRecord().getPositions().stream()
            .map(Position::getProfit)
            .reduce(numFactory.zero(), Num::plus)
    );
  }
}
