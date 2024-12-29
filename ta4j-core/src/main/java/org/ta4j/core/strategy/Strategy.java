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

import java.util.Set;

import org.ta4j.core.indicators.TimeFrame;

/**
 * A {@code Strategy} (also called "trading strategy") is a pair of
 * complementary (entry and exit) {@link Rule rules}. It may recommend to enter
 * or to exit. Recommendations are based respectively on the entry rule or on
 * the exit rule.
 */
public interface Strategy {

  /**
   * @return the name of the strategy
   */
  String name();

  /**
   * Which timeframes this strategy accepts.
   *
   * Leaf strategy wil return exactly one element.
   *
   * Root strategy that aggregates multiple timeframe restricted strategies will return all timeframes of child
   * strategies.
   *
   * @return timeframes that this strategy expects
   */
  Set<TimeFrame> timeFrames();

  /**
   * @return the entry rule
   */
  Rule entryRule();

  /**
   * @return the exit rule
   */
  Rule exitRule();

  /**
   * @return true if this strategy is stable at current moment, false otherwise
   *     (unstable)
   */
  boolean isStable();

  /**
   * @return true to recommend to enter, false otherwise
   */
  default boolean shouldEnter() {
    return isStable() && entryRule().isSatisfied();
  }


  /**
   * @return true to recommend to exit, false otherwise
   */
  default boolean shouldExit() {
    return isStable() && exitRule().isSatisfied();
  }
}
