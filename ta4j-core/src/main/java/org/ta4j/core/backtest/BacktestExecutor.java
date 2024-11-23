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

import org.ta4j.core.Strategy;
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
   * @param tradeType the {@link Trade.TradeType} used to open the position
   *
   * @return a list of TradingStatements
   */
  public List<TradingStatement> execute(
      final List<StrategyFactory> strategyFactories,
      final List<MarketEvent> marketEvents,
      final Number amount,
      final Trade.TradeType tradeType
  ) {
    final var indicatorContext = IndicatorContext.empty();
    final var series = new BacktestBarSeriesBuilder()
        .withIndicatorContext(indicatorContext)
        .build();

    final var runtimeContext =
        new DefaultRuntimeContext(series, strategyFactories, this.configuration.numFactory().numOf(amount));

    replay(
        marketEvents,
        runtimeContext
    );

    return runtimeContext.getTradintStatements();
  }


  private void replay(
      final List<MarketEvent> marketEvents,
      final RuntimeContext runtimeContext
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


  private class DefaultRuntimeContext implements RuntimeContext {

    private final BacktestBarSeries series;
    private final Num amount;
    private final List<Strategy> strategies;


    public DefaultRuntimeContext(
        final BacktestBarSeries series,
        final List<StrategyFactory> strategyFactories,
        final Num amount
    ) {
      this.series = series;
      this.strategies =
          strategyFactories.stream()
              .map(s -> {
                    final Strategy strategy = s.createStrategy(series);
                    ((BacktestStrategy) strategy).register(new BackTestTradingRecord(
                            Trade.TradeType.BUY,    // TODO sell
                            BacktestExecutor.this.configuration.transactionCostModel(),
                            BacktestExecutor.this.configuration.holdingCostModel()
                        )
                    );
                    return strategy;
                  }
              )
              .toList();
      this.amount = amount;
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

        if (((BacktestStrategy) strategy).shouldOperate()) {
          BacktestExecutor.this.configuration.tradeExecutionModel().execute(
              barSeries.getBar(),
              ((BacktestStrategy) strategy).getTradeRecord(),
              amount
          );
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
          .map(strategy -> new TradingStatementGenerator().generate((BacktestStrategy) strategy))
          .toList();
    }
  }
}
