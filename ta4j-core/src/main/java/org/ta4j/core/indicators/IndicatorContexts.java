
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

package org.ta4j.core.indicators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ta4j.core.strategy.ObservableStrategyFactoryBuilder;

/**
 * Aggregation class that stores indicator contexts related to defined timeframes.
 */
public class IndicatorContexts {
  private final Map<TimeFrame, IndicatorContext> timeFramedContexts = new HashMap<>();


  private IndicatorContexts() {
  }


  public static IndicatorContexts empty() {
    return new IndicatorContexts();
  }


  public void add(final IndicatorContext context) {
    this.timeFramedContexts.put(context.timeFrame(), context);
  }


  public IndicatorContext get(final TimeFrame timeFrame) {
    return this.timeFramedContexts.computeIfAbsent(timeFrame, IndicatorContext::empty);
  }


  public void register(final ObservableStrategyFactoryBuilder.ObservableStrategy observableStrategy) {
    this.timeFramedContexts.values().forEach(indicatorContext -> indicatorContext.register(observableStrategy));
  }


  public Set<TimeFrame> timeFrames() {
    return this.timeFramedContexts.keySet();
  }


  public boolean isEmpty() {
    return this.timeFramedContexts.isEmpty();
  }
}
