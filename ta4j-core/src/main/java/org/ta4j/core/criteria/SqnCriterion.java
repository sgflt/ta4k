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
package org.ta4j.core.criteria;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.helpers.StandardDeviationCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * The SQN ("System Quality Number") Criterion.
 *
 * @see <a href=
 *     "https://indextrader.com.au/van-tharps-sqn/">https://indextrader.com.au/van-tharps-sqn/</a>
 */
public class SqnCriterion implements AnalysisCriterion {

  private final AnalysisCriterion criterion;
  private final StandardDeviationCriterion standardDeviationCriterion;
  private final NumberOfPositionsCriterion numberOfPositionsCriterion = new NumberOfPositionsCriterion();

  /**
   * The number to be used for the part of {@code √(numberOfPositions)} within the
   * SQN-Formula when there are more than 100 trades. If this value is
   * {@code null}, then the number of positions calculated by
   * {@link #numberOfPositionsCriterion} is used instead.
   */
  private final Integer nPositions;


  /**
   * Constructor with {@code criterion} = {@link ProfitLossCriterion}.
   */
  public SqnCriterion() {
    this(new ProfitLossCriterion());
  }


  /**
   * Constructor.
   *
   * @param criterion the Criterion (e.g. ProfitLossCriterion or
   *     ExpectancyCriterion)
   */
  public SqnCriterion(final AnalysisCriterion criterion) {
    this(criterion, null);
  }


  /**
   * Constructor.
   *
   * @param criterion the Criterion (e.g. ProfitLossCriterion or
   *     ExpectancyCriterion)
   * @param nPositions the {@link #nPositions} (optional)
   */
  public SqnCriterion(final AnalysisCriterion criterion, final Integer nPositions) {
    this.criterion = criterion;
    this.nPositions = nPositions;
    this.standardDeviationCriterion = new StandardDeviationCriterion(criterion);
  }


  @Override
  public Num calculate(final Position position) {
    final Num stdDevPnl = this.standardDeviationCriterion.calculate(position);
    if (stdDevPnl.isZero()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
    final Num numberOfPositions = this.numberOfPositionsCriterion.calculate(position);
    final Num pnl = this.criterion.calculate(position);
    final Num avgPnl = pnl.dividedBy(numberOfPositions);
    return avgPnl.dividedBy(stdDevPnl).multipliedBy(numberOfPositions.sqrt());
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.getPositions().isEmpty()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }
    final Num stdDevPnl = this.standardDeviationCriterion.calculate(tradingRecord);
    if (stdDevPnl.isZero()) {
      return NumFactoryProvider.getDefaultNumFactory().zero();
    }

    Num numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord);
    final Num pnl = this.criterion.calculate(tradingRecord);
    final Num avgPnl = pnl.dividedBy(numberOfPositions);
    if (this.nPositions != null && numberOfPositions.isGreaterThan(NumFactoryProvider.getDefaultNumFactory()
        .hundred())) {
      numberOfPositions = NumFactoryProvider.getDefaultNumFactory().numOf(this.nPositions);
    }
    // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
    return avgPnl.dividedBy(stdDevPnl).multipliedBy(numberOfPositions.sqrt());
  }


  /** The higher the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isGreaterThan(criterionValue2);
  }

}
