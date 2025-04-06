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
package org.ta4j.core.indicators.numeric.statistics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.num.NumFactory;

class VarianceIndicatorTest {
  private MarketEventTestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new MarketEventTestContext();
    this.testContext.withCandlePrices(1, 2, 3, 4, 3, 4, 5, 4, 3, 0, 9);
  }


  @ParameterizedTest(name = "VAR(4) [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void varianceUsingBarCount4UsingClosePrice(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var varianceIndicator = Indicators.closePrice().variance(4);
    this.testContext.withIndicator(varianceIndicator);

    // unstable values may produce garbage, this is why they are called unstable
    this.testContext.assertNext(0.0);
    assertFalse(varianceIndicator.isStable());

    this.testContext.assertNext(0.1667);
    assertFalse(varianceIndicator.isStable());

    this.testContext.assertNext(0.6667);
    assertFalse(varianceIndicator.isStable());

    // stable date bellow
    this.testContext.assertNext(1.6667);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.6667);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.3333);
    this.testContext.assertNext(0.6667);
    this.testContext.assertNext(0.6667);
    this.testContext.assertNext(0.6667);
    this.testContext.assertNext(4.6667);
    this.testContext.assertNext(14.000);
  }


  @Test
  void varianceShouldBeZeroWhenBarCountIs1() {
    assertThrows(IllegalArgumentException.class, () -> Indicators.closePrice().variance(1));
  }


  @ParameterizedTest(name = "VAR(2) [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void varianceUsingBarCount2UsingClosePrice(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var varianceIndicator = Indicators.closePrice().variance(2);
    this.testContext.withIndicator(varianceIndicator);

    this.testContext.assertNext(0.0);
    assertFalse(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    assertTrue(varianceIndicator.isStable());

    this.testContext.assertNext(0.5);
    this.testContext.assertNext(0.5);
    this.testContext.assertNext(0.5);
    this.testContext.assertNext(4.5);
    this.testContext.assertNext(40.5);
  }
}
