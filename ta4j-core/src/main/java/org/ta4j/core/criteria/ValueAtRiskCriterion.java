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
package org.ta4j.core.criteria;

import java.util.ArrayList;

import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.Returns;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.num.Num;

/**
 * Value at Risk criterion, returned in decimal format.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Value_at_risk">Value at Risk</a>
 */
public class ValueAtRiskCriterion extends AbstractAnalysisCriterion {

  private final BacktestBarSeries series;
  /** Confidence level as absolute value (e.g. 0.95). */
  private final double confidence;


  /**
   * Constructor.
   *
   * @param confidence the confidence level
   */
  public ValueAtRiskCriterion(final BacktestBarSeries series, final double confidence) {
    this.series = series;
    this.confidence = confidence;
  }


  @Override
  public Num calculate(final Position position) {
    if (!position.isClosed()) {
      return position.getEntry().getNetPrice().getNumFactory().zero();
    }

    final var returns = new Returns(this.series, position, Returns.ReturnType.ARITHMETIC);
    return calculateVaR(returns);
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    final var returns = new Returns(this.series, tradingRecord, Returns.ReturnType.ARITHMETIC);
    return calculateVaR(returns);
  }


  /**
   * Calculates the VaR on the return series.
   *
   * @param returns the corresponding returns
   * @return the relative Value at Risk
   */
  private Num calculateVaR(final Returns returns) {
    final var zero = returns.getBarSeries().numFactory().zero();
    final var returnRates = new ArrayList<>(returns.getValues().subList(1, returns.getSize() + 1));

    if (returnRates.isEmpty()) {
      return zero;
    }

    // F(x_var) >= alpha (=1-confidence)
    final var nInBody = (int) (returns.getSize() * this.confidence);
    final var nInTail = returns.getSize() - nInBody;

    returnRates.sort(null);
    var valueAtRisk = returnRates.get(nInTail - 1);

    // VaR is non-positive
    if (valueAtRisk.isGreaterThan(zero)) {
      valueAtRisk = zero;
    }

    return valueAtRisk;
  }


  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    // because it represents a loss, VaR is non-positive
    return criterionValue1.isGreaterThan(criterionValue2);
  }
}
