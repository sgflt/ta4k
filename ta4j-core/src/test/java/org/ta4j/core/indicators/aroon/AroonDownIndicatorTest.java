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
package org.ta4j.core.indicators.aroon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TestContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.num.NumFactory;

class AroonDownIndicatorTest {

  private TestContext context;


  @BeforeEach
  void init() {
    this.context = new TestContext();
    // barcount before
    this.context.withCandlePrices(
        167.15,  // 0 -> unstable
        168.20,  // 1 -> unstable
        166.41,  // 0 -> unstable
        166.18,  // 0 -> unstable
        166.33,  // 1 -> unstable
        165.00,  // 0 -> (5 - 0) / 5 * 100 = 100
        167.63,  // 1 -> (5 - 1) / 5 * 100 = 80
        171.97,  // 2 -> (5 - 2) / 5 * 100 = 60
        171.31,  // 3 -> (5 - 3) / 5 * 100 = 40
        169.55,  // 4 -> (5 - 4) / 5 * 100 = 20
        169.57,  // 5 -> (5 - 5) / 5 * 100 = 0
        170.27,
        170.80,
        172.20,
        175.00,
        172.06,
        170.50,
        170.26,
        169.34,
        170.36
    );
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void upDownAndHigh(final NumFactory numFactory) {
    final var aroonDownIndicator = Indicators.aroonDown(5);
    this.context.withIndicator(aroonDownIndicator)
        .withNumFactory(numFactory)
        .fastForwardUntilStable()
        .assertCurrent(100)
        .assertNext(80)
        .assertNext(60)
        .assertNext(40)
        .assertNext(20)
        .assertNext(0)
        .assertNext(0)
        .assertNext(40)
        .assertNext(20)
        .assertNext(0)
        .assertNext(0)
        .assertNext(0)
        .assertNext(100)
        .assertNext(100)
        .assertNext(80)
    ;
  }
}
