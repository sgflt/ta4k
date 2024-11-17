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
package org.ta4j.core.reports;

import org.ta4j.core.backtest.BacktestStrategy;

/**
 * Generates a {@link TradingStatement} based on the provided trading record and
 * bar series.
 */
public class TradingStatementGenerator {

  private final PerformanceReportGenerator performanceReportGenerator;
  private final PositionStatsReportGenerator positionStatsReportGenerator;


  /**
   * Constructor with new {@link PerformanceReportGenerator} and new
   * {@link PositionStatsReportGenerator}.
   */
  public TradingStatementGenerator() {
    this(new PerformanceReportGenerator(), new PositionStatsReportGenerator());
  }


  /**
   * Constructor.
   *
   * @param performanceReportGenerator the {@link PerformanceReportGenerator}
   * @param positionStatsReportGenerator the {@link PositionStatsReportGenerator}
   */
  public TradingStatementGenerator(
      final PerformanceReportGenerator performanceReportGenerator,
      final PositionStatsReportGenerator positionStatsReportGenerator
  ) {
    this.performanceReportGenerator = performanceReportGenerator;
    this.positionStatsReportGenerator = positionStatsReportGenerator;
  }


  public TradingStatement generate(final BacktestStrategy strategy) {
    final PerformanceReport performanceReport = this.performanceReportGenerator.generate(strategy.getTradeRecord());
    final PositionStatsReport positionStatsReport =
        this.positionStatsReportGenerator.generate(strategy.getTradeRecord());
    return new TradingStatement(strategy, positionStatsReport, performanceReport);
  }
}
