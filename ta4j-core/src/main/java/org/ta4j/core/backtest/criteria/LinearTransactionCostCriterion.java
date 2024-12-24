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

import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.Trade;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * A linear transaction cost criterion.
 *
 * <p>
 * Calculates the transaction cost according to an initial traded amount and a
 * linear function defined by a and b (a * x + b).
 */
public class LinearTransactionCostCriterion implements AnalysisCriterion {

  private final double initialAmount;

  private final double a;
  private final double b;

  private final ReturnCriterion grossReturn;


  /**
   * Constructor. (a * x)
   *
   * @param initialAmount the initially traded amount
   * @param a the a coefficient (e.g. 0.005 for 0.5% per {@link Trade
   *     trade})
   */
  public LinearTransactionCostCriterion(final double initialAmount, final double a) {
    this(initialAmount, a, 0);
  }


  /**
   * Constructor. (a * x + b)
   *
   * @param initialAmount the initially traded amount
   * @param a the a coefficient (e.g. 0.005 for 0.5% per {@link Trade
   *     trade})
   * @param b the b constant (e.g. 0.2 for $0.2 per {@link Trade
   *     trade})
   */
  public LinearTransactionCostCriterion(final double initialAmount, final double a, final double b) {
    this.initialAmount = initialAmount;
    this.a = a;
    this.b = b;
    this.grossReturn = new ReturnCriterion();
  }


  @Override
  public Num calculate(final Position position) {
    return getTradeCost(position, NumFactoryProvider.getDefaultNumFactory().numOf(this.initialAmount));
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    Num totalCosts = NumFactoryProvider.getDefaultNumFactory().zero();
    Num tradedAmount = NumFactoryProvider.getDefaultNumFactory().numOf(this.initialAmount);

    for (final var position : tradingRecord.getPositions()) {
      final Num tradeCost = getTradeCost(position, tradedAmount);
      totalCosts = totalCosts.plus(tradeCost);
      // To calculate the new traded amount:
      // - Remove the cost of the *first* trade
      // - Multiply by the profit ratio
      // - Remove the cost of the *second* trade
      tradedAmount = tradedAmount.minus(getTradeCost(position.getEntry(), tradedAmount));
      tradedAmount = tradedAmount.multipliedBy(this.grossReturn.calculate(position));
      tradedAmount = tradedAmount.minus(getTradeCost(position.getExit(), tradedAmount));
    }

    // Special case: if the current position is open
    final Position currentPosition = tradingRecord.getCurrentPosition();
    if (currentPosition.isOpened()) {
      totalCosts = totalCosts.plus(getTradeCost(currentPosition.getEntry(), tradedAmount));
    }

    return totalCosts;
  }


  /** The lower the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isLessThan(criterionValue2);
  }


  /**
   * @param trade the trade
   * @param tradedAmount the amount of the trade
   *
   * @return the absolute trade cost
   */
  private Num getTradeCost(final Trade trade, final Num tradedAmount) {
    final var numFactory = tradedAmount.getNumFactory();
    final Num tradeCost = numFactory.zero();
    if (trade != null) {
      return numFactory.numOf(this.a).multipliedBy(tradedAmount).plus(numFactory.numOf(this.b));
    }
    return tradeCost;
  }


  /**
   * @param position the position
   * @param initialAmount the initially traded amount for the position
   *
   * @return the absolute total cost of all trades in the position
   */
  private Num getTradeCost(final Position position, final Num initialAmount) {
    Num totalTradeCost = NumFactoryProvider.getDefaultNumFactory().zero();
    if (position != null && position.getEntry() != null) {
      totalTradeCost = getTradeCost(position.getEntry(), initialAmount);
      if (position.getExit() != null) {
        // To calculate the new traded amount:
        // - Remove the cost of the first trade
        // - Multiply by the profit ratio
        final Num newTradedAmount = initialAmount.minus(totalTradeCost)
            .multipliedBy(this.grossReturn.calculate(position));
        totalTradeCost = totalTradeCost.plus(getTradeCost(position.getExit(), newTradedAmount));
      }
    }
    return totalTradeCost;
  }
}
