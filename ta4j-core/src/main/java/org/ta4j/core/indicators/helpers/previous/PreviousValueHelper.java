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
package org.ta4j.core.indicators.helpers.previous;

import java.util.LinkedList;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.Indicator;

/**
 * Returns the (n-th) previous value of an indicator.
 */
class PreviousValueHelper<T> implements Indicator<T> {

  private final int n;
  private final Indicator<T> indicator;
  private final LinkedList<T> previousValues = new LinkedList<>();
  private T value;
  private Bar currentBar;


  /**
   * Constructor.
   *
   * @param indicator the indicator from which to calculate the previous value
   * @param n parameter defines the previous n-th value
   */
  public PreviousValueHelper(final Indicator<T> indicator, final int n) {
    if (n < 1) {
      throw new IllegalArgumentException("n must be positive number, but was: " + n);
    }
    this.n = n;
    this.indicator = indicator;
  }


  protected T calculate() {
    final var currentValue = this.indicator.getValue();
    this.previousValues.addLast(currentValue);

    if (this.previousValues.size() > this.n) {
      return this.previousValues.removeFirst();
    }

    return null;
  }


  @Override
  public String toString() {
    return String.format("PREV(%s) => %s", this.indicator, getValue());
  }


  @Override
  public T getValue() {
    return this.value;
  }


  @Override
  public void onBar(final Bar bar) {
    if (bar != this.currentBar) {
      updateState(bar);
      this.currentBar = bar;
    }
  }


  public void updateState(final Bar bar) {
    this.indicator.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.previousValues.size() == this.n && this.value != null;
  }
}
