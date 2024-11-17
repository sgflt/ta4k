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
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.helpers.previous;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.bool.BooleanIndicator;

/**
 * @author Lukáš Kvídera
 */
public class PreviousBooleanValueIndicator extends BooleanIndicator {


  private final PreviousValueHelper<Boolean> previousValueHelper;


  public PreviousBooleanValueIndicator(final BooleanIndicator indicator, final int n) {
    this.previousValueHelper = new PreviousValueHelper<>(indicator, n);
  }


  private Boolean calculate() {
    final var value = this.previousValueHelper.getValue();
    return value != null && value;
  }


  @Override
  public void updateState(final Bar bar) {
    this.previousValueHelper.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.previousValueHelper.isStable();
  }
}
