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
package org.ta4j.core;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Trade.OrderType;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NumFactory;

class TradeTest {

  @Test
  void buyTransactionShouldIncreaseNetPriceByTransactionCosts() {
    final var transactionCostModel = new LinearTransactionCostModel(0.1); // 10% fee
    final var trade = Trade.builder()
        .type(TradeType.BUY)
        .orderType(OrderType.OPEN)
        .whenExecuted(Instant.EPOCH)
        .pricePerAsset(DoubleNum.valueOf(100))
        .amount(DoubleNum.valueOf(1))
        .transactionCostModel(transactionCostModel)
        .build();

    assertNumEquals(100, trade.getPricePerAsset()); // Original price
    assertNumEquals(110, trade.getNetPrice()); // Price + 10% fee
    assertNumEquals(10, trade.getCost()); // Transaction cost
  }


  @Test
  void sellTransactionShouldDecreaseNetPriceByTransactionCosts() {
    final var transactionCostModel = new LinearTransactionCostModel(0.1); // 10% fee
    final var trade = Trade.builder()
        .type(TradeType.SELL)
        .orderType(OrderType.CLOSE)
        .whenExecuted(Instant.EPOCH)
        .pricePerAsset(DoubleNum.valueOf(100))
        .amount(DoubleNum.valueOf(1))
        .transactionCostModel(transactionCostModel)
        .build();

    assertNumEquals(100, trade.getPricePerAsset()); // Original price
    assertNumEquals(90, trade.getNetPrice()); // Price - 10% fee
    assertNumEquals(10, trade.getCost()); // Transaction cost
  }


  @Test
  void tradingValueShouldBeBasedOnPriceAndAmount() {
    final var trade = Trade.builder()
        .type(TradeType.BUY)
        .transactionCostModel(new ZeroCostModel())
        .orderType(OrderType.OPEN)
        .whenExecuted(Instant.EPOCH)
        .pricePerAsset(DoubleNum.valueOf(100))
        .amount(DoubleNum.valueOf(2))
        .build();

    // Value = price * amount (without transaction costs)
    assertNumEquals(200, trade.getValue());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void costsShouldScaleWithTradeSize(final NumFactory numFactory) {
    final var transactionCostModel = new LinearTransactionCostModel(0.01); // 1% fee

    // Small trade
    final var smallTrade = Trade.builder()
        .type(TradeType.BUY)
        .orderType(OrderType.OPEN)
        .whenExecuted(Instant.EPOCH)
        .pricePerAsset(numFactory.numOf(100))
        .amount(numFactory.numOf(1))
        .transactionCostModel(transactionCostModel)
        .build();

    // Large trade - 10x size
    final var largeTrade = Trade.builder()
        .type(TradeType.BUY)
        .orderType(OrderType.OPEN)
        .whenExecuted(Instant.EPOCH)
        .pricePerAsset(numFactory.numOf(100))
        .amount(numFactory.numOf(10))
        .transactionCostModel(transactionCostModel)
        .build();

    // Cost should scale linearly with amount
    assertNumEquals(largeTrade.getCost(), smallTrade.getCost().multipliedBy(numFactory.numOf(10)));
  }
}
