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
package org.ta4j.core.live;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.api.callback.BarListener;
import org.ta4j.core.api.series.Bar;
import org.ta4j.core.api.series.BarBuilderFactory;
import org.ta4j.core.api.series.BarSeries;
import org.ta4j.core.api.series.PastCandleParadoxException;
import org.ta4j.core.api.series.WrongTimeFrameException;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.TimeFrame;
import org.ta4j.core.num.NumFactory;

/**
 * Class deigned for use as live trading backing. Stores only current bar for analysis.
 *
 * Bar addition forces recalculation of all strategies.
 *
 * @author Lukáš Kvídera
 */
class LiveBarSeries implements BarSeries {

  private final TimeFrame timeFrame;
  private final NumFactory numFactory;
  private final String name;
  private final BarBuilderFactory barBuilderFactory;
  private final List<BarListener> barListeners = new ArrayList<>();

  private Bar bar;


  LiveBarSeries(
      final String name,
      final TimeFrame timeFrame,
      final NumFactory numFactory,
      final BarBuilderFactory barBuilderFactory,
      final IndicatorContext indicatorContext
  ) {
    this.name = name;
    this.timeFrame = timeFrame;
    this.numFactory = numFactory;
    this.barBuilderFactory = barBuilderFactory;
    this.barListeners.add(indicatorContext);
  }


  @Override
  public NumFactory numFactory() {
    return this.numFactory;
  }


  @Override
  public TimeFrame timeFrame() {
    return this.timeFrame;
  }


  @Override
  public Instant getCurrentTime() {
    return this.bar == null ? Instant.EPOCH : this.bar.endTime();
  }


  @Override
  public LiveBarBuilder barBuilder() {
    return (LiveBarBuilder) this.barBuilderFactory.createBarBuilder(this);
  }


  @Override
  public String getName() {
    return this.name;
  }


  @Override
  public Bar getBar() {
    return this.bar;
  }


  @Override
  public void addBar(final Bar bar) {
    if (this.bar != null && bar.endTime().isBefore(this.bar.endTime())) {
      throw new PastCandleParadoxException(bar.endTime(), getCurrentTime());
    }

    this.bar = bar;

    for (final var barListener : this.barListeners) {
      barListener.onBar(bar);
    }
  }


  @Override
  public void addBarListener(final BarListener listener) {
    this.barListeners.add(listener);
  }


  @Override
  public void onCandle(final CandleReceived event) {
    checkTimeFrame(event);

    barBuilder()
        .startTime(event.beginTime())
        .endTime(event.beginTime())
        .openPrice(event.openPrice())
        .highPrice(event.highPrice())
        .lowPrice(event.lowPrice())
        .closePrice(event.closePrice())
        .volume(event.volume())
        .add();
  }


  private void checkTimeFrame(final CandleReceived candleReceived) {
    if (!candleReceived.timeFrame().equals(this.timeFrame)) {
      throw new WrongTimeFrameException(
          this.timeFrame,
          candleReceived.timeFrame()
      );
    }
  }
}
