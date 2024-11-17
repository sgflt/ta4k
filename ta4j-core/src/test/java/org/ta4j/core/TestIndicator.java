package org.ta4j.core;

import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.indicators.Indicator;

/**
 * @author Lukáš Kvídera
 */
public class TestIndicator<T> implements Indicator<T> {
  private final BacktestBarSeries series;
  private final Indicator<T> indicator;


  public TestIndicator(final BacktestBarSeries series, final Indicator<T> indicator) {
    this.series = series;
    this.indicator = indicator;
  }


  @Override
  public T getValue() {
    return this.indicator.getValue();
  }


  @Override
  public void onBar(final Bar bar) {
    this.indicator.onBar(bar);
  }


  @Override
  public boolean isStable() {
    return this.indicator.isStable();
  }


  public BacktestBarSeries getBarSeries() {
    return this.series;
  }
}
