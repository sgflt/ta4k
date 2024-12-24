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
import org.ta4j.core.DefaultStrategy;
import org.ta4j.core.TradeType;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.api.strategy.Rule;
import org.ta4j.core.api.strategy.RuntimeContext;
import org.ta4j.core.api.strategy.Strategy;
import org.ta4j.core.api.strategy.StrategyFactory;
import org.ta4j.core.backtest.strategy.BacktestRunFactory;
import org.ta4j.core.backtest.strategy.BacktestStrategy;
import org.ta4j.core.backtest.strategy.NOOPRuntimeContextFactory;
import org.ta4j.core.backtest.strategy.RuntimeContextFactory;
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.indicators.IndicatorContext;
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
        getBacktestRunFactories(),
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
        .closePrice(Math.max(0.0, previous.closePrice() + this.random.nextDouble(-1.0, 1.2)))
        .build();
  }


  private static List<BacktestRunFactory> getBacktestRunFactories() {
    return List.of(new TestBacktestRunFactory());
  }


  private static class TestBacktestRunFactory implements BacktestRunFactory {
    @Override
    public RuntimeContextFactory getRuntimeContextFactory() {
      return new NOOPRuntimeContextFactory();
    }


    @Override
    public StrategyFactory<BacktestStrategy> getStrategyFactory() {
      return StrategyFactoryConverter.convert(new TestStrategyFactory());
    }
  }


  private static class TestStrategyFactory implements StrategyFactory<Strategy> {
    private static final String SMA_FAST = "smaFast";
    private static final String SMA_SLOW = "smaSlow";


    @Override
    public TradeType getTradeType() {
      return TradeType.BUY;
    }


    @Override
    public Strategy createStrategy(
        final RuntimeContext runtimeContext,
        final IndicatorContext indicatorContext
    ) {

      final var closePrice = Indicators.closePrice();
      indicatorContext.add(closePrice.sma(11), SMA_FAST);
      indicatorContext.add(closePrice.sma(200), SMA_SLOW);

      return DefaultStrategy.builder()
          .name("test-strategy")
          .entryRule(createEntryRule(indicatorContext))
          .exitRule(createExitRule(indicatorContext))
          .indicatorContext(indicatorContext)
          .build();
    }


    private Rule createEntryRule(final IndicatorContext indicatorContext) {
      final var smaFast = indicatorContext.getNumericIndicator(SMA_FAST);
      final var crossIndicator = smaFast.crossedOver(indicatorContext.getNumericIndicator(SMA_SLOW));
      indicatorContext.add(crossIndicator);
      return crossIndicator.toRule();
    }


    private Rule createExitRule(final IndicatorContext indicatorContext) {
      final var smaFast = indicatorContext.getNumericIndicator(SMA_FAST);
      final var crossIndicator = smaFast.crossedUnder(indicatorContext.getNumericIndicator(SMA_SLOW));
      indicatorContext.add(crossIndicator);
      return crossIndicator.toRule();
    }
  }
}
