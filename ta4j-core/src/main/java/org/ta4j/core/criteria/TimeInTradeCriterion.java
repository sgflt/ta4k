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
package org.ta4j.core.criteria;

import java.time.temporal.ChronoUnit;

import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Time in market criterion.
 *
 * <p>
 * Returns the total time in the market in seconds.
 */
public class TimeInTradeCriterion extends AbstractAnalysisCriterion {

  private final ChronoUnit unit;


  public TimeInTradeCriterion(final ChronoUnit unit) {
    this.unit = unit;
  }


  public static TimeInTradeCriterion seconds() {
    return new TimeInTradeCriterion(ChronoUnit.SECONDS);
  }


  public static TimeInTradeCriterion minutes() {
    return new TimeInTradeCriterion(ChronoUnit.MINUTES);
  }


  public static TimeInTradeCriterion hours() {
    return new TimeInTradeCriterion(ChronoUnit.HOURS);
  }


  public static TimeInTradeCriterion days() {
    return new TimeInTradeCriterion(ChronoUnit.DAYS);
  }


  @Override
  public Num calculate(final Position position) {
    if (position.isClosed()) {
      final var start = position.getEntry().getWhenExecuted();
      final var end = position.getExit().getWhenExecuted();
      return NumFactoryProvider.getDefaultNumFactory().numOf(this.unit.between(start, end));
    }

    return NumFactoryProvider.getDefaultNumFactory().zero();
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    return tradingRecord.getPositions()
        .stream()
        .filter(Position::isClosed)
        .map(this::calculate)
        .reduce(
            NumFactoryProvider.getDefaultNumFactory().zero(),
            Num::plus
        );        // FIXME it adds overlapping periods
  }


  /** The lower the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isLessThan(criterionValue2);
  }
}
