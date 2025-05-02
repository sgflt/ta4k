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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.indicators.numeric.oscilators.AccelerationDecelerationIndicator;
import org.ta4j.core.num.NumFactory;

class AccelerationDecelerationIndicatorTest {

  private MarketEventTestContext context;


  @BeforeEach
  void setUp() {
    this.context = new MarketEventTestContext()
        .withCandlePrices(1, 2, 3, 4, 5, 6);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void calculateWithSma2AndSma3(final NumFactory numFactory) {
    final var shortBarCount = 2;
    final var longBarCount = 3;

    this.context.withNumFactory(numFactory)
        .withIndicator(new AccelerationDecelerationIndicator(numFactory, shortBarCount, longBarCount));

    final var shortSma1 = (0 + 1.) / shortBarCount;
    final var longSma1 = (0 + 0 + 1.) / longBarCount;
    final var awesome1 = shortSma1 - longSma1;
    final var awesomeSma1 = (0. + awesome1) / shortBarCount;
    final var acceleration1 = awesome1 - awesomeSma1;
    this.context.assertNext(acceleration1);

    final var shortSma2 = (1. + 2.) / shortBarCount;
    final var longSma2 = (0 + 1. + 2.) / longBarCount;
    final var awesome2 = shortSma2 - longSma2;
    final var awesomeSma2 = (awesome2 + awesome1) / shortBarCount;
    final var acceleration2 = awesome2 - awesomeSma2;
    this.context.assertNext(acceleration2);


    final var shortSma3 = (2. + 3.) / shortBarCount;
    final var longSma3 = (1. + 2. + 3.) / longBarCount;
    final var awesome3 = shortSma3 - longSma3;
    final var awesomeSma3 = (awesome3 + awesome2) / shortBarCount;
    final var acceleration3 = awesome3 - awesomeSma3;
    this.context.assertNext(acceleration3);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void withSma1AndSma2(final NumFactory numFactory) {
    this.context.withNumFactory(numFactory)
        .withIndicator(new AccelerationDecelerationIndicator(numFactory, 1, 2))
        .fastForward(5)
        .assertCurrent(0);
  }
}
