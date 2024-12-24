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
package org.ta4j.core.backtest.criteria.pnl;

import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.criteria.AnalysisCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Net profit and loss in percentage criterion (relative PnL, excludes trading
 * costs), returned in percentage format (e.g. 1 = 1%).
 *
 * <p>
 * Defined as the position profit over the purchase price. The profit or loss in
 * percentage over the provided {@link Position position(s)}.
 * https://www.investopedia.com/ask/answers/how-do-you-calculate-percentage-gain-or-loss-investment/
 */
public class ProfitLossPercentageCriterion implements AnalysisCriterion {

  @Override
  public Num calculate(final Position position) {
    if (position.isClosed()) {
      final Num entryPrice = position.getEntry().getValue();
      return position.getProfit()
          .dividedBy(entryPrice)
          .multipliedBy(NumFactoryProvider.getDefaultNumFactory().hundred());
    }
    return NumFactoryProvider.getDefaultNumFactory().zero();
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    return tradingRecord.getPositions()
        .stream()
        .filter(Position::isClosed)
        .map(this::calculate)
        .reduce(NumFactoryProvider.getDefaultNumFactory().zero(), Num::plus);
  }


  /** The higher the criterion value, the better. */
  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    return criterionValue1.isGreaterThan(criterionValue2);
  }

}
