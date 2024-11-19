package org.ta4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ta4j.core.TestUtils.GENERAL_OFFSET;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
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
    this.indicatorContext.add(indicator, UUID.randomUUID().toString());
    return this;
  }


  public TestContext withIndicator(final Indicator<?> indicator, final String name) {
    this.indicatorContext.add(indicator, name);
    return this;
  }


  public TestContext withIndicators(final Indicator<?>... indicator) {
    Stream.of(indicator).forEach(this::withIndicator);
    return this;
  }


  /**
   * This method should be called before creation of any indicator to preserve precision.
   *
   * @param factory that wil build {@link Num}
   *
   * @return this
   */
  public TestContext withNumFactory(final NumFactory factory) {
    this.barSeries =
        new BacktestBarSeriesBuilder()
            .withNumFactory(factory)
            .withIndicatorContext(this.indicatorContext)
            .build();
    return this;
  }


  public boolean advance() {
    log.trace("\t ### advance");

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


  public NumericIndicator getFirstNumericIndicator() {
    return (NumericIndicator) this.indicatorContext.getFirst();
  }


  public BooleanIndicator getFisrtBooleanIndicator() {
    return (BooleanIndicator) this.indicatorContext.getFirst();
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
    assertThat(context.getFisrtBooleanIndicator().getValue()).isFalse();
  }


  public static void assertNextTrue(final TestContext context) {
    context.advance();
    assertThat(context.getFisrtBooleanIndicator().getValue()).isTrue();
  }


  /**
   * @param bars how many bars should be skipped.
   *
   * @return this
   */
  public TestContext fastForward(final int bars) {
    log.debug("Fast forward =====> {}", bars);
    for (int i = 0; i < bars; i++) {
      if (!advance()) {
        throw new IllegalStateException("Fast forward failed at index " + i);
      }
    }
    return this;
  }


  public TestContext assertCurrent(final double expected) {
    assertCurrent(getFirstNumericIndicator(), expected);
    return this;
  }


  private TestContext assertCurrent(final NumericIndicator indicator, final double expected) {
    assertNumEquals(expected, indicator.getValue());
    return this;
  }


  private NumericIndicator getNumericIndicator(final String indicatorName) {
    return (NumericIndicator) this.indicatorContext.get(indicatorName);
  }


  public TestContext assertNext(final double expected) {
    assertNext(getFirstNumericIndicator(), expected);
    return this;
  }


  private TestContext assertNext(final NumericIndicator indicator, final double expected) {
    if (!advance()) {
      throw new IllegalStateException("Next failed");
    }

    assertNumEquals(expected, indicator.getValue());
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


  public IndicatorAsserts onIndicator(final String name) {
    return new IndicatorAsserts(name);
  }


  public class IndicatorAsserts {
    private final String indicatorName;


    public IndicatorAsserts(final String indicatorName) {
      this.indicatorName = indicatorName;
      if (!TestContext.this.indicatorContext.contains(indicatorName)) {
        throw new IllegalStateException("Indicator " + indicatorName + " not found");
      }
    }


    public IndicatorAsserts assertNext(final double expected) {
      TestContext.this.assertNext(getNumericIndicator(this.indicatorName), expected);
      return this;
    }


    public IndicatorAsserts assertCurrent(final double expected) {
      TestContext.this.assertCurrent(getNumericIndicator(this.indicatorName), expected);
      return this;
    }
  }
}
