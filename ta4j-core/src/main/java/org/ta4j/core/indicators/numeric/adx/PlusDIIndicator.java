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
import org.ta4j.core.indicators.numeric.ATRIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.average.MMAIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * +DI indicator.
 *
 * <p>
 * Part of the Directional Movement System.
 *
 * @see <a href=
 *     "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx">
 *     http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx</a>
 * @see <a href=
 *     "https://www.investopedia.com/terms/a/adx.asp">https://www.investopedia.com/terms/a/adx.asp</a>
 */
public class PlusDIIndicator extends NumericIndicator {

  private final int barCount;
  private final ATRIndicator atrIndicator;
  private final MMAIndicator avgPlusDMIndicator;
  private int barsPassed;


  /**
   * Constructor.
   *
   * @param numFactory the bar numFactory
   * @param barCount the bar count for {@link #atrIndicator} and
   *     {@link #avgPlusDMIndicator}
   */
  public PlusDIIndicator(final NumFactory numFactory, final int barCount) {
    super(numFactory);
    this.barCount = barCount;
    this.atrIndicator = NumericIndicator.atr(barCount);
    this.avgPlusDMIndicator = NumericIndicator.plusDMI().mma(barCount);
  }


  protected Num calculate() {
    return this.avgPlusDMIndicator.getValue()
        .dividedBy(this.atrIndicator.getValue())
        .multipliedBy(getNumFactory().hundred());
  }


  @Override
  public void updateState(final Bar bar) {
    ++this.barsPassed;
    this.atrIndicator.onBar(bar);
    this.avgPlusDMIndicator.onBar(bar);
    this.value = calculate();
  }


  public boolean isStable() {
    return this.barsPassed >= this.barCount && this.atrIndicator.isStable() && this.avgPlusDMIndicator.isStable();
  }


  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + this.atrIndicator + " " + this.avgPlusDMIndicator;
  }
}
