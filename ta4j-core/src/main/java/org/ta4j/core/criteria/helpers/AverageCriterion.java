/*
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
 * Average criterion.
 *
 * <p>
 * Calculates the average of a Criterion by dividing it by the number of
 * positions.
 */
public class AverageCriterion extends AbstractAnalysisCriterion {

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
   * @param criterion the criterion from which the "average" is calculated
   */
  public AverageCriterion(final AnalysisCriterion criterion) {
    this.criterion = criterion;
    this.lessIsBetter = false;
  }


  /**
   * Constructor.
   *
   * @param criterion the criterion from which the "average" is calculated
   * @param lessIsBetter the {@link #lessIsBetter}
   */
  public AverageCriterion(final AnalysisCriterion criterion, final boolean lessIsBetter) {
    this.criterion = criterion;
    this.lessIsBetter = lessIsBetter;
  }


  @Override
  public Num calculate(final Position position) {
    final Num numberOfPositions = this.numberOfPositionsCriterion.calculate(position);
    return this.criterion.calculate(position).dividedBy(numberOfPositions);
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.getPositions().isEmpty()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    final Num numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord);
    return this.criterion.calculate(tradingRecord).dividedBy(numberOfPositions);
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
