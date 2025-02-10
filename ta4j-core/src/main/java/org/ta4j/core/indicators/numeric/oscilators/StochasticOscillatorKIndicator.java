/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.oscilators;

import org.ta4j.core.api.Indicator;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.api.series.Bar;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator;
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Stochastic oscillator K.
 */
public class StochasticOscillatorKIndicator extends NumericIndicator {

  private final Indicator<Num> indicator;
  private final HighestValueIndicator highestHigh;
  private final LowestValueIndicator lowestMin;


  /**
   * Constructor with:
   *
   * <ul>
   * <li>{@code indicator} = {@link ClosePriceIndicator}
   * <li>{@code highPriceIndicator} = {@link HighPriceIndicator}
   * <li>{@code lowPriceIndicator} = {@link LowPriceIndicator}
   * </ul>
   *
   * @param numFactory the bar series
   * @param barCount the time frame
   */
  public StochasticOscillatorKIndicator(final NumFactory numFactory, final int barCount) {
    this(
        numFactory,
        Indicators.closePrice(),
        barCount,
        Indicators.highPrice(),
        Indicators.lowPrice()
    );
  }


  /**
   * Constructor.
   *
   * @param indicator the {@link Indicator}
   * @param barCount the time frame
   * @param highPriceIndicator the {@link HighPriceIndicator}
   * @param lowPriceIndicator the {@link LowPriceIndicator}
   */
  public StochasticOscillatorKIndicator(
      final NumFactory numFactory,
      final Indicator<Num> indicator,
      final int barCount,
      final HighPriceIndicator highPriceIndicator,
      final LowPriceIndicator lowPriceIndicator
  ) {
    super(numFactory);
    this.indicator = indicator;
    this.highestHigh = highPriceIndicator.highest(barCount);
    this.lowestMin = lowPriceIndicator.lowest(barCount);
  }


  private Num calculate() {
    final Num highestHighPrice = this.highestHigh.getValue();
    final Num lowestLowPrice = this.lowestMin.getValue();

    return this.indicator.getValue()
        .minus(lowestLowPrice)
        .dividedBy(highestHighPrice.minus(lowestLowPrice))
        .multipliedBy(getNumFactory().hundred());
  }


  @Override
  protected void updateState(final Bar bar) {
    this.indicator.onBar(bar);
    this.highestHigh.onBar(bar);
    this.lowestMin.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.indicator.isStable() && this.highestHigh.isStable() && this.lowestMin.isStable();
  }
}
