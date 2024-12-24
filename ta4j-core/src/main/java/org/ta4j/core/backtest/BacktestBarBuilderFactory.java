package org.ta4j.core.backtest;

import org.ta4j.core.api.series.BarBuilderFactory;
import org.ta4j.core.api.series.BarSeries;

/**
 * @author Lukáš Kvídera
 */
class BacktestBarBuilderFactory implements BarBuilderFactory {


  private BacktestBarBuilder backtestBarBuilder;


  @Override
  public BacktestBarBuilder createBarBuilder(final BarSeries series) {
    if (this.backtestBarBuilder == null) {
      this.backtestBarBuilder = new BacktestBarBuilder(series);
    }

    return this.backtestBarBuilder;
  }
}
