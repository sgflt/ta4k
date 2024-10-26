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
package org.ta4j.core.indicators.numeric.statistics;

import static org.ta4j.core.TestUtils.assertNext;
import static org.ta4j.core.TestUtils.fastForward;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.MockStrategy;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.mocks.MockBarSeriesBuilder;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class CorrelationCoefficientIndicatorTest extends AbstractIndicatorTest<Num> {

  private NumericIndicator close;
  private NumericIndicator volume;
  private BacktestBarSeries data;


  public CorrelationCoefficientIndicatorTest(final NumFactory numFactory) {
    super(numFactory);
  }


  @Before
  public void setUp() {
    this.data = new MockBarSeriesBuilder().withNumFactory(this.numFactory).build();
    this.data.barBuilder().closePrice(6).volume(100).add();
    this.data.barBuilder().closePrice(7).volume(105).add();
    this.data.barBuilder().closePrice(9).volume(130).add();
    this.data.barBuilder().closePrice(12).volume(160).add();
    this.data.barBuilder().closePrice(11).volume(150).add();
    this.data.barBuilder().closePrice(10).volume(130).add();
    this.data.barBuilder().closePrice(11).volume(95).add();
    this.data.barBuilder().closePrice(13).volume(120).add();
    this.data.barBuilder().closePrice(15).volume(180).add();
    this.data.barBuilder().closePrice(12).volume(160).add();
    this.data.barBuilder().closePrice(8).volume(150).add();
    this.data.barBuilder().closePrice(4).volume(200).add();
    this.data.barBuilder().closePrice(3).volume(150).add();
    this.data.barBuilder().closePrice(4).volume(85).add();
    this.data.barBuilder().closePrice(3).volume(70).add();
    this.data.barBuilder().closePrice(5).volume(90).add();
    this.data.barBuilder().closePrice(8).volume(100).add();
    this.data.barBuilder().closePrice(9).volume(95).add();
    this.data.barBuilder().closePrice(11).volume(110).add();
    this.data.barBuilder().closePrice(10).volume(95).add();

    this.close = NumericIndicator.closePrice(this.data);
    this.volume = NumericIndicator.volume(this.data);
  }


  @Test
  public void usingBarCount5UsingClosePriceAndVolume() {
    final var coef = new CorrelationCoefficientIndicator(this.close, this.volume, 5);
    this.data.replaceStrategy(new MockStrategy(coef));

    fastForward(this.data, 5);
    assertNext(this.data, 0.7977, coef);
    assertNext(this.data, 0.7842, coef);
    assertNext(this.data, 0.2536, coef);
    assertNext(this.data, 0.0548, coef);
    assertNext(this.data, 0.4841, coef);
    assertNext(this.data, 0.5219, coef);
    assertNext(this.data, 0.2436, coef);
    assertNext(this.data, -0.3662, coef);
    assertNext(this.data, 0.02518, coef);
    assertNext(this.data, 0.1169, coef);
    assertNext(this.data, 0.2139, coef);
    assertNext(this.data, -0.0481, coef);
    assertNext(this.data, -0.0853, coef);
    assertNext(this.data, 0.6913, coef);
    assertNext(this.data, 0.7473, coef);
    assertNext(this.data, 0.5843, coef);
  }
}
