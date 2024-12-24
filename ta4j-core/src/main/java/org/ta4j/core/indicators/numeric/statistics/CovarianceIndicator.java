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

import java.util.LinkedList;
import java.util.Queue;

import org.ta4j.core.api.series.Bar;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * Covariance indicator.
 */
public class CovarianceIndicator extends NumericIndicator {

  private final NumericIndicator indicator1;
  private final NumericIndicator indicator2;
  private final Queue<XY> window = new LinkedList<>();
  private Num sumX;
  private Num sumXY;
  private Num sumY;
  private final int barCount;


  /**
   * Constructor.
   *
   * @param indicator1 the first indicator
   * @param indicator2 the second indicator
   * @param barCount the time frame
   */
  public CovarianceIndicator(final NumericIndicator indicator1, final NumericIndicator indicator2, final int barCount) {
    super(indicator1.getNumFactory());
    this.indicator1 = indicator1;
    this.indicator2 = indicator2;
    this.barCount = barCount;
    this.sumX = getNumFactory().zero();
    this.sumXY = getNumFactory().zero();
    this.sumY = getNumFactory().zero();
  }


  protected Num calculate() {
    final var x = this.indicator1.getValue();
    final var y = this.indicator2.getValue();

    final var newValue = new XY(x, y);
    this.window.offer(newValue);

    if (this.window.size() > this.barCount) {
      final var polled = this.window.poll();
      removeOldPoint(polled);
    }

    // Update the mean and covariance
    return updateMeanAndCovariance(newValue);
  }


  private void removeOldPoint(final XY polled) {
    this.sumX = this.sumX.minus(polled.x());
    this.sumY = this.sumY.minus(polled.y());
    this.sumXY = this.sumXY.minus(polled.x().multipliedBy(polled.y()));
  }


  private Num updateMeanAndCovariance(final XY newValue) {
    this.sumX = this.sumX.plus(newValue.x());
    this.sumY = this.sumY.plus(newValue.y());
    this.sumXY = this.sumXY.plus(newValue.x().multipliedBy(newValue.y()));

    final var divisor = getNumFactory().numOf(this.window.size());
    final var meanX = this.sumX.dividedBy(divisor);
    final var meanY = this.sumY.dividedBy(divisor);

    return this.sumXY.dividedBy(divisor).minus(meanX.multipliedBy(meanY));
  }


  @Override
  public void updateState(final Bar bar) {
    this.indicator1.onBar(bar);
    this.indicator2.onBar(bar);
    this.value = calculate();
    this.value = this.window.isEmpty()
                 ? getNumFactory().zero()
                 : this.value;
  }


  @Override
  public boolean isStable() {
    return this.window.size() == this.barCount && this.indicator1.isStable() && this.indicator2.isStable();
  }


  @Override
  public String toString() {
    return String.format("COV(%d) => %s", this.barCount, getValue());
  }


  private record XY(Num x, Num y) {

  }
}
