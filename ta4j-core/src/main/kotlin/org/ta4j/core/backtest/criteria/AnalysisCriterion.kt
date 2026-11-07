/*
 * SPDX-License-Identifier: MIT
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
}
