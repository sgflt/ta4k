/*
 * The MIT License (MIT)
 *
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
package org.ta4j.core.indicators.numeric;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.mocks.MockMarketEventBuilder;
import org.ta4j.core.num.NumFactory;

class CloseLocationValueIndicatorTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void getValue(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandleDuration(ChronoUnit.DAYS)
        .withMarketEvents(
            new MockMarketEventBuilder()
                .candle()
                .closePrice(18).highPrice(20).lowPrice(10)
                .add()
                .candle()
                .closePrice(20).highPrice(21).lowPrice(17)
                .add()
                .candle()
                .closePrice(15).highPrice(16).lowPrice(14)
                .add()
                .candle()
                .closePrice(11).highPrice(15).lowPrice(8)
                .add()
                .candle()
                .closePrice(12).highPrice(12).lowPrice(10)
                .add()
                .build()
        );

    final var indicator = new CloseLocationValueIndicator(numFactory);
    context.withIndicator(indicator);

    context
        .assertNext(0.6)
        .assertNext(0.5)
        .assertNext(0)
        .assertNext(-1d / 7)
        .assertNext(1);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void returnZeroIfHighEqualsLow(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandleDuration(ChronoUnit.DAYS)
        .withMarketEvents(
            new MockMarketEventBuilder()
                .candle()
                .closePrice(10).highPrice(10).lowPrice(10)
                .add()
                .candle()
                .closePrice(12).highPrice(12).lowPrice(10)
                .add()
                .candle()
                .closePrice(120).highPrice(140).lowPrice(100)
                .add()
                .build()
        );

    final var indicator = new CloseLocationValueIndicator(numFactory);
    context.withIndicator(indicator);

    context
        .assertNext(0)
        .assertNext(1)
        .assertNext(0);
  }
}
