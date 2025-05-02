package org.ta4j.core;

import org.jetbrains.annotations.NotNull;
import org.ta4j.core.api.Indicator;
import org.ta4j.core.api.series.Bar;
import org.ta4j.core.backtest.BacktestBarSeries;

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
  public void onBar(@NotNull final Bar bar) {
    this.indicator.onBar(bar);
  }


  @Override
  public boolean isStable() {
    return this.indicator.isStable();
  }


  public BacktestBarSeries getBarSeries() {
    return this.series;
  }


  @Override
  public int getLag() {
    return 0;
  }
}
