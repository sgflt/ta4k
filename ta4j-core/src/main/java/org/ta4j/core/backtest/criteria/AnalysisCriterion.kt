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
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * An analysis criterion. It can be used to:
 *
 *
 *  * analyze the performance of a [strategy][Strategy]
 *  * compare several [strategies][Strategy] together
 *
 */
interface AnalysisCriterion {
    /** Filter to differentiate between winning or losing positions.  */
    enum class PositionFilter {
        /** Consider only winning positions.  */
        PROFIT,

        /** Consider only losing positions.  */
        LOSS
    }

    /**
     * @param position the position, not null
     *
     * @return the criterion value for the position
     */
    fun calculate(position: Position): Num

    /**
     * @param tradingRecord the trading record, not null
     *
     * @return the criterion value for the positions
     */
    fun calculate(tradingRecord: TradingRecord): Num

    /**
     * @param backtestExecutor the bar series backtestExecutor with entry type of
     * BUY
     *
     * @return the best strategy (among the provided ones) according to the
     * criterion
     */
    //  default Strategy chooseBest(final BacktestExecutor backtestExecutor, final List<BacktestStrategy> strategies) {
    //    return chooseBest(backtestExecutor, TradeType.BUY, strategies);
    //  }
    /**
     * @param backtestExecutor the bar series backtestExecutor
     * @param tradeType the entry type (BUY or SELL) of the first trade in
     * the trading session
     *
     * @return the best strategy (among the provided ones) according to the
     * criterion
     */
    // TODO
    //  default Strategy chooseBest(
    //      final BacktestExecutor backtestExecutor, final TradeType tradeType,
    //      final List<BacktestStrategy> strategies
    //  ) {
    //
    //    final var tradingStatements = backtestExecutor.execute(strategies.getFirst(), tradeType);
    //    BacktestStrategy bestStrategy = strategies.getFirst();
    //    Num bestCriterionValue = calculate(backtestExecutor.getBarSeries(), bestStrategy.getTradeRecord());
    //
    //    for (final var tradingStatement : tradingStatements) {
    //      final var currentStrategy = tradingStatement.getStrategy();
    //      final var currentCriterionValue = calculate(backtestExecutor.getBarSeries(), currentStrategy.getTradeRecord());
    //
    //      if (betterThan(currentCriterionValue, bestCriterionValue)) {
    //        bestStrategy = currentStrategy;
    //        bestCriterionValue = currentCriterionValue;
    //      }
    //    }
    //
    //    return bestStrategy;
    //  }
    /**
     * @param criterionValue1 the first value
     * @param criterionValue2 the second value
     *
     * @return true if the first value is better than (according to the criterion)
     * the second one, false otherwise
     */
    // FIXME This method does not makes sence. It would be better to use OOP. new CriterionX(tradingRecord).isBetterThan(new CriterionZ(tradingRecord))
    // at second glance it simply compares uncomparable: PnL vs Return ???  it is different domain
    fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean
}
