package org.ta4j.core;

import static org.ta4j.core.TestUtils.GENERAL_OFFSET;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.ta4j.core.backtest.BacktestBarSeriesBuilder;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.bool.BooleanIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * @author Lukáš Kvídera
 */
public class TestContext {


  private Queue<MarketEvent> marketEvents;
  private final IndicatorContext indicatorContext = IndicatorContext.empty();
  private final BarSeries barSeries = new BacktestBarSeriesBuilder().build();


  public TestContext withMarketEvents(final List<MarketEvent> marketEvents) {
    this.marketEvents = new LinkedList<>(marketEvents);
    return this;
  }


  public TestContext withDefaultMarketEvents() {
    this.marketEvents = new LinkedList<>();

    for (double i = 0d; i < 5000; i++) {
      this.marketEvents.add(
          new CandleReceived(
              Duration.ofMinutes(1),
              ZonedDateTime.now().minusMinutes((long) (5001 - i)).toInstant(),
              i,
              i + 1,
              i + 2,
              i + 3,
              i + 4,
              i + 5
          )
      );
    }

    return this;
  }


  public TestContext withIndicator(final Indicator<?> indicator) {
    this.indicatorContext.add(indicator);
    return this;
  }


  public TestContext withIndicators(final Indicator<?>... indicator) {
    Stream.of(indicator).forEach(this::withIndicator);
    return this;
  }


  public boolean advance() {
    final var marketEvent = this.marketEvents.poll();
    if (marketEvent == null) {
      return false;
    }

    switch (marketEvent) {
      case final CandleReceived e -> this.barSeries.onCandle(e);
      default -> throw new IllegalStateException("Unexpected value: " + marketEvent);
    }

    return true;
  }


  public NumericIndicator getNumericIndicator(final int indicatorIndex) {
    return (NumericIndicator) this.indicatorContext.get(indicatorIndex);
  }


  public BooleanIndicator getBooleanIndicator(final int indicatorIndex) {
    return (BooleanIndicator) this.indicatorContext.get(indicatorIndex);
  }


  public void fastForward(final int bars) {
    TestUtils.fastForward(this, bars);
  }


  public TestContext assertNext(final double expected) {
    TestUtils.assertNext(this, expected);
    return this;
  }


  public void assertIndicatorEquals(final Indicator<Num> expectedIndicator, final Indicator<Num> indicator) {
    while (advance()) {
      if (Math.abs(expectedIndicator.getValue().doubleValue() - indicator.getValue().doubleValue()) > GENERAL_OFFSET) {
        return;
      }
    }
    throw new AssertionError("Indicators match to " + GENERAL_OFFSET);
  }
}
