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

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.TradeType;
import org.ta4j.core.backtest.analysis.CashFlow;
import org.ta4j.core.num.NumFactory;

@Slf4j
class CashFlowTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowBuyWithOnlyOnePosition(final NumFactory numFactory) {
    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(1, 2)
        .toTradingRecordContext()
        .enter(1).after(1)
        .exit(1).after(1);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());

    assertNumEquals(numFactory.one(), cashFlow.getValue(tradingContext.getBarSeries().getBar(0).endTime()));
    assertNumEquals(numFactory.numOf(2), cashFlow.getValue(tradingContext.getBarSeries().getBar(1).endTime()));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowShortSellWith20PercentGain(final NumFactory numFactory) {
    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 90, 80)
        .toTradingRecordContext()
        .withTradeType(TradeType.SELL)
        .enter(1).after(1)
        .exit(1).after(2);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());
    final var bars = tradingContext.getBarSeries();

    assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime()));
    assertNumEquals(numFactory.numOf(1.1), cashFlow.getValue(bars.getBar(1).endTime()));
    assertNumEquals(numFactory.numOf(1.2), cashFlow.getValue(bars.getBar(2).endTime()));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowLongWith50PercentLoss(final NumFactory numFactory) {
    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(200, 190, 180, 170, 160, 150, 140, 130, 120, 110, 100)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY);

    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(10);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());
    final var bars = tradingContext.getBarSeries();

    // Check values at each step (price increases by 10 each bar)
    for (int i = 0; i <= 10; i++) {
      log.debug("{}: {}@{}", i, bars.getBar(i).endTime(), bars.getBar(i).closePrice());
      final var expectedValue = i < 1 ?
                                numFactory.one() :
                                numFactory.one().minus(
                                    numFactory.numOf(0.05).multipliedBy(numFactory.numOf(i))
                                );
      log.debug("expected {}", expectedValue);
      assertNumEquals(expectedValue, cashFlow.getValue(bars.getBar(i).endTime()));
    }
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowShortSellWith100PercentLoss(final NumFactory numFactory) {

    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200)
        .toTradingRecordContext()
        .withTradeType(TradeType.SELL)
        .enter(1).after(1)
        .exit(1).after(10);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());
    final var bars = tradingContext.getBarSeries();

    // Check values at each step (price increases by 10 each bar)
    for (int i = 0; i <= 10; i++) {
      log.debug("{}: {}@{}", i, bars.getBar(i).endTime(), bars.getBar(i).closePrice());
      final var expectedValue = i < 1 ?
                                numFactory.one() :
                                numFactory.one().minus(
                                    numFactory.numOf(0.1).multipliedBy(numFactory.numOf(i))
                                );
      log.debug("expected {}", expectedValue);
      assertNumEquals(expectedValue, cashFlow.getValue(bars.getBar(i).endTime()));
    }
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowWithNoTrades(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory);
    final var tradingRecord = context.toTradingRecordContext().getTradingRecord();
    final var cashFlow = new CashFlow(tradingRecord);

    assertNumEquals(numFactory.one(), cashFlow.getValue(Instant.EPOCH));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowHODL(final NumFactory numFactory) {

    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 120, 150, 135, 100, 100, 100, 200, 200, 160)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY);

    // Position 1: 100 -> 160 (profit: +60%)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(9);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());
    final var bars = tradingContext.getBarSeries();

    // Initial value
    assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime()));

    // After Position 1 close: 120/100 = 1.20
    assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(bars.getBar(1).endTime()));

    // After Position 2 close: 135/100 = 0.9
    assertNumEquals(numFactory.numOf(1.35), cashFlow.getValue(bars.getBar(3).endTime()));

    // After Position 3 close: 100/100
    assertNumEquals(numFactory.numOf(1.0), cashFlow.getValue(bars.getBar(5).endTime()));

    // After Position 4 close: 200/100 = 2.0
    assertNumEquals(numFactory.numOf(2.0), cashFlow.getValue(bars.getBar(7).endTime()));

    // After Position 5 close: 160 / 100 = 1.6
    assertNumEquals(numFactory.numOf(1.6), cashFlow.getValue(bars.getBar(9).endTime()));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void cashFlowWithMultipleBuyPositions(final NumFactory numFactory) {

    final var tradingContext = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 120, 150, 135, 100, 100, 100, 200, 200, 160)
        .toTradingRecordContext()
        .withTradeType(TradeType.BUY);

    // Position 1: 100 -> 120 (profit: +20%)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(1);

    // Position 2: 150 -> 135 (loss: -10%)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(1);

    // Position 3: 100 -> 100 (neutral)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(1);

    // Position 4: 100 -> 200 (profit: +100%)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(1);

    // Position 5: 200 -> 160 (loss: -20%)
    tradingContext.enter(1).after(1);
    tradingContext.exit(1).after(1);

    final var cashFlow = new CashFlow(tradingContext.getTradingRecord());
    final var bars = tradingContext.getBarSeries();

    // Initial value
    assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime()));

    // After Position 1 close: 120/100 = 1.20
    assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(bars.getBar(1).endTime()));

    // After Position 2 close: 135/150 = 0.9
    assertNumEquals(numFactory.numOf(0.9), cashFlow.getValue(bars.getBar(3).endTime()));

    // After Position 3 close: 100/100
    assertNumEquals(numFactory.numOf(1.0), cashFlow.getValue(bars.getBar(5).endTime()));

    // After Position 4 close: 200/100 = 2.0
    assertNumEquals(numFactory.numOf(2.0), cashFlow.getValue(bars.getBar(7).endTime()));

    // After Position 5 close: 160 / 200 = 0.8
    assertNumEquals(numFactory.numOf(0.8), cashFlow.getValue(bars.getBar(9).endTime()));
  }
}
