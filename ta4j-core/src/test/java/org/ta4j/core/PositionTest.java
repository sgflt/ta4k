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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.NumFactory;

class PositionTest {


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("New position should start with correct state")
  void newPositionShouldHaveCorrectState(final NumFactory numFactory) {
    final var position = new Position(TradeType.BUY, numFactory);
    assertThat(position.isNew()).isTrue();
    assertThat(position.isOpened()).isFalse();
    assertThat(position.isClosed()).isFalse();
    assertThat(position.getEntry()).isNull();
    assertThat(position.getExit()).isNull();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Position should transition states correctly")
  void shouldTransitionStatesCorrectly(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory);

    context.enter(1).at(100);
    final var modifiedPosition = context.getTradingRecord().getCurrentPosition();
    assertThat(modifiedPosition.isOpened()).isTrue();

    context.exit(1).at(110);
    assertThat(modifiedPosition.isClosed()).isTrue();
    assertThat(context.getTradingRecord().getCurrentPosition().isNew()).isTrue();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should calculate profit correctly for long position")
  void shouldCalculateLongPositionProfit(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY);

    context.enter(1).at(100)
        .exit(1).at(110);

    final var position = context.getTradingRecord().getLastPosition();
    assertNumEquals(numFactory.numOf(10), position.getProfit());
    assertThat(position.hasProfit()).isTrue();
    assertThat(position.hasLoss()).isFalse();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should calculate profit correctly for short position")
  void shouldCalculateShortPositionProfit(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL);

    context.enter(1).at(100)
        .exit(1).at(90);

    final var position = context.getTradingRecord().getLastPosition();
    assertNumEquals(numFactory.numOf(10), position.getProfit());
    assertThat(position.hasProfit()).isTrue();
    assertThat(position.hasLoss()).isFalse();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should calculate costs with transaction cost model")
  void shouldCalculateTransactionCosts(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTransactionCostModel(new LinearTransactionCostModel(0.01));

    context.enter(1).at(100)
        .exit(1).at(110);

    final var position = context.getTradingRecord().getLastPosition();
    assertNumEquals(numFactory.numOf(2.1), position.getPositionCost());
    assertNumEquals(numFactory.numOf(7.9), position.getProfit());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  @DisplayName("Should handle zero prices")
  void shouldHandleZeroPrices(final NumFactory numFactory) {
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory);

    context.enter(1).at(0)
        .exit(1).at(0);

    final var position = context.getTradingRecord().getLastPosition();
    assertNumEquals(numFactory.zero(), position.getProfit());
    assertThat(position.hasProfit()).isFalse();
    assertThat(position.hasLoss()).isFalse();
  }


  @Test
  @DisplayName("Should throw exception when starting type is null")
  void shouldThrowExceptionOnNullStartingType() {
    assertThrows(
        NullPointerException.class,
        () -> new Position(null, DecimalNumFactory.getInstance())
    );
  }
}
