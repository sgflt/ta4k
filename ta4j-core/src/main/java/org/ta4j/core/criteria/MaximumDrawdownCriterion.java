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

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Maximum drawdown criterion, returned in decimal format.
 *
 * <p>
 * The maximum drawdown measures the largest loss. Its value can be within the
 * range of [0,1], e.g. a maximum drawdown of {@code +1} (= +100%) means a total
 * loss, a maximum drawdown of {@code 0} (= 0%) means no loss at all.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Drawdown_%28economics%29">
 *      https://en.wikipedia.org/wiki/Drawdown_(economics)</a>
 */
public class MaximumDrawdownCriterion implements AnalysisCriterion {

  private final NumFactory numFactory;


  /**
   * Constructor.
   *
   * @param numFactory the factory for creating number instances
   */
  public MaximumDrawdownCriterion(final NumFactory numFactory) {
    this.numFactory = numFactory;
  }


  @Override
  public Num calculate(final Position position) {
    if (position == null || position.getEntry() == null) {
      return this.numFactory.zero();
    }

    final var cashFlow = new CashFlow(this.numFactory, position);
    return calculateMaximumDrawdown(cashFlow);
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord == null || tradingRecord.isEmpty()) {
      return this.numFactory.zero();
    }

    final var cashFlow = new CashFlow(this.numFactory, tradingRecord);
    return calculateMaximumDrawdown(cashFlow);
  }


  /** The lower the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isLessThan(criterionValue2);
  }


  /**
   * Calculates the maximum drawdown from a cash flow.
   *
   * The formula is:
   * MDD = (LP - PV) / PV
   * where:
   * MDD: Maximum drawdown, in percent
   * LP: Lowest point (lowest value after peak value)
   * PV: Peak value (highest value within the observation period)
   *
   * @param cashFlow the cash flow to analyze
   * @return the maximum drawdown during the period
   */
  private Num calculateMaximumDrawdown(final CashFlow cashFlow) {
    final var values = cashFlow.getValues();
    if (values.isEmpty()) {
      return this.numFactory.zero();
    }

    var maxPeak = this.numFactory.one();
    var maximumDrawdown = this.numFactory.zero();

    for (final var value : values.values()) {
      // Update peak if we have a new high
      if (value.isGreaterThan(maxPeak)) {
        maxPeak = value;
      }

      // Calculate drawdown from peak
      final var drawdown = maxPeak.minus(value).dividedBy(maxPeak);
      if (drawdown.isGreaterThan(maximumDrawdown)) {
        maximumDrawdown = drawdown;
      }
    }

    return maximumDrawdown;
  }
}
