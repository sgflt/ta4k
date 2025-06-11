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
package org.ta4j.core.analysis;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.analysis.RealizedCashFlow;
import org.ta4j.core.backtest.strategy.BackTestTradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

class RealizedCashFlowTest {

  private final Clock clock = Clock.fixed(Instant.MIN, ZoneId.systemDefault());


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowBuyWithOnlyOnePosition(final NumFactory numFactory) {
    final var position = new Position(TradeType.BUY, numFactory);
    final var now = Instant.now(this.clock);

    // Execute buy at price 1
    position.operate(now, numFactory.numOf(1), numFactory.numOf(1));

    // Execute sell at price 2
    position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(2), numFactory.numOf(1));

    final var cashFlow = new RealizedCashFlow(numFactory, position);

    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now));
    assertNumEquals(numFactory.numOf(2), cashFlow.getValue(now.plus(Duration.ofMinutes(1))));
  }


  @ParameterizedTest
  @Disabled("Ta4J currently does not support mixed trades")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowWithSellAndBuyTrades(final NumFactory numFactory) {
    final var buyContext = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.BUY);

    // First trade: buy at 2, sell at 1 (loss: 50%)
    buyContext.enter(1).at(2);
    buyContext.exit(1).at(1);

    // Second trade: buy at 5, sell at 6 (profit: 20%)
    buyContext.enter(1).at(5);
    buyContext.exit(1).at(6);

    // Switch to sell context for the third trade
    final var sellContext = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(TradeType.SELL);

    // Third trade: sell at 3, buy at 20 (loss: large)
    sellContext.enter(1).at(3);
    sellContext.exit(1).at(20);

    // Merge the trading records
    final var tradingRecord = buyContext.getTradingRecord();
    sellContext.getTradingRecord().getPositions().forEach(position -> {
      tradingRecord.enter(
          position.getEntry().getWhenExecuted(),
          position.getEntry().getNetPrice(),
          position.getEntry().getAmount()
      );
      if (position.getExit() != null) {
        tradingRecord.exit(
            position.getExit().getWhenExecuted(),
            position.getExit().getNetPrice(),
            position.getExit().getAmount()
        );
      }
    });

    final var cashFlow = new RealizedCashFlow(numFactory, tradingRecord);
    final var now = Instant.now(this.clock);

    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now));
    assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(1))));
    assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(2))));
    assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(3))));
    assertNumEquals(numFactory.numOf(0.6), cashFlow.getValue(now.plus(Duration.ofMinutes(4))));
    assertNumEquals(numFactory.numOf(0.6), cashFlow.getValue(now.plus(Duration.ofMinutes(5))));
    assertNumEquals(numFactory.numOf(-2.8), cashFlow.getValue(now.plus(Duration.ofMinutes(6))));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowShortSellWith20PercentGain(final NumFactory numFactory) {
    final var position = new Position(TradeType.SELL, numFactory);
    final var now = Instant.now(this.clock);

    // Short sell at 100
    position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(100), numFactory.numOf(1));

    // Cover at 80 (20% gain)
    position.operate(now.plus(Duration.ofMinutes(3)), numFactory.numOf(80), numFactory.numOf(1));

    final var cashFlow = new RealizedCashFlow(numFactory, position);

    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now));
    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(1))));
    assertNumEquals(numFactory.numOf(1.1), cashFlow.getValue(now.plus(Duration.ofMinutes(2))));
    assertNumEquals(numFactory.numOf(1.2), cashFlow.getValue(now.plus(Duration.ofMinutes(3))));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowShortSellWith100PercentLoss(final NumFactory numFactory) {
    final var position = new Position(TradeType.SELL, numFactory);
    final var now = Instant.now(this.clock);

    // Short sell at 100
    position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(100), numFactory.numOf(1));

    // Cover at 200 (100% loss)
    position.operate(now.plus(Duration.ofMinutes(11)), numFactory.numOf(200), numFactory.numOf(1));

    final var cashFlow = new RealizedCashFlow(numFactory, position);

    // Check values at each step (price increases by 10 each minute)
    for (int i = 0; i <= 11; i++) {
      final Num expectedValue;
      if (i <= 1) {
        expectedValue = numFactory.one();
      } else {
        final Num decline = numFactory.numOf(0.1).times(numFactory.numOf(i - 1));
        final var x = numFactory.one().minus(decline);
        expectedValue = x.compareTo(numFactory.zero()) == 1 ? x : numFactory.zero();
      }
      assertNumEquals(
          expectedValue,
          cashFlow.getValue(now.plus(Duration.ofMinutes(i)))
      );
    }
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowWithNoTrades(final NumFactory numFactory) {
    final var tradingRecord = new BackTestTradingRecord(TradeType.BUY, numFactory);
    final var cashFlow = new RealizedCashFlow(numFactory, tradingRecord);
    final var now = Instant.now(this.clock);

    // Should return 1 for any timestamp when no trades exist
    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now));
    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(5))));
    assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(10))));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowWithMultipleBuyPositions(final NumFactory numFactory) {
    final var startTime = Instant.parse("1970-01-01T00:00:00.000Z");

    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withStartTime(startTime)
        .withCandleDuration(ChronoUnit.MINUTES)
        .withCandlePrices(100, 120, 150, 135, 100, 100, 100, 200, 200, 160)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY);


    // Position 1: 100 -> 120 (profit: +20%)
    context.enter(1).asap();
    context.exit(1).asap();

    // Position 2: 150 -> 135 (loss: -10%)
    context.enter(1).asap();
    context.exit(1).asap();

    // Position 3: 100 -> 100 (neutral)
    context.enter(1).asap();
    context.exit(1).asap();

    // Position 4: 100 -> 200 (profit: +100%)
    context.enter(1).asap();
    context.exit(1).asap();

    // Position 5: 200 -> 160 (loss: -20%)
    context.enter(1).asap();
    context.exit(1).asap();

    final var tradingRecord = context.getTradingRecord();
    final var cashFlow = new RealizedCashFlow(numFactory, tradingRecord);

    final var endTime = startTime.plus(Duration.ofMinutes(1));

    // Initial value at the first trade time
    assertNumEquals(numFactory.one(), cashFlow.getValue(endTime.plus(Duration.ofMinutes(1))));

    // After Position 1 close: 1.0 * 1.20 = 1.20
    assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(endTime.plus(Duration.ofMinutes(2))));

    // After Position 2 close: 1.20 * 0.90 = 1.08
    assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(4))));

    // After Position 3 close: 1.08 * 1.00 = 1.08
    assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(6))));

    // After Position 4 close: 1.08 * 2.00 = 2.16
    assertNumEquals(numFactory.numOf(2.16), cashFlow.getValue(endTime.plus(Duration.ofMinutes(8))));

    // After Position 5 close: 2.16 * 0.80 = 1.728
    assertNumEquals(numFactory.numOf(1.728), cashFlow.getValue(endTime.plus(Duration.ofMinutes(10))));

    // Test interpolation points
    assertNumEquals(numFactory.numOf(1.10), cashFlow.getValue(endTime.plus(Duration.ofMinutes(1).plusSeconds(30))));
    assertNumEquals(numFactory.numOf(1.14), cashFlow.getValue(endTime.plus(Duration.ofMinutes(3))));
    assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(5))));
    assertNumEquals(numFactory.numOf(1.62), cashFlow.getValue(endTime.plus(Duration.ofMinutes(7))));
    assertNumEquals(numFactory.numOf(1.944), cashFlow.getValue(endTime.plus(Duration.ofMinutes(9))));
  }
}
