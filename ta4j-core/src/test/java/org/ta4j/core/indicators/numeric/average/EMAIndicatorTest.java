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
package org.ta4j.core.indicators.numeric.average;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.indicators.XLSIndicatorTest;
import org.ta4j.core.num.NumFactory;

class EMAIndicatorTest {

  private MarketEventTestContext context;


  @BeforeEach
  void setUp() {
    this.context = new MarketEventTestContext()
        .withCandlePrices(
            64.75, 63.79, 63.73, 63.73, 63.55, 63.19,
            63.91, 63.85, 62.95, 63.37, 61.33, 61.51
        );
  }


  @ParameterizedTest(name = "First value equals first data value [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void firstValueShouldBeEqualsToFirstDataValue(final NumFactory factory) {
    this.context.withNumFactory(factory)
        .withIndicator(Indicators.closePrice().ema(1))
        .assertNext(64.75);
  }


  @ParameterizedTest(name = "EMA with barCount 10 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testEmaWithBarCount10(final NumFactory factory) {
    this.context.withNumFactory(factory)
        .withIndicator(Indicators.closePrice().ema(10))
        .fastForwardUntilStable()
        .assertCurrent(63.6948)
        .assertNext(63.2648)
        .assertNext(62.9457);
  }


  @ParameterizedTest(name = "External data test [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testExternalData(final NumFactory numFactory) throws Exception {
    final var xls = new XLSIndicatorTest(this.getClass(), "EMA.xls", 6, numFactory);
    final var xlsContext = new MarketEventTestContext();
    xlsContext.withMarketEvents(xls.getMarketEvents());

    final var indicator = Indicators.closePrice().ema(1);
    final var expectedIndicator = xls.getIndicator(1);
    xlsContext.withIndicators(indicator, expectedIndicator)
        .assertIndicatorEquals(expectedIndicator, indicator)
        .assertCurrent(329.0);
  }


  @ParameterizedTest(name = "External data with bar count 3 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testExternalDataBarCount3(final NumFactory numFactory) throws Exception {
    final var xls = new XLSIndicatorTest(this.getClass(), "EMA.xls", 6, numFactory);
    final var xlsContext = new MarketEventTestContext();
    xlsContext.withMarketEvents(xls.getMarketEvents());

    final var indicator = Indicators.closePrice().ema(3);
    final var expectedIndicator = xls.getIndicator(3);
    xlsContext.withIndicators(indicator, expectedIndicator)
        .assertIndicatorEquals(expectedIndicator, indicator)
        .assertCurrent(327.7748);
  }


  @ParameterizedTest(name = "External data with bar count 13 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testExternalDataBarCount13(final NumFactory numFactory) throws Exception {
    final var xls = new XLSIndicatorTest(this.getClass(), "EMA.xls", 6, numFactory);
    final var xlsContext = new MarketEventTestContext();
    xlsContext.withMarketEvents(xls.getMarketEvents());

    final var indicator = Indicators.closePrice().ema(13);
    final var expectedIndicator = xls.getIndicator(13);
    xlsContext.withIndicators(indicator, expectedIndicator)
        .assertIndicatorEquals(expectedIndicator, indicator)
        .assertCurrent(327.4076);
  }
}
