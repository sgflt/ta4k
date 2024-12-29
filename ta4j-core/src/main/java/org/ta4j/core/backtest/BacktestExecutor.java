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
package org.ta4j.core.backtest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ta4j.core.MultiTimeFrameSeries;
import org.ta4j.core.api.callback.MarketEventHandler;
import org.ta4j.core.api.callback.TickListener;
import org.ta4j.core.backtest.reports.TradingStatement;
import org.ta4j.core.backtest.reports.TradingStatementGenerator;
import org.ta4j.core.backtest.strategy.BackTestTradingRecord;
import org.ta4j.core.backtest.strategy.BacktestRun;
import org.ta4j.core.backtest.strategy.BacktestStrategy;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.events.NewsReceived;
import org.ta4j.core.events.TickReceived;
import org.ta4j.core.indicators.IndicatorContexts;
import org.ta4j.core.num.Num;
import org.ta4j.core.strategy.CompoundRuntimeContext;
import org.ta4j.core.strategy.RuntimeContext;
import org.ta4j.core.strategy.StrategyFactory;

/**
 * Allows backtesting multiple strategies and comparing them to find out which
 * is the best.
 */
public class BacktestExecutor {

  private final BacktestConfiguration configuration;


  BacktestExecutor(final BacktestConfiguration configuration) {
    this.configuration = configuration;
  }


  /**
   * Executes given strategies with specified trade type to open the position and
   * return the trading statements.
   *
   * @param backtestRun that creates strategies to test and their contexts
   * @param amount the amount used to open/close the position
   *
   * @return a list of TradingStatements
   */
  public TradingStatement execute(
      final BacktestRun backtestRun,
      final List<MarketEvent> marketEvents,
      final Number amount
  ) {
    final var marketEventHandler =
        new DefaultMarketEventHandler(backtestRun, this.configuration.numFactory().numOf(amount));

    replay(
        marketEvents,
        marketEventHandler
    );
    return marketEventHandler.getTradingStatement();

  }


  private void replay(
      final List<MarketEvent> marketEvents,
      final MarketEventHandler marketEventHandler
  ) {
    marketEvents.stream()
        .sorted(Comparator.comparing(MarketEvent::beginTime))
        .forEach(marketEvent -> {
              switch (marketEvent) {
                case final CandleReceived c -> marketEventHandler.onCandle(c);
                case final NewsReceived n -> marketEventHandler.onNews(n);
                case final TickReceived t -> marketEventHandler.onTick(t);
                default -> throw new IllegalStateException("Unexpected value: " + marketEvent);
              }
            }
        );
  }


  /**
   * Context links Strategy, TradingRecord, IndicatorContext and BarSeries.
   */
  private class DefaultMarketEventHandler implements MarketEventHandler {

    private final MultiTimeFrameSeries<BacktestBarSeries> multiTimeFrameSeries = new MultiTimeFrameSeries<>();
    private final Num amount;
    private final BacktestStrategy strategy;
    private final List<TickListener> tickListeners = new ArrayList<>();
    private RuntimeContext runtimeContext;


    public DefaultMarketEventHandler(
        final BacktestRun backtestRun,
        final Num amount
    ) {
      this.strategy = createStrategy(backtestRun);
      this.amount = amount;
    }


    private BacktestStrategy createStrategy(final BacktestRun backtestRun) {
      final var strategyFactory = backtestRun.getStrategyFactory();

      final var runtimeContext = getCompoundRuntimeContext(backtestRun, strategyFactory);
      final var indicatorContexts = IndicatorContexts.empty();
      final var strategy =
          strategyFactory.createStrategy(backtestRun.getConfiguration(), runtimeContext, indicatorContexts);

      strategy.timeFrames().forEach(
          timeFrame -> {
            final var series = new BacktestBarSeriesBuilder()
                .withIndicatorContext(indicatorContexts.get(timeFrame))
                .withNumFactory(BacktestExecutor.this.configuration.numFactory())
                .build();
            series.addBarListener(strategy);
            series.addBarListener(runtimeContext);
            this.multiTimeFrameSeries.add(series);
          }
      );

      this.runtimeContext = runtimeContext;
      this.tickListeners.add(strategy);
      this.tickListeners.add(runtimeContext);
      return strategy;
    }


    private CompoundRuntimeContext getCompoundRuntimeContext(
        final BacktestRun backtestRun,
        final StrategyFactory<BacktestStrategy> strategyFactory
    ) {
      final var backTestTradingRecord = new BackTestTradingRecord(
          strategyFactory.getTradeType(),
          BacktestExecutor.this.configuration.transactionCostModel(),
          BacktestExecutor.this.configuration.holdingCostModel(),
          BacktestExecutor.this.configuration.numFactory()
      );

      final var runtimeContextFactory = backtestRun.getRuntimeContextFactory();
      return CompoundRuntimeContext.of(
          runtimeContextFactory.createRuntimeContext(),
          backTestTradingRecord
      );
    }


    @Override
    public void onCandle(final CandleReceived event) {
      this.multiTimeFrameSeries.onCandle(event);
      reevaluate(this.amount);
    }


    private void reevaluate(final Num amount) {
      final var tradeExecutionModel = BacktestExecutor.this.configuration.tradeExecutionModel();
      switch (this.strategy.shouldOperate()) {
        case ENTER -> tradeExecutionModel.enter(
            this.runtimeContext,
            this.strategy.getTradeRecord(),
            amount
        );
        case EXIT -> tradeExecutionModel.exit(
            this.runtimeContext,
            this.strategy.getTradeRecord(),
            amount
        );
        case NOOP -> {
        }
      }
    }


    @Override
    public void onTick(final TickReceived event) {
      this.tickListeners.forEach(listener -> listener.onTick(event));
      reevaluate(this.amount);
    }


    @Override
    public void onNews(final NewsReceived event) {
      // TODO
    }


    public TradingStatement getTradingStatement() {
      return new TradingStatementGenerator().generate(this.strategy);
    }
  }
}
