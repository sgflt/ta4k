/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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
package org.ta4j.core.indicators.bool;

import java.time.Instant;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitShortIndicator;
import org.ta4j.core.indicators.helpers.previous.PreviousBooleanValueIndicator;
import org.ta4j.core.num.NumFactoryProvider;
import org.ta4j.core.rules.BooleanIndicatorRule;

public abstract class BooleanIndicator implements Indicator<Boolean> {

  private Instant currentBeginTime = Instant.MIN;

  protected boolean value;


  public BooleanIndicatorRule toRule() {
    return new BooleanIndicatorRule(this);
  }


  public PreviousBooleanValueIndicator previous(final int barCount) {
    return new PreviousBooleanValueIndicator(this, barCount);
  }


  public static ChandelierExitLongIndicator chandelierExitLong(
      final int barCount,
      final double coefficient
  ) {
    return new ChandelierExitLongIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount, coefficient);
  }


  public static ChandelierExitShortIndicator chandelierExitShort(
      final int barCount,
      final double coefficient
  ) {
    return new ChandelierExitShortIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount, coefficient);
  }


  @Override
  public final Boolean getValue() {
    return this.value;
  }


  @Override
  public final void onBar(final Bar bar) {
    if (bar.beginTime().isAfter(this.currentBeginTime)) {
      updateState(bar);
      this.currentBeginTime = bar.beginTime();
    }
  }


  /**
   * Updates internal stqte of indicator.
   *
   * If indicator depends on other indicators, it is required to call {@link #onBar(Bar)} on them to refresh their state
   * before calculation
   *
   * @param bar that comes from exchange's stream
   */
  protected abstract void updateState(final Bar bar);
}
