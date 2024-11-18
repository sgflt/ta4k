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
package org.ta4j.core.mocks;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Generates BacktestBar implementations with mocked time or duration if not set
 * by tester.
 */
public class MockMarketEventBuilder {

  private static final Clock clock = Clock.fixed(Instant.ofEpochMilli(-1), ZoneId.systemDefault());
  private boolean defaultData;
  private NumFactory numFactory = NumFactoryProvider.getDefaultNumFactory();
  private List<CandleReceived> candleEvents = new ArrayList<>();


  public MockMarketEventBuilder withNumFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    return this;
  }


  /**
   * Generates bars with given close prices.
   *
   * @param data close prices
   *
   * @return this
   */
  public MockMarketEventBuilder withCandleClosePrices(final List<Double> data) {

    data.forEach(d -> {
      this.candleEvents.add(
          new CandleReceived(
              Duration.ofDays(1),
              Instant.now(clock),
              this.numFactory.numOf(d),
              this.numFactory.numOf(d),
              this.numFactory.numOf(d),
              this.numFactory.numOf(d),
              this.numFactory.numOf(d),
              this.numFactory.zero()
          )
      );
    });

    return this;
  }


  /**
   * Generates bars with given close prices.
   *
   * @param data close prices
   *
   * @return this
   */
  public MockMarketEventBuilder withCandlePrices(final double... data) {
    withCandlePrices(DoubleStream.of(data).boxed().toList());
    return this;
  }


  public MockMarketEventBuilder withDefaultData() {
    this.defaultData = true;
    return this;
  }


  private void arbitraryBars() {
    final var candleEvents = new ArrayList<CandleReceived>();
    for (double i = 0d; i < 5000; i++) {
      candleEvents.add(
          new CandleReceived(
              Duration.ofDays(1),
              Instant.now(clock),
              this.numFactory.numOf(i + 1),
              this.numFactory.numOf(i + 2),
              this.numFactory.numOf(i + 3),
              this.numFactory.numOf(i + 4),
              this.numFactory.numOf(i + 5),
              this.numFactory.zero()
          )
      );
    }

    this.candleEvents = candleEvents;
  }


  public List<MarketEvent> build() {
    if (this.defaultData) {
      arbitraryBars();
    }

    return List.copyOf(this.candleEvents);
  }
}
