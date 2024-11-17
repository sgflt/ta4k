/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.indicators.helpers;

import java.util.Deque;
import java.util.LinkedList;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * Lowest value indicator.
 *
 * <p>
 * Returns the lowest indicator value from the bar series within the bar count.
 */
public class LowestValueIndicator extends NumericIndicator {

  private final NumericIndicator indicator;
  private final int barCount;

  /** circular array */
  private final Num[] window;
  private final Deque<Integer> deque = new LinkedList<>();
  private Num value;
  private int barsPassed;


  /**
   * Constructor.
   *
   * @param indicator the {@link Indicator}
   * @param barCount the time frame
   */
  public LowestValueIndicator(final NumericIndicator indicator, final int barCount) {
    super(indicator.getNumFactory());
    this.indicator = indicator;
    this.barCount = barCount;
    this.window = new Num[barCount];
  }


  protected Num calculate() {
    final int actualIndex = this.barsPassed % this.barCount;

    if (this.barsPassed >= this.barCount) {
      final int outgoingIndex = (this.barsPassed - this.barCount) % this.barCount;
      if (!this.deque.isEmpty() && this.deque.peekFirst() == outgoingIndex) {
        this.deque.pollFirst();
      }
    }

    final var currentValue = this.indicator.getValue();
    if (currentValue.isNaN()) {
      return currentValue;
    }

    this.window[actualIndex] = currentValue;

    while (!this.deque.isEmpty() && (
        this.window[this.deque.peekLast()].isGreaterThan(currentValue)
        || this.window[this.deque.peekLast()].isNaN()
    )) {
      this.deque.pollLast();
    }

    this.deque.offerLast(actualIndex);
    this.barsPassed++;

    return this.window[this.deque.peekFirst()];
  }


  @Override
  public void updateState(final Bar bar) {
    this.indicator.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.barsPassed >= this.barCount && this.indicator.isStable();
  }


  @Override
  public String toString() {
    return String.format("LoVa(%s, %s) => %s", this.indicator, this.barCount, getValue());
  }
}
