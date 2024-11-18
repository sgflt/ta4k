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
package org.ta4j.core.indicators.numeric.channels.keltner;

import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.average.EMAIndicator;

/**
 * A facade to create the 3 Keltner Channel indicators. An exponential moving
 * average of close price is used as the middle channel.
 *
 * <p>
 * This class creates lightweight "fluent" numeric indicators. These objects are
 * not cached, although they may be wrapped around cached objects. Overall there
 * is less caching and probably better performance.
 */
public class KeltnerChannelFacade {

  private final EMAIndicator middle;
  private final NumericIndicator upper;
  private final NumericIndicator lower;


  /**
   * Constructor.
   *
   * @param emaCount the bar count for the {@code EmaIndicator}
   * @param atrCount the bar count for the {@code ATRIndicator}
   * @param k the multiplier for the {@link #upper} and {@link #lower}
   *     channel
   */
  public KeltnerChannelFacade(final int emaCount, final int atrCount, final Number k) {
    final var price = Indicators.closePrice();
    final var atr = Indicators.atr(atrCount);
    this.middle = price.ema(emaCount);
    this.upper = this.middle.plus(atr.multipliedBy(k));
    this.lower = this.middle.minus(atr.multipliedBy(k));
  }


  /** @return the middle channel */
  public EMAIndicator middle() {
    return this.middle;
  }


  /** @return the upper channel */
  public NumericIndicator upper() {
    return this.upper;
  }


  /** @return the lower channel */
  public NumericIndicator lower() {
    return this.lower;
  }

}
