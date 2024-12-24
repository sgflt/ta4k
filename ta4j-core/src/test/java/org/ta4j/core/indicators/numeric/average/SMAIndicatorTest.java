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

class SMAIndicatorTest {


  private MarketEventTestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new MarketEventTestContext();
    this.testContext.withCandlePrices(1, 2, 3, 4, 3, 4, 5, 4, 3, 3, 4, 3, 2);
  }


  @ParameterizedTest(name = "SMA 3 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void usingBarCount3UsingClosePrice(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var indicator = Indicators.closePrice().sma(3);
    this.testContext.withIndicators(indicator)
        .assertNext((0d + 0d + 1d) / 3)
        .assertNext((0d + 1d + 2d) / 3)
        .assertNext((1d + 2d + 3d) / 3)
        .assertNext(3)
        .assertNext(10d / 3)
        .assertNext(11d / 3)
        .assertNext(4)
        .assertNext(13d / 3)
        .assertNext(4)
        .assertNext(10d / 3)
        .assertNext(10d / 3)
        .assertNext(10d / 3)
        .assertNext(3)
    ;
  }


  @ParameterizedTest(name = "SMA 1 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void whenBarCountIs1ResultShouldBeIndicatorValue(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);
    final var closePrice = Indicators.closePrice();
    final var indicator = closePrice.sma(1);

    this.testContext.withIndicators(indicator)
        .assertIndicatorEquals(closePrice, indicator);
  }


  @ParameterizedTest(name = "SMA 3 external data [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void externalData3(final NumFactory numFactory) throws Exception {
    externalData(numFactory, 326.6333, 3);
  }


  @ParameterizedTest(name = "SMA 13 external data [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void externalData13(final NumFactory numFactory) throws Exception {
    externalData(numFactory, 327.7846, 13);
  }


  private void externalData(final NumFactory numFactory, final double expectedLastValue, final int barCount)
      throws Exception {
    final var xlsContext = new MarketEventTestContext();
    xlsContext.withNumFactory(numFactory);
    final var xls = new XLSIndicatorTest(getClass(), "SMA.xls", 6, numFactory);
    xlsContext.withMarketEvents(xls.getMarketEvents());

    final var actualIndicator = Indicators.closePrice().sma(barCount);
    final var expectedIndicator = xls.getIndicator(barCount);
    xlsContext.withIndicators(actualIndicator, expectedIndicator)
        .fastForwardUntilStable()
        .assertIndicatorEquals(expectedIndicator, actualIndicator)
        .assertCurrent(expectedLastValue);
  }

}
