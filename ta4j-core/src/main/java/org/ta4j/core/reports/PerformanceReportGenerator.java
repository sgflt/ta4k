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

import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.LossCriterion;
import org.ta4j.core.criteria.pnl.ProfitCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;

/**
 * Generates a {@link PerformanceReport} based on the provided trading record
 * and bar series.
 */
public class PerformanceReportGenerator implements ReportGenerator<PerformanceReport> {

  @Override
  public PerformanceReport generate(final TradingRecord tradingRecord) {
    final var pnl = new ProfitLossCriterion().calculate(tradingRecord);
    final var pnlPercentage = new ProfitLossPercentageCriterion().calculate(tradingRecord);
    final var netProfit = new ProfitCriterion(false).calculate(tradingRecord);
    final var netLoss = new LossCriterion(false).calculate(tradingRecord);
    return new PerformanceReport(pnl, pnlPercentage, netProfit, netLoss);
  }
}
