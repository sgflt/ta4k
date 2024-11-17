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
package org.ta4j.core.indicators.numeric.statistics;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * Correlation coefficient indicator.
 *
 * @see <a href=
 *     "https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/correlation-coefficient">
 *     https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/correlation-coefficient</a>
 */
public class CorrelationCoefficientIndicator extends NumericIndicator {

  private final VarianceIndicator variance1;
  private final VarianceIndicator variance2;
  private final CovarianceIndicator covariance;


  /**
   * Constructor.
   *
   * @param indicator1 the first indicator
   * @param indicator2 the second indicator
   * @param barCount the time frame
   */
  public CorrelationCoefficientIndicator(
      final NumericIndicator indicator1,
      final NumericIndicator indicator2,
      final int barCount
  ) {
    super(indicator1.getNumFactory());
    this.variance1 = indicator1.variance(barCount);
    this.variance2 = indicator2.variance(barCount);
    this.covariance = indicator1.covariance(indicator2, barCount);
  }


  protected Num calculate() {
    final var cov = this.covariance.getValue();
    final var var1 = this.variance1.getValue();
    final var var2 = this.variance2.getValue();
    final var multipliedSqrt = var1.multipliedBy(var2).sqrt();
    return cov.dividedBy(multipliedSqrt);
  }


  @Override
  public void updateState(final Bar bar) {
    this.variance1.onBar(bar);
    this.variance2.onBar(bar);
    this.covariance.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.variance1.isStable() && this.variance2.isStable() && this.covariance.isStable();
  }
}
