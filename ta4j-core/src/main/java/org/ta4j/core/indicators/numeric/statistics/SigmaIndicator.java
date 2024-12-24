/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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

import org.ta4j.core.api.series.Bar;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.average.SMAIndicator;
import org.ta4j.core.num.Num;

/**
 * Sigma-Indicator (also called, "z-score" or "standard score").
 */
public class SigmaIndicator extends NumericIndicator {

  private final NumericIndicator ref;

  private final SMAIndicator mean;
  private final StandardDeviationIndicator sd;


  /**
   * Constructor.
   *
   * @param ref the indicator
   * @param barCount the time frame
   */
  public SigmaIndicator(final NumericIndicator ref, final int barCount) {
    super(ref.getNumFactory());
    this.ref = ref;
    this.mean = ref.sma(barCount);
    this.sd = ref.stddev(barCount);
  }


  protected Num calculate() {
    if (this.sd.getValue().isZero()) {
      return getNumFactory().one();
    }

    // z-score = (ref - mean) / sd
    return this.ref.getValue().minus(this.mean.getValue()).dividedBy(this.sd.getValue());
  }


  @Override
  public void updateState(final Bar bar) {
    this.ref.onBar(bar);
    this.mean.onBar(bar);
    this.sd.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.ref.isStable() && this.mean.isStable() && this.sd.isStable();
  }
}
