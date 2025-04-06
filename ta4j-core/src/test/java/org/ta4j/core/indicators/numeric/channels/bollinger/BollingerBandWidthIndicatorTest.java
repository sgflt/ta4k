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
package org.ta4j.core.indicators.numeric.channels.bollinger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.num.NumFactory;

class BollingerBandWidthIndicatorTest {


  private MarketEventTestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new MarketEventTestContext();
    this.testContext.withCandlePrices(10, 12, 15, 14, 17, 20, 21, 20, 20, 19, 20, 17, 12, 12, 9, 8, 9, 10, 9, 10);
  }


  @ParameterizedTest(name = "Bollinger bands width [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void bollingerBandWidthUsingSMAAndStandardDeviation(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var bbf = Indicators.bollingerBands(5, 2);
    final var bandwidth = bbf.getBandwidth();

    this.testContext.withIndicators(bandwidth)
        .fastForwardUntilStable()
        .assertCurrent(79.4662)
        .assertNext(78.1946)
        .assertNext(70.1055)
        .assertNext(62.6298)
        .assertNext(30.9505)
        .assertNext(14.1421)
        .assertNext(14.1421)
        .assertNext(27.1633)
        .assertNext(76.3989)
        .assertNext(95.1971)
        .assertNext(126.1680)
        .assertNext(120.9357)
        .assertNext(74.8331)
        .assertNext(63.1906)
        .assertNext(31.4270)
        .assertNext(36.3766)
    ;
  }
}
