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
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.api.series.Bar;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Close Location Value (CLV) indicator.
 *
 * @see <a href="https://www.investopedia.com/terms/c/close_location_value.asp">Close Location Value</a>
 */
public class CloseLocationValueIndicator extends NumericIndicator {

  public CloseLocationValueIndicator(final NumFactory numFactory) {
    super(numFactory);
  }


  private Num calculate(final Bar bar) {
    final Num low = bar.lowPrice();
    final Num high = bar.highPrice();
    final Num close = bar.closePrice();

    final Num diffHighLow = high.minus(low);

    return diffHighLow.isNaN() || diffHighLow.isZero()
           ? getNumFactory().zero()
           : ((close.minus(low)).minus(high.minus(close))).dividedBy(diffHighLow);
  }


  @Override
  public boolean isStable() {
    return true;
  }


  @Override
  protected void updateState(final Bar bar) {
    this.value = calculate(bar);
  }
}
