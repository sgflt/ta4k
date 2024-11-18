package org.ta4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ta4j.core.TestUtils.GENERAL_OFFSET;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.assertj.core.data.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.backtest.BacktestBarSeriesBuilder;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.indicators.IndicatorContext;
import org.ta4j.core.indicators.bool.BooleanIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.mocks.MockMarketEventBuilder;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * @author Lukáš Kvídera
 */
public class TestContext {
  private static final Logger log = LoggerFactory.getLogger(TestContext.class);

  private Queue<MarketEvent> marketEvents;
  private final IndicatorContext indicatorContext = IndicatorContext.empty();
  private BarSeries barSeries = new BacktestBarSeriesBuilder().withIndicatorContext(this.indicatorContext).build();


  /**
   * Sets OHLC to the same value.
   *
   * @param prices to set for each candle
   *
   * @return this
   */
  public TestContext withCandlePrices(final double... prices) {
    this.marketEvents = new LinkedList<>(
        new MockMarketEventBuilder()
            .withCandlePrices(prices)
            .build()
    );
    return this;
  }


  public TestContext withMarketEvents(final List<MarketEvent> marketEvents) {
    this.marketEvents = new LinkedList<>(marketEvents);
    return this;
  }


  public TestContext withDefaultMarketEvents() {
    this.marketEvents = new LinkedList<>(new MockMarketEventBuilder().withDefaultData().build());
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


  public TestContext withNumFactory(final NumFactory factory) {
    this.barSeries =
        new BacktestBarSeriesBuilder()
            .withNumFactory(factory)
            .withIndicatorContext(this.indicatorContext)
            .build();
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


  public TestContext fastForwardUntilStable() {
    while (this.indicatorContext.isNotEmpty() && !this.indicatorContext.isStable()) {
      fastForward(1);
    }

    return this;
  }


  public static void assertNextNaN(final TestContext context, final NumericIndicator indicator) {
    context.advance();
    assertNumEquals(NaN.NaN, indicator.getValue());
  }


  public static void assertNextFalse(final TestContext context) {
    context.advance();
    assertThat(context.getBooleanIndicator(0).getValue()).isFalse();
  }


  public static void assertNextTrue(final TestContext context) {
    context.advance();
    assertThat(context.getBooleanIndicator(0).getValue()).isTrue();
  }


  /**
   * @param bars how many bars should be skipped.
   *
   * @return this
   */
  public TestContext fastForward(final int bars) {
    log.debug("Fast forward =====> {}", bars);
    for (int i = 0; i < bars; i++) {
      log.trace("\t =====> {}", i);
      if (!advance()) {
        throw new IllegalStateException("Fast forward failed at index " + i);
      }
    }
    return this;
  }


  public TestContext assertCurrent(final double expected) {
    assertNumEquals(expected, getNumericIndicator(0).getValue());
    return this;
  }


  public TestContext assertNext(final double expected) {
    if (!advance()) {
      throw new IllegalStateException("Next failed");
    }

    assertNumEquals(expected, getNumericIndicator(0).getValue());
    return this;
  }


  public TestContext assertIndicatorEquals(final Indicator<Num> expectedIndicator, final Indicator<Num> indicator) {
    while (advance()) {
      assertThat(indicator.getValue().doubleValue())
          .isCloseTo(
              expectedIndicator.getValue().doubleValue(),
              Offset.offset(GENERAL_OFFSET)
          );
    }

    return this;
  }
}
