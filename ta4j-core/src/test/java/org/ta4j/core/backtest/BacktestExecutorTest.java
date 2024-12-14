/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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

package org.ta4j.core.backtest;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.StrategyFactory;
import org.ta4j.core.Trade;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator;
import org.ta4j.core.num.NumFactory;

@Slf4j
class BacktestExecutorTest {

  private final Instant time = Instant.EPOCH;
  public Random random;


  @BeforeEach
  void setUp() {
    this.random = new Random(0L);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void execute(final NumFactory numFactory) {
    final var executor = new BacktestExecutorBuilder()
        .numFactory(numFactory)
        .build();

    final var tradingStatements = executor.execute(
        getStrategyFactories(),
        getMarketEvents(),
        1.0
    );

    final var statement = tradingStatements.getFirst();
    log.info(statement.toString());
    assertNumEquals(-4.75019506722970, statement.performanceReport().totalLoss());
    assertNumEquals(718.71244689979, statement.performanceReport().totalProfit());
    assertNumEquals(713.96225183256, statement.performanceReport().totalProfitLoss());
    assertNumEquals(130.26868530393, statement.performanceReport().totalProfitLossPercentage());
  }


  private List<MarketEvent> getMarketEvents() {
    final var events = new ArrayList<MarketEvent>();
    var marketEvent = generateEvent();
    events.add(marketEvent);
    for (var i = 0; i < 10000; i++) {
      marketEvent = generateEvent(marketEvent);
      events.add(marketEvent);
    }
    return events;
  }


  private CandleReceived generateEvent() {
    return CandleReceived.builder()
        .beginTime(this.time)
        .endTime(this.time.plus(Duration.ofDays(1)))
        .closePrice(100.0)
        .build();
  }


  private CandleReceived generateEvent(final CandleReceived previous) {
    return CandleReceived.builder()
        .beginTime(previous.endTime())
        .endTime(previous.endTime().plus(Duration.ofDays(1)))
        .closePrice(Math.max(0.0, previous.closePrice() + random.nextDouble(-1.0, 1.2)))
        .build();
  }


  private static List<StrategyFactory<BacktestStrategy>> getStrategyFactories() {
    return List.of(new StrategyFactory<>() {
      @Override
      public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
      }


      @Override
      public BacktestStrategy createStrategy(
          final RuntimeContext runtimeContext,
          final IndicatorContext indicatorContext
      ) {
        return new BacktestStrategy(
            getTestedStrategy(indicatorContext),
            (BackTestTradingRecord) runtimeContext
        );
      }
    });
  }


  private static Strategy getTestedStrategy(final IndicatorContext indicatorContext) {
    return new Strategy() {
      private static final String SMA_FAST = "smaFast";
      private static final String SMA_SLOW = "smaSlow";

      private final Rule entryRule;
      private final Rule exitRule;
      private final ClosePriceIndicator closePrice = Indicators.closePrice();

      {
        indicatorContext.add(this.closePrice.sma(11), SMA_FAST);
        indicatorContext.add(this.closePrice.sma(200), SMA_SLOW);
        this.entryRule = createEntryRule();
        this.exitRule = createExitRule();
      }

      // TODO better Supplier way?
      private Rule createEntryRule() {
        final var smaFast = indicatorContext.getNumericIndicator(SMA_FAST);
        final var crossIndicator = smaFast.crossedOver(indicatorContext.getNumericIndicator(SMA_SLOW));
        indicatorContext.add(crossIndicator);
        return crossIndicator.toRule();
      }


      private Rule createExitRule() {
        final var smaFast = indicatorContext.getNumericIndicator(SMA_FAST);
        final var crossIndicator = smaFast.crossedUnder(indicatorContext.getNumericIndicator(SMA_SLOW));
        indicatorContext.add(crossIndicator);
        return crossIndicator.toRule();
      }


      @Override
      public String getName() {
        return "test-strategy";
      }


      @Override
      public Rule getEntryRule() {
        return this.entryRule;
      }


      @Override
      public Rule getExitRule() {
        return this.exitRule;
      }


      @Override
      public boolean isStable() {
        return indicatorContext.isStable();
      }
    };
  }
}
