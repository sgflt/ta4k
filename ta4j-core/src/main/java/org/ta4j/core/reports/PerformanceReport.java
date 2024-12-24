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

import org.ta4j.core.num.Num;

/**
 * Represents a report that contains performance statistics.
 *
 * @param totalProfitLoss The total PnL.
 * @param totalProfitLossPercentage The total PnL in percent.
 * @param totalProfit The total profit.
 * @param totalLoss The total loss.
 * @param averageLoss
 */
public record PerformanceReport(
    Num totalProfitLoss,
    Num totalProfitLossPercentage,
    Num totalProfit,
    Num totalLoss,
    Num averageProfit,
    Num averageLoss,
    Num totalPositions,
    Num winningPositions,
    Num losingPositions,
    Num maximumDrawdown
) {
  @Override
  public String toString() {
    return """
        totalProfitLoss: %.2f
        totalProfitLossPercentage: %.2f
        totalProfit: %.2f
        totalLoss: %.2f
        averageProfit: %.2f
        averageLoss: %.2f
        totalPositions: %.0f
        winningPositions: %.0f
        losingPositions: %.0f
        maximumDrawdown: %.2f
        """.formatted(
        this.totalProfitLoss.doubleValue(),
        this.totalProfitLossPercentage.doubleValue(),
        this.totalProfit.doubleValue(),
        this.totalLoss.doubleValue(),
        this.averageProfit.doubleValue(),
        this.averageLoss.doubleValue(),
        this.totalPositions.doubleValue(),
        this.winningPositions.doubleValue(),
        this.losingPositions.doubleValue(),
        this.maximumDrawdown.doubleValue()
    );
  }
}
