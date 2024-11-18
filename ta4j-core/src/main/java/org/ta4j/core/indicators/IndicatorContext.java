
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.ta4j.core.Bar;

/**
 * @author Lukáš Kvídera
 */
public class IndicatorContext {
  private final List<Indicator<?>> indicators;
  private final Set<IndicatorChangeListener> changeListeners = new HashSet<>();


  private IndicatorContext(final Indicator<?>... indicators) {
    this.indicators = List.of(indicators);
  }


  private IndicatorContext() {
    this.indicators = new ArrayList<>();
  }


  /**
   * Creates immutable context with defined indicators.
   *
   * @param indicators that will be refreshed
   *
   * @return instance of {@link IndicatorContext}
   */
  public static IndicatorContext of(final Indicator<?>... indicators) {
    return new IndicatorContext(indicators);
  }


  /**
   * Creates mutable context with defined indicators.
   *
   * @return mutable instance of {@link IndicatorContext}
   */
  public static IndicatorContext empty() {
    return new IndicatorContext();
  }


  public void register(final IndicatorChangeListener changeListener) {
    this.changeListeners.add(changeListener);
  }


  public void add(final Indicator<?> indicator) {
    this.indicators.add(indicator);
  }


  public Indicator<?> get(final int index) {
    return this.indicators.get(index);
  }


  public void addAll(final Indicator<?>... indicator) {
    this.indicators.addAll(List.of(indicator));
  }


  public void refresh(final Bar bar) {
    for (final var indicator : this.indicators) {
      indicator.onBar(bar);
      for (final var changeListener : this.changeListeners) {
        changeListener.accept(bar.beginTime(), indicator);
      }
    }
  }


  public void inspect(final Consumer<NamedIndicator<?>> consumer) {
    for (final var indicator : this.indicators) {
      if (indicator instanceof final NamedIndicator<?> namedIndicator) {
        consumer.accept(namedIndicator);
      }
    }
  }


  public boolean isStable() {
    return this.indicators.stream().allMatch(Indicator::isStable);
  }


  public boolean isNotEmpty() {
    return !this.indicators.isEmpty();
  }
}
