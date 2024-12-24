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

package org.ta4j.core;

import org.ta4j.core.api.series.Bar;
import org.ta4j.core.api.strategy.RuntimeContext;
import org.ta4j.core.api.strategy.RuntimeValueResolver;
import org.ta4j.core.events.TickReceived;

/**
 * A RuntimeContext that combines multiple other RuntimeContexts.
 * When querying values, it checks contexts in order until it finds a non-null result.
 */
public final class CompoundRuntimeContext implements RuntimeContext {
  private final RuntimeContext primary;
  private final RuntimeContext secondary;


  private CompoundRuntimeContext(final RuntimeContext primary, final RuntimeContext secondary) {
    this.primary = primary;
    this.secondary = secondary;
  }


  /**
   * Creates a new CompoundRuntimeContext by combining two RuntimeContexts.
   *
   * @param primary the primary context to check first
   * @param secondary the secondary context to check if primary returns null
   *
   * @return a new CompoundRuntimeContext
   */
  public static CompoundRuntimeContext of(final RuntimeContext primary, final RuntimeContext secondary) {
    return new CompoundRuntimeContext(primary, secondary);
  }


  @Override
  public <T> T getValue(final RuntimeValueResolver<T> resolver) {
    final var primaryValue = resolver.resolve(this.primary);
    return primaryValue != null ? primaryValue : resolver.resolve(this.secondary);
  }


  @Override
  public Object getValue(final String key) {
    final var value = this.primary.getValue(key);
    return value != null ? value : this.secondary.getValue(key);
  }


  @Override
  public void onBar(final Bar bar) {
    this.primary.onBar(bar);
    this.secondary.onBar(bar);
  }


  @Override
  public void onTick(final TickReceived tick) {
    this.primary.onTick(tick);
    this.secondary.onTick(tick);
  }
}
