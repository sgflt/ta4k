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

package org.ta4j.core.backtest;

import org.ta4j.core.RuntimeContext;
import org.ta4j.core.Strategy;
import org.ta4j.core.StrategyFactory;
import org.ta4j.core.Trade;
import org.ta4j.core.indicators.IndicatorContext;

/**
 * Converts standard StrategyFactory<Strategy> to StrategyFactory<BacktestStrategy>
 * for use in backtesting.
 */
public final class StrategyFactoryConverter {

  private StrategyFactoryConverter() {
    // Utility class
  }


  /**
   * Converts a standard strategy factory to a backtest strategy factory.
   *
   * @param originalFactory the original strategy factory to convert
   *
   * @return a new factory that creates BacktestStrategy instances
   */
  public static StrategyFactory<BacktestStrategy> convert(final StrategyFactory<Strategy> originalFactory) {
    return new BacktestStrategyFactory(originalFactory);
  }


  private static final class BacktestStrategyFactory implements StrategyFactory<BacktestStrategy> {
    private final StrategyFactory<Strategy> originalFactory;


    private BacktestStrategyFactory(final StrategyFactory<Strategy> originalFactory) {
      this.originalFactory = originalFactory;
    }


    @Override
    public Trade.TradeType getTradeType() {
      return this.originalFactory.getTradeType();
    }


    /**
     * @param runtimeContext that provides additional data to strategy,
     * @param indicatorContext that performs indicator recalculation on each bar
     *
     * @return strategy for backtesting
     */
    @Override
    public BacktestStrategy createStrategy(
        final RuntimeContext runtimeContext,
        final IndicatorContext indicatorContext
    ) {
      final var originalStrategy = this.originalFactory.createStrategy(
          runtimeContext,
          indicatorContext
      );

      return new BacktestStrategy(
          originalStrategy,
          (BackTestTradingRecord) runtimeContext
      );
    }
  }
}
