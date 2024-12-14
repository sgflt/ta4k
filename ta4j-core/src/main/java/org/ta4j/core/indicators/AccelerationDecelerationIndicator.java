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
package org.ta4j.core.indicators;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.average.SMAIndicator;
import org.ta4j.core.indicators.numeric.oscilators.AwesomeOscillatorIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Acceleration-deceleration indicator.
 */
public class AccelerationDecelerationIndicator extends NumericIndicator {

  private final AwesomeOscillatorIndicator awesome;
  private final SMAIndicator sma;


  /**
   * Constructor.
   *
   * @param numFactory the numFactory
   * @param shortBarCount the bar count for {@link #awesome}
   * @param longBarCount the bar count for {@link #sma}
   */
  public AccelerationDecelerationIndicator(
      final NumFactory numFactory,
      final int shortBarCount,
      final int longBarCount
  ) {
    super(numFactory);
    this.awesome = Indicators.awesomeOscillator(shortBarCount, longBarCount);
    this.sma = this.awesome.sma(shortBarCount);
  }


  /**
   * Constructor with {@code barCountSma1} = 5 and {@code barCountSma2} = 34.
   *
   * @param numFactory the numFactory
   */
  public AccelerationDecelerationIndicator(final NumFactory numFactory) {
    this(numFactory, 5, 34);
  }


  protected Num calculate() {
    return this.awesome.getValue().minus(this.sma.getValue());
  }


  @Override
  public void updateState(final Bar bar) {
    this.awesome.onBar(bar);
    this.sma.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.awesome.isStable() && this.sma.isStable();
  }
}
