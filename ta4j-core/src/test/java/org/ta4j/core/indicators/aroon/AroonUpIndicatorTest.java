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
package org.ta4j.core.indicators.aroon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TestContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.num.NumFactory;

class AroonUpIndicatorTest {

  private TestContext context;


  @BeforeEach
  void init() {
    this.context = new TestContext();
    // barcount before
    this.context.withCandlePrices(
        169.87,
        169.36,
        169.29,
        168.38,
        167.70,
        168.43,
        170.18,
        172.15,
        172.92,
        172.39,
        172.48,
        173.31,
        173.49,
        173.89,
        174.17,
        173.17,
        172.28,
        172.34,
        172.07,
        172.56
    )
    ;
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void upAndSlowDown(final NumFactory numFactory) {
    final var arronUp = Indicators.aroonUp(5);

    this.context.withNumFactory(numFactory)
        .withIndicators(arronUp)
        .fastForwardUntilStable()
        .assertCurrent(0)
        .assertNext(100)
        .assertNext(100)
        .assertNext(100)
        .assertNext(80)
        .assertNext(60)
        .assertNext(100)
        .assertNext(100)
        .assertNext(100)
        .assertNext(100)
        .assertNext(80)
        .assertNext(60)
        .assertNext(40)
        .assertNext(20)
        .assertNext(0)
    ;
  }
}
