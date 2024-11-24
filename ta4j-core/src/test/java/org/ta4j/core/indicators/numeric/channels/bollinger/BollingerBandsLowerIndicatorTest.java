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
import org.ta4j.core.TestContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.num.NumFactory;

class BollingerBandsLowerIndicatorTest {

  private int barCount;


  private TestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new TestContext();
    this.testContext.withCandlePrices(
        1, 2, 3, 4, 3, 4, 5, 4, 3, 3, 4, 3, 2
    );
    this.barCount = 3;
  }


  @ParameterizedTest(name = "Lower bollinger band derived from STDEV of SMA [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void bollingerBandsLowerUsingSMAAndStandardDeviation(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePrice = Indicators.closePrice();
    final var sma = closePrice.sma(this.barCount);
    final var bbmSMA = new BollingerBandsMiddleIndicator(sma);
    final var standardDeviation = closePrice.stddev(this.barCount);
    final var bblSMA = new BollingerBandsLowerIndicator(bbmSMA, standardDeviation);

    this.testContext.withIndicator(bblSMA, "bblSMA")
        .fastForwardUntilStable()
        .onIndicator("bblSMA")
        .assertCurrent(0.0000)
        .assertNext(1.0000)
        .assertNext(2.1786)
        .assertNext(2.5119)
        .assertNext(2.0000)
        .assertNext(3.1786)
        .assertNext(2.0000)
        .assertNext(2.1786)
        .assertNext(2.1786)
        .assertNext(2.1786)
        .assertNext(1.0000)
    ;
  }


  @ParameterizedTest(name = "Lower bollinger band derived from STDEV of SMA with smaller K = 1.5 [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void bollingerBandsLowerUsingSMAAndStandardDeviationWithK(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePrice = Indicators.closePrice();
    final var sma = closePrice.sma(this.barCount);
    final var bbmSMA = new BollingerBandsMiddleIndicator(sma);
    final var standardDeviation = closePrice.stddev(this.barCount);
    final var bblSMA = new BollingerBandsLowerIndicator(bbmSMA, standardDeviation, numFactory.numOf(1.5));

    this.testContext.withIndicator(bblSMA, "bblSMA")
        .fastForwardUntilStable()
        .onIndicator("bblSMA")
        .assertCurrent(0.5000)
        .assertNext(1.5000)
        .assertNext(2.4673)
        .assertNext(2.8006)
        .assertNext(2.5000)
        .assertNext(3.4673)
        .assertNext(2.5000)
        .assertNext(2.4673)
        .assertNext(2.4673)
        .assertNext(2.4673)
        .assertNext(1.5000)
    ;
  }
}
