/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.oscillators;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.indicators.TimeFrame;
import org.ta4j.core.indicators.numeric.oscilators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.numeric.oscilators.StochasticOscillatorKIndicator;
import org.ta4j.core.num.NumFactory;

class StochasticOscillatorDIndicatorTest {

  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void noMovement(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 8, 7, 6, 5, 4, 3, 2, 1, 2, 3, 4, 5
        );

    final var k = new StochasticOscillatorKIndicator(numFactory, 2);
    final var d = new StochasticOscillatorDIndicator(numFactory, k);

    context.withIndicator(d);

    context.fastForwardUntilStable()
        // (C - L) / (H - L) => 0/0 => 100.0
        .assertNext(100.0)
        .assertNext(100.0)
        .assertNext(100.0);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void movement(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandleDuration(ChronoUnit.MINUTES)
        .withMarketEvents(
            List.of(
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH)
                    .endTime(Instant.EPOCH.plusSeconds(60))
                    .closePrice(1)
                    .highPrice(2)
                    .lowPrice(0)
                    .build(),
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH.plusSeconds(60))
                    .endTime(Instant.EPOCH.plusSeconds(120))
                    .closePrice(2)
                    .highPrice(2)
                    .lowPrice(0)
                    .build(),
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH.plusSeconds(120))
                    .endTime(Instant.EPOCH.plusSeconds(180))
                    .closePrice(3)
                    .highPrice(3)
                    .lowPrice(0)
                    .build(),
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH.plusSeconds(180))
                    .endTime(Instant.EPOCH.plusSeconds(240))
                    .closePrice(1)
                    .highPrice(2)
                    .lowPrice(0)
                    .build()  ,
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH.plusSeconds(240))
                    .endTime(Instant.EPOCH.plusSeconds(300))
                    .closePrice(2)
                    .highPrice(12)
                    .lowPrice(0)
                    .build(),
                CandleReceived.builder()
                    .timeFrame(TimeFrame.DAY)
                    .beginTime(Instant.EPOCH.plusSeconds(300))
                    .endTime(Instant.EPOCH.plusSeconds(360))
                    .closePrice(2)
                    .highPrice(25)
                    .lowPrice(1)
                    .build()
            )
        );

    final var k = new StochasticOscillatorKIndicator(numFactory, 2);
    final var d = new StochasticOscillatorDIndicator(numFactory, k);

    context.withIndicator(d);

    context.fastForwardUntilStable()
        .assertNext(77.7777)
        .assertNext(49.9999)
        .assertNext(19.3333);
  }
}
