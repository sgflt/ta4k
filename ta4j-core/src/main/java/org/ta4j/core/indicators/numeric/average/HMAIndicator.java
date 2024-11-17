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
package org.ta4j.core.indicators.numeric.average;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.helpers.CombineIndicator;
import org.ta4j.core.indicators.helpers.TransformIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * Hull moving average (HMA) indicator.
 *
 * @see <a href="http://alanhull.com/hull-moving-average">
 *     http://alanhull.com/hull-moving-average</a>
 */
public class HMAIndicator extends NumericIndicator {

  private final int barCount;
  private final WMAIndicator sqrtWma;


  /**
   * Constructor.
   *
   * @param indicator the {@link Indicator}
   * @param barCount the time frame
   */
  public HMAIndicator(final NumericIndicator indicator, final int barCount) {
    super(indicator.getNumFactory());
    this.barCount = barCount;

    final var halfWma = indicator.wma(barCount / 2);
    final var origWma = indicator.wma(barCount);

    final var indicatorForSqrtWma = CombineIndicator.minus(TransformIndicator.multiply(halfWma, 2), origWma);
    this.sqrtWma = indicatorForSqrtWma.wma(getNumFactory().numOf(barCount).sqrt().intValue());
  }


  protected Num calculate() {
    return this.sqrtWma.getValue();
  }


  @Override
  public void updateState(final Bar bar) {
    this.sqrtWma.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.sqrtWma.isStable();
  }


  @Override
  public String toString() {
    return String.format("HMA(%d) => %s", this.barCount, getValue());
  }

}
