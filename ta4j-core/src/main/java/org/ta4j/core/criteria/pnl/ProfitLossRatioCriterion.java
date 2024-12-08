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
package org.ta4j.core.criteria.pnl;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Ratio gross profit and loss criterion = Average gross profit (includes
 * trading costs) / Average gross loss (includes trading costs), returned in
 * decimal format.
 */
public class ProfitLossRatioCriterion implements AnalysisCriterion {

  private final AverageProfitCriterion averageProfitCriterion = new AverageProfitCriterion();
  private final AverageLossCriterion averageLossCriterion = new AverageLossCriterion();


  @Override
  public Num calculate(final Position position) {
    final Num averageProfit = this.averageProfitCriterion.calculate(position);
    if (averageProfit.isZero()) {
      // only loosing positions means a ratio of 0
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    final Num averageLoss = this.averageLossCriterion.calculate(position);
    if (averageLoss.isZero()) {
      // only winning positions means a ratio of 1
      return NumFactoryProvider.getDefaultNumFactory().one();
    }
    return averageProfit.dividedBy(averageLoss);
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    final Num averageProfit = this.averageProfitCriterion.calculate(tradingRecord);
    if (averageProfit.isZero()) {
      // only loosing positions means a ratio of 0
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    final Num averageLoss = this.averageLossCriterion.calculate(tradingRecord);
    if (averageLoss.isZero()) {
      // only winning positions means a ratio of 1
      return NumFactoryProvider.getDefaultNumFactory().one();
    }
    return averageProfit.dividedBy(averageLoss).abs();
  }


  /** The higher the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isGreaterThan(criterionValue2);
  }

}
