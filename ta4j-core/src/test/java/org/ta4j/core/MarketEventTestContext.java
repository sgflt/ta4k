package org.ta4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ta4j.core.TestUtils.GENERAL_OFFSET;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.Getter;
import org.assertj.core.data.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.backtest.BacktestBarSeries;
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
 * This class simplifies testing by connecting market events and bar series through indicator context.
 *
 * Events are replayed to bar series that refreshes indicator context by created bar.
 */
public class MarketEventTestContext {
  private static final Logger log = LoggerFactory.getLogger(MarketEventTestContext.class);

  private Queue<MarketEvent> marketEvents;
  private final IndicatorContext indicatorContext = IndicatorContext.empty();
  @Getter
  private BacktestBarSeries barSeries =
      new BacktestBarSeriesBuilder().withIndicatorContext(this.indicatorContext).build();
  private Duration candleDuration = Duration.ofDays(1);
  private Instant startTime = Instant.EPOCH;


  /**
   * Sets OHLC to the same value.
   *
   * @param prices to set for each candle
   *
   * @return this
   */
  public MarketEventTestContext withCandlePrices(final double... prices) {
    this.marketEvents = new LinkedList<>(
        new MockMarketEventBuilder()
            .withStartTime(this.startTime)
            .withCandleDuration(this.candleDuration)
            .withCandlePrices(prices)
            .build()
    );
    return this;
  }


  public MarketEventTestContext withMarketEvents(final List<MarketEvent> marketEvents) {
    this.marketEvents = new LinkedList<>(marketEvents);
    return this;
  }


  public MarketEventTestContext withDefaultMarketEvents() {
    this.marketEvents = new LinkedList<>(new MockMarketEventBuilder().withDefaultData().build());
    return this;
  }


  public MarketEventTestContext withIndicator(final Indicator<?> indicator) {
    this.indicatorContext.add(indicator, UUID.randomUUID().toString());
    return this;
  }


  public MarketEventTestContext withIndicator(final Indicator<?> indicator, final String name) {
    this.indicatorContext.add(indicator, name);
    return this;
  }


  public MarketEventTestContext withIndicators(final Indicator<?>... indicator) {
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
  public MarketEventTestContext withNumFactory(final NumFactory factory) {
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


  public MarketEventTestContext fastForwardUntilStable() {
    while (this.indicatorContext.isNotEmpty() && !this.indicatorContext.isStable()) {
      fastForward(1);
    }

    return this;
  }


  public static void assertNextNaN(final MarketEventTestContext context, final NumericIndicator indicator) {
    context.advance();
    assertNumEquals(NaN.NaN, indicator.getValue());
  }


  public static void assertNextFalse(final MarketEventTestContext context) {
    context.advance();
    assertThat(context.getFisrtBooleanIndicator().getValue()).isFalse();
  }


  public static void assertNextTrue(final MarketEventTestContext context) {
    context.advance();
    assertThat(context.getFisrtBooleanIndicator().getValue()).isTrue();
  }


  /**
   * @param bars how many bars should be skipped.
   *
   * @return this
   */
  public MarketEventTestContext fastForward(final int bars) {
    log.debug("Fast forward =====> {}", bars);
    for (int i = 0; i < bars; i++) {
      if (!advance()) {
        throw new IllegalStateException("Fast forward failed at index " + i);
      }
    }
    return this;
  }


  public MarketEventTestContext assertCurrent(final double expected) {
    assertCurrent(getFirstNumericIndicator(), expected);
    return this;
  }


  private MarketEventTestContext assertCurrent(final NumericIndicator indicator, final double expected) {
    assertNumEquals(expected, indicator.getValue());
    return this;
  }


  private NumericIndicator getNumericIndicator(final String indicatorName) {
    return (NumericIndicator) this.indicatorContext.get(indicatorName);
  }


  public MarketEventTestContext assertNext(final double expected) {
    assertNext(getFirstNumericIndicator(), expected);
    return this;
  }


  private MarketEventTestContext assertNext(final NumericIndicator indicator, final double expected) {
    if (!advance()) {
      throw new IllegalStateException("Next failed");
    }

    assertNumEquals(expected, indicator.getValue());
    return this;
  }


  public MarketEventTestContext assertIndicatorEquals(
      final Indicator<Num> expectedIndicator,
      final Indicator<Num> indicator
  ) {
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


  /**
   * Adaptation of market events to executed position testing.
   *
   * There is some magic due to different clocks. This method adjusts clock to match with market events.
   *
   * Some criteria are not event based, but are retrospective instead. So for them, we have to replay all events
   * and calculate them on historical data.
   */
  public TradingRecordTestContext toTradingRecordContext() {
    return new TradingRecordTestContext(this);
  }


  public void withBarListener(final BarListener barListener) {
    this.barSeries.clearListeners();
    this.barSeries.addListener(barListener);
  }


  public MarketEventTestContext withCandleDuration(final ChronoUnit candleDuration) {
    this.candleDuration = candleDuration.getDuration();
    return this;
  }


  public MarketEventTestContext withStartTime(final Instant startTime) {
    this.startTime = startTime;
    return this;
  }


  public class IndicatorAsserts {
    private final String indicatorName;


    public IndicatorAsserts(final String indicatorName) {
      this.indicatorName = indicatorName;
      if (!MarketEventTestContext.this.indicatorContext.contains(indicatorName)) {
        throw new IllegalStateException("Indicator " + indicatorName + " not found");
      }
    }


    public IndicatorAsserts assertNext(final double expected) {
      MarketEventTestContext.this.assertNext(getNumericIndicator(this.indicatorName), expected);
      return this;
    }


    public IndicatorAsserts assertCurrent(final double expected) {
      MarketEventTestContext.this.assertCurrent(getNumericIndicator(this.indicatorName), expected);
      return this;
    }
  }
}
