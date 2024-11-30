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
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.num.NumFactory;

class DoubleEMAIndicatorTest {


  private MarketEventTestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new MarketEventTestContext();
    this.testContext.withCandlePrices(
        0.73,
        0.72,
        0.86,
        0.72,
        0.62,
        0.76,
        0.84,
        0.69,
        0.65,
        0.71,
        0.53,
        0.73,
        0.77,
        0.67,
        0.68
    );
  }


  @ParameterizedTest(name = "DoubleEMA with window of 5 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void doubleEMAUsingBarCount5UsingClosePrice(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var doubleEma = Indicators.closePrice().doubleEMA(5);
    this.testContext.withIndicator(doubleEma)
        .assertNext(0.73)
        .assertNext(0.7244)
        .assertNext(0.7992)
        .fastForward(4)
        .assertCurrent(0.7858)
        .assertNext(0.7374)
        .assertNext(0.6884)
        .fastForward(4)
        .assertCurrent(0.7184)
        .assertNext(0.6939)
        .assertNext(0.6859)
    ;
  }
}
