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
package org.ta4j.core.backtest.criteria.helpers;

import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.criteria.AnalysisCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Standard deviation criterion.
 *
 * <p>
 * Calculates the standard deviation for a Criterion.
 */
public class StandardDeviationCriterion implements AnalysisCriterion {

  /**
   * If true, then the lower the criterion value the better, otherwise the higher
   * the criterion value the better. This property is only used for
   * {@link #betterThan(Num, Num)}.
   */
  private final boolean lessIsBetter;

  private final VarianceCriterion varianceCriterion;


  /**
   * Constructor with {@link #lessIsBetter} = false.
   *
   * @param criterion the criterion from which the "standard deviation" is
   *     calculated
   */
  public StandardDeviationCriterion(final AnalysisCriterion criterion) {
    this.varianceCriterion = new VarianceCriterion(criterion);
    this.lessIsBetter = false;
  }


  /**
   * Constructor.
   *
   * @param criterion the criterion from which the "standard deviation" is
   *     calculated
   * @param lessIsBetter the {@link #lessIsBetter}
   */
  public StandardDeviationCriterion(final AnalysisCriterion criterion, final boolean lessIsBetter) {
    this.varianceCriterion = new VarianceCriterion(criterion);
    this.lessIsBetter = lessIsBetter;
  }


  @Override
  public Num calculate(final Position position) {
    return this.varianceCriterion.calculate(position).sqrt();
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.getPositions().isEmpty()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    return this.varianceCriterion.calculate(tradingRecord).sqrt();
  }


  /**
   * If {@link #lessIsBetter} == false, then the lower the criterion value, the
   * better, otherwise the higher the criterion value the better.
   */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return this.lessIsBetter ? criterionValue1.isLessThan(criterionValue2)
                             : criterionValue1.isGreaterThan(criterionValue2);
  }

}
