/**
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
package ta4jexamples.backtesting;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.backtest.BacktestExecutorBuilder;
import org.ta4j.core.backtest.BacktestStrategy;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DoubleNumFactory;
import org.ta4j.core.num.NumFactoryProvider;
import org.ta4j.core.reports.TradingStatement;

public class SimpleMovingAverageBacktest {

  public static void main(final String[] args) throws InterruptedException {
    final var numFactory = DoubleNumFactory.getInstance();
    final var backtestExecutor = new BacktestExecutorBuilder().numFactory(numFactory).build();
    final List<TradingStatement> tradingStatements = backtestExecutor.execute(
        List.of(
            SimpleMovingAverageBacktest::create2DaySmaStrategy,
            SimpleMovingAverageBacktest::create3DaySmaStrategy
        ),
        createCandlEvents(),
        50,
        Trade.TradeType.BUY
    );
    System.out.println(tradingStatements);

    final var criterion = new ReturnCriterion();
    tradingStatements.stream()
        .map(tradingStatement -> criterion.calculate(tradingStatement.getStrategy().getTradeRecord()))
        .forEach(sum -> System.out.println(sum));
  }


  private static List<MarketEvent> createCandlEvents() {
    final var candles = new ArrayList<MarketEvent>();
    candles.add(createCandle(createDay(1), 100.0, 100.0, 100.0, 100.0, 1060));
    candles.add(createCandle(createDay(1), 100.0, 100.0, 100.0, 100.0, 1060));
    candles.add(createCandle(createDay(2), 110.0, 110.0, 110.0, 110.0, 1070));
    candles.add(createCandle(createDay(3), 140.0, 140.0, 140.0, 140.0, 1080));
    candles.add(createCandle(createDay(4), 119.0, 119.0, 119.0, 119.0, 1090));
    candles.add(createCandle(createDay(5), 100.0, 100.0, 100.0, 100.0, 1100));
    candles.add(createCandle(createDay(6), 110.0, 110.0, 110.0, 110.0, 1110));
    candles.add(createCandle(createDay(7), 120.0, 120.0, 120.0, 120.0, 1120));
    candles.add(createCandle(createDay(8), 130.0, 130.0, 130.0, 130.0, 1130));
    return candles;
  }


  private static CandleReceived createCandle(
      final Instant start,
      final double open,
      final double high,
      final double low,
      final double close,
      final int volume
  ) {
    return new CandleReceived(
        Duration.ofDays(1),
        start,
        NumFactoryProvider.getDefaultNumFactory().numOf(open),
        NumFactoryProvider.getDefaultNumFactory().numOf(high),
        NumFactoryProvider.getDefaultNumFactory().numOf(low),
        NumFactoryProvider.getDefaultNumFactory().numOf(close),
        NumFactoryProvider.getDefaultNumFactory().numOf(volume),
        NumFactoryProvider.getDefaultNumFactory().zero()
    );
  }


  private static Instant createDay(final int day) {
    return Instant.EPOCH.plus(Duration.ofDays(day));
  }


  private static Strategy create3DaySmaStrategy(final BarSeries series) {
    final var closePrice = NumericIndicator.closePrice(series);
    final var sma = closePrice.sma(3);
    return new BacktestStrategy(
        "",
        Rule.of(() -> sma.isLessThan(closePrice)),
        Rule.of(() -> sma.isGreaterThan(closePrice)),
        IndicatorContext.of(sma)
    );
  }


  private static Strategy create2DaySmaStrategy(final BarSeries series) {
    final var closePrice = NumericIndicator.closePrice(series);
    final var sma = closePrice.sma(2);
    return new BacktestStrategy(
        "",
        Rule.of(() -> sma.isLessThan(closePrice)),
        Rule.of(() -> sma.isGreaterThan(closePrice)),
        IndicatorContext.of(sma)
    );
  }
}
