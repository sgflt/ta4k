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

import java.time.Instant;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TradeType;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.analysis.cost.FixedTransactionCostModel;
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

class FixedTransactionCostModelTest {

  private static final Random RANDOM = new Random();

  private static final Num PRICE = DoubleNum.valueOf(100);

  private static final Num AMOUNT = DoubleNum.valueOf(5);


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculatePerPositionWhenPositionIsOpen(final NumFactory numFactory) {
    final var positionTrades = 1;
    final var feePerTrade = RANDOM.nextDouble();
    final var model = new FixedTransactionCostModel(feePerTrade);

    final var position = new Position(TradeType.BUY, ZeroCostModel.INSTANCE, model, numFactory);
    position.operate(Instant.now(), PRICE, AMOUNT);
    final var cost = model.calculate(position);

    assertNumEquals(DoubleNum.valueOf(feePerTrade * positionTrades), cost);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculatePerPositionWhenPositionIsClosed(final NumFactory numFactory) {
    final var positionTrades = 2;
    final var feePerTrade = RANDOM.nextDouble();
    final var model = new FixedTransactionCostModel(feePerTrade);

    final var position = new Position(TradeType.BUY, model, model, numFactory);
    position.operate(Instant.now(), PRICE, AMOUNT);
    position.operate(Instant.now(), PRICE, AMOUNT);
    final var cost = model.calculate(position, RANDOM.nextInt());

    assertNumEquals(DoubleNum.valueOf(feePerTrade * positionTrades), cost);
  }


  @Test
  void calculatePerPrice() {
    final double feePerTrade = RANDOM.nextDouble();
    final FixedTransactionCostModel model = new FixedTransactionCostModel(feePerTrade);
    final Num cost = model.calculate(PRICE, AMOUNT);

    assertNumEquals(cost, DoubleNum.valueOf(feePerTrade));
  }
}
