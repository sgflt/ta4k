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
package org.ta4j.core.indicators.numeric.adx;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * DX indicator.
 *
 * <p>
 * Part of the Directional Movement System.
 */
public class DXIndicator extends NumericIndicator {

  private final PlusDIIndicator plusDIIndicator;
  private final MinusDIIndicator minusDIIndicator;


  /**
   * Constructor.
   *
   * @param series the bar series
   * @param barCount the bar count for {@link #plusDIIndicator} and
   *     {@link #minusDIIndicator}
   */
  public DXIndicator(final BarSeries series, final int barCount) {
    super(series.numFactory());
    this.plusDIIndicator = Indicators.plusDII(barCount);
    this.minusDIIndicator = Indicators.minusDII(barCount);
  }


  protected Num calculate() {
    final Num pdiValue = this.plusDIIndicator.getValue();
    final Num mdiValue = this.minusDIIndicator.getValue();
    if (pdiValue.plus(mdiValue).equals(getNumFactory().zero())) {
      return getNumFactory().zero();
    }
    return pdiValue.minus(mdiValue)
        .abs()
        .dividedBy(pdiValue.plus(mdiValue))
        .multipliedBy(getNumFactory().hundred());
  }


  @Override
  public void updateState(final Bar bar) {
    this.plusDIIndicator.onBar(bar);
    this.minusDIIndicator.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.plusDIIndicator.isStable() && this.minusDIIndicator.isStable();
  }


  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + this.plusDIIndicator + " " + this.minusDIIndicator;
  }
}
