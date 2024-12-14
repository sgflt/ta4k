package org.ta4j.core;

import org.ta4j.core.backtest.RuntimeContext;
import org.ta4j.core.indicators.IndicatorContext;

public interface StrategyFactory<T extends Strategy> {

  /**
   * For what direction is produced Strategy designed
   *
   * @return BUY or SELL
   */
  Trade.TradeType getTradeType();

  /**
   * @param runtimeContext that provides additional data to strategy
   * @param indicatorContext that performs indicator recalculation on each bar
   *
   * @return strategy that is source for trading signals
   */
  T createStrategy(RuntimeContext runtimeContext, IndicatorContext indicatorContext);
}
