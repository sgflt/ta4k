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

import java.util.ArrayList;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.utils.CircularNumArray;

/**
 * Linearly Weighted Moving Average (LWMA) indicator.
 *
 * @see <a href=
 *     "https://www.investopedia.com/terms/l/linearlyweightedmovingaverage.asp">
 *     https://www.investopedia.com/terms/l/linearlyweightedmovingaverage.asp</a>
 */
public class LWMAIndicator extends NumericIndicator {

  private final NumericIndicator indicator;
  private final int barCount;
  private final CircularNumArray values;
  private final ArrayList<Num> weights;
  private final Num denominator;


  /**
   * Constructor.
   *
   * @param indicator the {@link Indicator}
   * @param barCount the time frame
   */
  public LWMAIndicator(final NumericIndicator indicator, final int barCount) {
    super(indicator.getNumFactory());
    this.indicator = indicator;
    this.barCount = barCount;
    this.values = new CircularNumArray(barCount);
    this.weights = new ArrayList<>(barCount);
    final var numFactory = getNumFactory();
    for (int i = 1; i < barCount + 1; i++) {
      this.weights.add(numFactory.numOf(i));
    }
    this.denominator = numFactory.numOf(this.weights.stream().mapToInt(Num::intValue).sum());
  }


  protected Num calculate() {
    final var numFactory = getNumFactory();
    Num sum = numFactory.zero();

    this.values.addLast(this.indicator.getValue());

    if (this.values.isNotFull()) {
      return numFactory.zero();
    }

    int count = 0;
    for (final var val : this.values) {
      final var weight = this.weights.get(count++);
      sum = sum.plus(val.multipliedBy(weight));
    }

    return sum.dividedBy(this.denominator);
  }


  @Override
  public void updateState(final Bar bar) {
    this.indicator.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return false;
  }


  @Override
  public String toString() {
    return String.format("LWMA(%d) => %s", this.barCount, getValue());
  }
}
