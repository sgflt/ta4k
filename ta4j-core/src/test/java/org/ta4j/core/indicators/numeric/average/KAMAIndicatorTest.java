/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
import org.ta4j.core.TestContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.num.NumFactory;

/**
 * The Class KAMAIndicatorTest.
 *
 * @see <a
 *     href="http://stockcharts.com/school/data/media/chart_school/technical_indicators_and_overlays/kaufman_s_adaptive_moving_average/cs-kama.xls>
 *     http://stockcharts.com/school/data/media/chart_school/technical_indicators_and_overlays/kaufman_s_adaptive_moving_average/cs-kama.xls</a>
 */
class KAMAIndicatorTest {


  private TestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new TestContext();
    this.testContext.withCandlePrices(
        110.46, 109.80, 110.17, 109.82, 110.15, 109.31, 109.05, 107.94, 107.76, 109.24, 109.40,
        108.50, 107.96, 108.55, 108.85, 110.44, 109.89, 110.70, 110.79, 110.22, 110.00, 109.27, 106.69,
        107.07, 107.92, 107.95, 107.70, 107.97, 106.09, 106.03, 107.65, 109.54, 110.26, 110.38, 111.94,
        113.59, 113.98, 113.91, 112.62, 112.20, 111.10, 110.18, 111.13, 111.55, 112.08, 111.95, 111.60,
        111.39, 112.25
    );
  }


  @ParameterizedTest(name = "KAMA [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void kama(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);
    final var kama = Indicators.closePrice().kama(10, 2, 30);

    this.testContext.withIndicator(kama)
        .fastForward(10)
        .assertCurrent(109.2400)
        .assertNext(109.2449)
        .assertNext(109.2165)
        .assertNext(109.1173)
        .assertNext(109.0981)
        .assertNext(109.0894)
        .assertNext(109.1240)
        .assertNext(109.1376)
        .assertNext(109.2769)
        .assertNext(109.4365)
        .assertNext(109.4569)
        .assertNext(109.4651)
        .assertNext(109.4612)
        .assertNext(109.3904)
        .assertNext(109.3165)
        .assertNext(109.2924)
        .assertNext(109.1836)
        .assertNext(109.0778)
        .assertNext(108.9498)
        .assertNext(108.4230)
        .assertNext(108.0157)
        .assertNext(107.9967)
        .assertNext(108.0069)
        .assertNext(108.2596)
        .assertNext(108.4818)
        .assertNext(108.9119)
        .assertNext(109.6734)
        .assertNext(110.4947)
        .assertNext(111.1077)
        .assertNext(111.4622)
        .assertNext(111.6092)
        .assertNext(111.5663)
        .assertNext(111.5491)
        .assertNext(111.5425)
        .assertNext(111.5426)
        .assertNext(111.5457)
        .assertNext(111.5658)
        .assertNext(111.5688)
        .assertNext(111.5522)
        .assertNext(111.5595)
    ;
  }
}
