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
package org.ta4j.core.indicators.numeric.candles;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.bool.BooleanIndicator;
import org.ta4j.core.indicators.helpers.TransformIndicator;
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Doji indicator.
 *
 * <p>
 * A candle/bar is considered Doji if its body height is lower than the average
 * multiplied by a factor.
 *
 * @see <a href=
 *     "http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks#doji">
 *     http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks#doji</a>
 */
public class DojiIndicator extends BooleanIndicator {

  /** Body height. */
  private final NumericIndicator bodyHeightInd;

  /** Average body height. */
  private final PreviousNumericValueIndicator averageBodyHeightInd;

  /** The factor used when checking if a candle is Doji. */
  private final Num factor;
  private Boolean value;


  /**
   * Constructor.
   *
   * @param barCount the number of bars used to calculate the average body
   *     height
   * @param bodyFactor the factor used when checking if a candle is Doji
   */
  public DojiIndicator(final NumFactory numFactory, final int barCount, final double bodyFactor) {
    this.bodyHeightInd = TransformIndicator.abs(Indicators.realBody());
    this.averageBodyHeightInd = this.bodyHeightInd.sma(barCount).previous();
    this.factor = numFactory.numOf(bodyFactor);
  }


  protected Boolean calculate() {
    if (this.value == null) {
      return this.bodyHeightInd.getValue().isZero();
    }

    final Num averageBodyHeight = this.averageBodyHeightInd.getValue();
    final Num currentBodyHeight = this.bodyHeightInd.getValue();

    return currentBodyHeight.isLessThan(averageBodyHeight.multipliedBy(this.factor));
  }


  @Override
  public void updateState(final Bar bar) {
    this.bodyHeightInd.onBar(bar);
    this.averageBodyHeightInd.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.value != null;
  }
}
