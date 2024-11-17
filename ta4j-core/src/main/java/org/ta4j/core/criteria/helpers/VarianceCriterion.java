/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.criteria.helpers;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.AbstractAnalysisCriterion;
import org.ta4j.core.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Variance criterion.
 *
 * <p>
 * Calculates the variance for a Criterion.
 */
public class VarianceCriterion extends AbstractAnalysisCriterion {

  /**
   * If true, then the lower the criterion value the better, otherwise the higher
   * the criterion value the better. This property is only used for
   * {@link #betterThan(Num, Num)}.
   */
  private final boolean lessIsBetter;

  private final AnalysisCriterion criterion;
  private final NumberOfPositionsCriterion numberOfPositionsCriterion = new NumberOfPositionsCriterion();


  /**
   * Constructor with {@link #lessIsBetter} = false.
   *
   * @param criterion the criterion from which the "variance" is calculated
   */
  public VarianceCriterion(final AnalysisCriterion criterion) {
    this.criterion = criterion;
    this.lessIsBetter = false;
  }


  /**
   * Constructor.
   *
   * @param criterion the criterion from which the "variance" is calculated
   * @param lessIsBetter the {@link #lessIsBetter}
   */
  public VarianceCriterion(final AnalysisCriterion criterion, final boolean lessIsBetter) {
    this.criterion = criterion;
    this.lessIsBetter = lessIsBetter;
  }


  @Override
  public Num calculate(final Position position) {
    final Num criterionValue = this.criterion.calculate(position);
    final Num numberOfPositions = this.numberOfPositionsCriterion.calculate(position);

    Num variance = NumFactoryProvider.getDefaultNumFactory().zero();
    final Num average = criterionValue.dividedBy(numberOfPositions);
    final Num pow = this.criterion.calculate(position).minus(average).pow(2);
    variance = variance.plus(pow);
    variance = variance.dividedBy(numberOfPositions);
    return variance;
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.getPositions().isEmpty()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    final Num criterionValue = this.criterion.calculate(tradingRecord);
    final Num numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord);

    Num variance = NumFactoryProvider.getDefaultNumFactory().zero();
    final Num average = criterionValue.dividedBy(numberOfPositions);

    for (final Position position : tradingRecord.getPositions()) {
      final Num pow = this.criterion.calculate(position).minus(average).pow(2);
      variance = variance.plus(pow);
    }
    variance = variance.dividedBy(numberOfPositions);
    return variance;
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
