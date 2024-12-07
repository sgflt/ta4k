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

import java.util.Comparator;
import java.util.List;

import org.ta4j.core.StrategyFactory;
import org.ta4j.core.Trade;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.events.NewsReceived;
import org.ta4j.core.events.TickReceived;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

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
   * @param strategyFactories that creates strategies to test
   * @param amount the amount used to open/close the position
   *
   * @return a list of TradingStatements
   */
  public List<TradingStatement> execute(
      final List<StrategyFactory<BacktestStrategy>> strategyFactories,
      final List<MarketEvent> marketEvents,
      final Number amount
  ) {
    final var marketEventHandler =
        new DefaultMarketEventHandler(strategyFactories, this.configuration.numFactory().numOf(amount));

    replay(
        marketEvents,
        marketEventHandler
    );

    return marketEventHandler.getTradintStatements();
  }


  private void replay(
      final List<MarketEvent> marketEvents,
      final MarketEventHandler runtimeContext
  ) {

    marketEvents.stream()
        .sorted(Comparator.comparing(MarketEvent::beginTime))
        .forEach(marketEvent -> {
              switch (marketEvent) {
                case final CandleReceived c -> runtimeContext.onCandle(c);
                case final NewsReceived n -> runtimeContext.onNews(n);
                case final TickReceived t -> runtimeContext.onTick(t);
                default -> throw new IllegalStateException("Unexpected value: " + marketEvent);
              }
            }
        );
  }


  /**
   * Context links Strategy, TradingRecord, IndicatorContext and BarSeries.
   */
  private class DefaultMarketEventHandler implements MarketEventHandler {

    private final BacktestBarSeries series;
    private final Num amount;
    private final List<BacktestStrategy> strategies;


    public DefaultMarketEventHandler(
        final List<StrategyFactory<BacktestStrategy>> strategyFactories,
        final Num amount
    ) {

      final var indicatorContext = IndicatorContext.empty();
      this.series = new BacktestBarSeriesBuilder()
          .withIndicatorContext(indicatorContext)
          .withNumFactory(BacktestExecutor.this.configuration.numFactory())
          .build();

      this.strategies =
          strategyFactories.stream()
              .map(s -> createStrategy(s, indicatorContext))
              .toList();

      this.amount = amount;
    }


    private BacktestStrategy createStrategy(
        final StrategyFactory<BacktestStrategy> s,
        final IndicatorContext indicatorContext
    ) {
      final var backTestTradingRecord = new BackTestTradingRecord(
          Trade.TradeType.BUY,    // TODO sell
          BacktestExecutor.this.configuration.transactionCostModel(),
          BacktestExecutor.this.configuration.holdingCostModel(),
          BacktestExecutor.this.configuration.numFactory()
      );

      final var strategy = s.createStrategy(backTestTradingRecord, indicatorContext);
      this.series.addListener(strategy);
      return strategy;
    }


    @Override
    public void onCandle(final CandleReceived event) {
      this.series.barBuilder()
          .endTime(event.beginTime())  // FIXME
          .openPrice(event.openPrice())
          .highPrice(event.highPrice())
          .lowPrice(event.lowPrice())
          .closePrice(event.closePrice())
          .volume(event.volume())
          .add()
      ;

      reevaluate(this.series, this.amount);
    }


    private void reevaluate(final BacktestBarSeries barSeries, final Num amount) {
      this.strategies.forEach(strategy -> {

        final var tradeExecutionModel = BacktestExecutor.this.configuration.tradeExecutionModel();
        switch (strategy.shouldOperate()) {
          case ENTER -> tradeExecutionModel.enter(
              barSeries.getBar(),
              strategy.getTradeRecord(),
              amount
          );
          case EXIT -> tradeExecutionModel.exit(
              barSeries.getBar(),
              strategy.getTradeRecord(),
              amount
          );
          case NOOP -> {
          }
        }
      });
    }


    @Override
    public void onTick(final TickReceived event) {
      // TODO
    }


    @Override
    public void onNews(final NewsReceived event) {
      // TODO
    }


    public List<TradingStatement> getTradintStatements() {
      return this.strategies.stream()
          .map(strategy -> new TradingStatementGenerator().generate(strategy))
          .toList();
    }
  }
}
