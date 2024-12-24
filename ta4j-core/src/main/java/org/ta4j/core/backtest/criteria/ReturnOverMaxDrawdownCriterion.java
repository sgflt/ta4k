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
package org.ta4j.core.backtest.criteria;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Return over maximum drawdown criterion (RoMaD).
 *
 * Measures strategy's risk-adjusted returns by dividing total return by maximum drawdown.
 * Higher values indicate better risk-adjusted performance.
 *
 * RoMaD = Total Return / Maximum Drawdown
 *
 * Interpretation:
 * - RoMaD > 1: Strategy generates more returns than its worst loss
 * - RoMaD < 1: Strategy's worst loss exceeds its returns
 * - RoMad = 0: No return
 * - RoMaD = NaN: no drawdown
 * - Negative RoMaD: Strategy lost money overall
 */

/**
 * Return over maximum drawdown criterion (RoMaD).
 */
@Slf4j
public final class ReturnOverMaxDrawdownCriterion implements AnalysisCriterion {

  private final ReturnCriterion returnCriterion = new ReturnCriterion(false);
  private final MaximumDrawdownCriterion maxDrawdownCriterion;
  private final NumFactory numFactory;


  public ReturnOverMaxDrawdownCriterion(final NumFactory numFactory) {
    this.numFactory = numFactory;
    this.maxDrawdownCriterion = new MaximumDrawdownCriterion(numFactory);
  }


  @Override
  public Num calculate(final Position position) {
    if (!position.isOpened()) {
      return this.numFactory.zero();
    }

    final var drawdown = this.maxDrawdownCriterion.calculate(position);
    final var totalReturn = this.returnCriterion.calculate(position);

    log.debug(
        "Position entry: {}, exit: {}",
        position.getEntry().getPricePerAsset(),
        position.getExit().getPricePerAsset()
    );
    log.debug("Is short: {}", position.getEntry().isSell());
    log.debug("Return: {}, Drawdown: {}", totalReturn, drawdown);

    final var result = totalReturn.dividedBy(drawdown);
    log.debug("RoMaD result: {}", result);
    return result;
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.isEmpty()) {
      return this.numFactory.zero();
    }

    final var drawdown = this.maxDrawdownCriterion.calculate(tradingRecord);
    final var totalReturn = this.returnCriterion.calculate(tradingRecord);

    log.debug("Return: {}, Drawdown: {}", totalReturn, drawdown);

    final var result = totalReturn.dividedBy(drawdown);
    log.debug("RoMaD result: {}", result);
    return result;
  }


  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isGreaterThan(criterionValue2);
  }
}
