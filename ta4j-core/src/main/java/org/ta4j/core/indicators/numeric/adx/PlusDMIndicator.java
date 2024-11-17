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
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * +DM indicator.
 *
 * <p>
 * Part of the Directional Movement System.
 */
public class PlusDMIndicator extends NumericIndicator {

  private Bar previousBar;
  private boolean stable;


  /**
   * Constructor.
   *
   * @param numFactory the bar numFactory
   */
  public PlusDMIndicator(final NumFactory numFactory) {
    super(numFactory);
  }


  protected Num calculate(final Bar bar) {
    final var numFactory = getNumFactory();

    if (this.previousBar == null) {
      this.previousBar = bar;
      return numFactory.zero();
    }

    this.stable = true;
    final Bar prevBar = this.previousBar;

    final Num upMove = bar.highPrice().minus(prevBar.highPrice());
    final Num downMove = prevBar.lowPrice().minus(bar.lowPrice());

    this.previousBar = bar;
    if (upMove.isGreaterThan(downMove) && upMove.isGreaterThan(numFactory.zero())) {
      return upMove;
    }

    return numFactory.zero();
  }


  @Override
  public void updateState(final Bar bar) {
    this.value = calculate(bar);
  }


  @Override
  public boolean isStable() {
    return this.stable;
  }
}
