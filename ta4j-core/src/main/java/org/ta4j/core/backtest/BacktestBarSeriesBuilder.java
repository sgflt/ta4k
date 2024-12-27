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
package org.ta4j.core.backtest;

import java.util.List;

import org.ta4j.core.api.series.BarBuilderFactory;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.TimeFrame;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * A builder to build a new {@link BacktestBarSeries}.
 */
public class BacktestBarSeriesBuilder {

  private String name = "UNDEFINED";
  private NumFactory numFactory = NumFactoryProvider.getDefaultNumFactory();
  private BarBuilderFactory barBuilderFactory = new BacktestBarBuilderFactory();
  private TimeFrame timeFrame = TimeFrame.DAY;
  private IndicatorContext indicatorContext = IndicatorContext.empty(this.timeFrame);


  /**
   * Sets numFactory as default for whole runtime.
   *
   * @param numFactory to set {@link BacktestBarSeries#numFactory()}
   *
   * @return {@code this}
   */
  public BacktestBarSeriesBuilder withNumFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    NumFactoryProvider.setDefaultNumFactory(numFactory);
    return this;
  }


  /**
   * @param name to set {@link BacktestBarSeries#getName()}
   *
   * @return {@code this}
   */
  public BacktestBarSeriesBuilder withName(final String name) {
    this.name = name;
    return this;
  }


  /**
   * Mutually exclusive with {@link #withIndicatorContext(IndicatorContext)}
   */
  public BacktestBarSeriesBuilder withTimeFrame(final TimeFrame timeFrame) {
    this.timeFrame = timeFrame;
    this.indicatorContext = IndicatorContext.empty(timeFrame);
    return this;
  }


  /**
   * @param barBuilderFactory to build bars with the same datatype as series
   *
   * @return {@code this}
   */
  public BacktestBarSeriesBuilder withBarBuilderFactory(final BarBuilderFactory barBuilderFactory) {
    this.barBuilderFactory = barBuilderFactory;
    return this;
  }


  /**
   * Mutually exclusive with {@link #withTimeFrame(TimeFrame)}
   */
  public BacktestBarSeriesBuilder withIndicatorContext(final IndicatorContext indicatorContext) {
    this.indicatorContext = indicatorContext;
    this.timeFrame = indicatorContext.timeFrame();
    return this;
  }


  public BacktestBarSeries build() {
    return new BacktestBarSeries(
        this.name,
        this.timeFrame,
        this.numFactory,
        this.barBuilderFactory,
        List.of(this.indicatorContext)
    );
  }
}
