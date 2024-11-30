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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.backtest.BacktestBarSeriesBuilder;
import org.ta4j.core.num.NumFactory;

class ExpectedShortfallCriterionTest {

  private final Clock clock = Clock.fixed(Instant.MIN.plusSeconds(60), ZoneId.systemDefault());


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordMixedReturns(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(series, 0.95))
        .withConstantTimeDelays();

    // Add price history to series
    final var now = Instant.now(this.clock);
    final var prices = List.of(100.0, 102.0, 98.0, 97.0, 103.0, 95.0, 105.0, 100.0, 100., 10., 10., 10., 10., 10.);
    for (int i = 0; i < prices.size(); i++) {
      series.onCandle(candleBuilder(now.plusSeconds(i * 60), prices.get(i)));
    }

    context.forwardTime(1);

    // First position: Small loss (Buy 100, Sell 102)
    context.operate(1).at(100)
        .forwardTime(1)
        .operate(1).at(98);

    // Second position: Profit (Buy 97, Sell 103)
    context.operate(1).at(97)
        .operate(1).at(103);

    context.forwardTime(1);

    // Third position: Loss (Buy 105, Sell 10)
    context.operate(1).at(105)
        .forwardTime(3)
        .operate(1).at(10);

    context.assertResults(-0.9);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordOnlyLosses(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(series, 0.95))
        .withConstantTimeDelays();

    // Add price history to series
    final var now = Instant.now(this.clock);
    final var prices = List.of(100.0, 95.0, 90.0, 85.0, 80.0);
    for (int i = 0; i < prices.size(); i++) {
      series.onCandle(candleBuilder(now.plusSeconds(i * 60), prices.get(i)));
    }

    context.forwardTime(1);
    
    // First position: Loss
    context.operate(1).at(100)
        .operate(1).at(95);

    // Second position: Loss
    context.operate(1).at(90)
        .operate(1).at(85);

    context.assertResults(-0.05555);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordOnlyProfits(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(series, 0.95))
        .withConstantTimeDelays();

    // Add price history to series
    final var now = Instant.now(this.clock);
    final var prices = List.of(100.0, 105.0, 110.0, 115.0, 120.0);
    for (int i = 0; i < prices.size(); i++) {
      series.onCandle(candleBuilder(now.plusSeconds(i * 60), prices.get(i)));
    }

    context.forwardTime(1);
    
    // First position: Profit
    context.operate(1).at(100)
        .operate(1).at(105);

    // Second position: Profit
    context.operate(1).at(110)
        .operate(1).at(115);

    context.assertResults(0.04545);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithTradingRecordIncludingBreakeven(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(series, 0.95))
        .withConstantTimeDelays();

    // Add price history to series
    final var now = Instant.now(this.clock);
    final var prices = List.of(100.0, 105.0, 100.0, 100.0, 95.0);
    for (int i = 0; i < prices.size(); i++) {
      series.onCandle(candleBuilder(now.plusSeconds(i * 60), prices.get(i)));
    }

    context.forwardTime(1);

    // First position: Profit
    context.operate(1).at(100)
        .operate(1).at(105);

    // Second position: Breakeven
    context.operate(1).at(100)
        .operate(1).at(100);

    // Third position: Loss
    context.operate(1).at(100)
        .operate(1).at(95);

    context.assertResults(-0.05);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithGainPositions(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var context = new TradingRecordTestContext()
        .withNumFactory(numFactory)
        .withTradeType(Trade.TradeType.BUY)
        .withCriterion(new ExpectedShortfallCriterion(series, 0.95))
        .withConstantTimeDelays();

    final var now = Instant.now(this.clock);
    // Add bars to series
    series.onCandle(candleBuilder(now, 100));
    series.onCandle(candleBuilder(now.plusSeconds(60), 106));
    series.onCandle(candleBuilder(now.plusSeconds(120), 107));
    series.onCandle(candleBuilder(now.plusSeconds(180), 115));

    context.forwardTime(1);
    
    // First trade: buy at 100, sell at 106 (gain: +6%)
    context.operate(1).at(100)
        .operate(1).at(106);

    // Second trade: buy at 107, sell at 115 (gain: +7.5%)
    context.operate(1).at(107)
        .operate(1).at(115);

    // Only gains, so expected shortfall should be 0.06
    context.assertResults(0.06);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithASimplePosition(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var position = new Position(Trade.TradeType.BUY);
    final var now = Instant.now(this.clock);

    // Add bars to series
    series.onCandle(candleBuilder(now, 104));
    series.onCandle(candleBuilder(now.plusSeconds(60), 90));

    // Single trade: buy at 104, sell at 90
    position.operate(now, numFactory.numOf(104), numFactory.numOf(1));
    position.operate(now.plusSeconds(60), numFactory.numOf(90), numFactory.numOf(1));

    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    assertNumEquals(-0.13461538461538461538461538461538, criterion.calculate(position));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateOnlyWithLossPosition(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var prices = List.of(100.0, 75.0, 50.0, 25.0, 1.0);
    final var position = new Position(Trade.TradeType.BUY);

    // Add bars to series
    final var now = Instant.now(this.clock);
    for (int i = 0; i < prices.size(); i++) {
      series.onCandle(candleBuilder(now.plusSeconds(i * 60), prices.get(i)));
    }

    // Execute entry at price 100
    position.operate(now, numFactory.numOf(prices.getFirst()), numFactory.numOf(1));

    // Execute exit at price 1
    position.operate(now.plusSeconds(prices.size() * 60), numFactory.numOf(prices.getLast()), numFactory.numOf(1));

    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    assertNumEquals(numFactory.numOf(-0.96), criterion.calculate(position));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithNoBarsShouldReturnZero(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    final var tradingRecord = new BackTestTradingRecord();
    assertNumEquals(numFactory.numOf(0), criterion.calculate(tradingRecord));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithBuyAndHold(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var position = new Position(Trade.TradeType.BUY);
    final var now = Instant.now(this.clock);

    // Add bars to series
    series.onCandle(candleBuilder(now, 100));
    series.onCandle(candleBuilder(now.plusSeconds(60), 99));

    // Execute buy at price 100
    position.operate(now, numFactory.numOf(100), numFactory.numOf(1));
    // Execute sell at price 99
    position.operate(now.plusSeconds(60), numFactory.numOf(99), numFactory.numOf(1));

    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    assertNumEquals(Math.log(99d / 100), criterion.calculate(position));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithSinglePosition(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var position = new Position(Trade.TradeType.BUY);
    final var now = Instant.now(this.clock);

    // Add bar to series
    series.onCandle(candleBuilder(now, 100));

    // Only execute entry
    position.operate(now, numFactory.numOf(100), numFactory.numOf(1));

    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    assertNumEquals(numFactory.numOf(0), criterion.calculate(position));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void betterThan(final NumFactory numFactory) {
    final var series = new BacktestBarSeriesBuilder().withNumFactory(numFactory).build();
    final var criterion = new ExpectedShortfallCriterion(series, 0.95);
    assertTrue(criterion.betterThan(numFactory.numOf(-0.1), numFactory.numOf(-0.2)));
    assertFalse(criterion.betterThan(numFactory.numOf(-0.1), numFactory.numOf(0.0)));
  }


  private org.ta4j.core.events.CandleReceived candleBuilder(final Instant time, final double price) {
    return org.ta4j.core.events.CandleReceived.builder()
        .timePeriod(Duration.ofMinutes(1))
        .beginTime(time)
        .openPrice(price)
        .highPrice(price)
        .lowPrice(price)
        .closePrice(price)
        .volume(100)
        .amount(price * 100)
        .build();
  }
}
