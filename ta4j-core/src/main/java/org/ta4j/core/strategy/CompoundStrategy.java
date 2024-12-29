
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

package org.ta4j.core.strategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ta4j.core.indicators.TimeFrame;
import org.ta4j.core.strategy.rules.CompoundRule;

/**
 * Aggregates multiple strategies. Usually used for multiple timeframes.
 *
 * Larger time frame used for hint and smaller timeframe for precise entry/exit.
 */
public class CompoundStrategy implements Strategy {

  private final List<Strategy> strategies;
  private final String name;
  private final Set<TimeFrame> timeFrames;
  private final CompoundRule entry;
  private final CompoundRule exit;


  public CompoundStrategy(final Strategy... strategies) {
    this.strategies = List.of(strategies);
    this.name = this.strategies.stream().map(Strategy::name).collect(Collectors.joining(","));
    this.timeFrames = this.strategies.stream()
        .flatMap(strategy -> strategy.timeFrames().stream())
        .collect(Collectors.toSet())
    ;
    this.entry = CompoundRule.of(this.strategies.stream().map(Strategy::entryRule).toList());
    this.exit = CompoundRule.of(this.strategies.stream().map(Strategy::exitRule).toList());
  }


  public static Strategy of(final Strategy... strategies) {
    return new CompoundStrategy(strategies);
  }


  @Override
  public String name() {
    return this.name;
  }


  @Override
  public Set<TimeFrame> timeFrames() {
    return this.timeFrames;
  }


  @Override
  public Rule entryRule() {
    return this.entry;
  }


  @Override
  public Rule exitRule() {
    return this.exit;
  }


  @Override
  public boolean isStable() {
    return this.strategies.stream().allMatch(Strategy::isStable);
  }
}
