package org.ta4j.core.analysis;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecordTestContext;
import org.ta4j.core.num.NumFactory;

class ReturnsTest {

  private MarketEventTestContext context;
  private TradingRecordTestContext tradingContext;
  private Instant startTime;


  @BeforeEach
  void setUp() {
    this.startTime = Instant.parse("2024-01-01T10:00:00Z");
    this.context = new MarketEventTestContext()
        .withCandleDuration(ChronoUnit.MINUTES)
        .withStartTime(this.startTime)
        .withCandlePrices(100, 110, 105, 115, 120, 118, 125, 122, 130, 135);

    this.tradingContext = this.context.toTradingRecordContext();
  }


  @Nested
  @DisplayName("ARITHMETIC Returns Tests")
  class ArithmeticReturnsTest {

    @Test
    @DisplayName("Should calculate correct arithmetic returns for long position")
    void shouldCalculateArithmeticReturnsForLongPosition() {
      // Given
      ReturnsTest.this.tradingContext
          .enter(1).asap()
          .exit(1).after(4);

      // When
      final var returns = new Returns(
          ReturnsTest.this.context.getBarSeries().numFactory(),
          ReturnsTest.this.tradingContext.getTradingRecord(), Returns.ReturnType.ARITHMETIC
      );

      // Then
      assertNumEquals(0.00, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(0.10, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0455, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(0.0952, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
      assertNumEquals(0.0435, returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES)));
    }


    @Test
    @DisplayName("Should calculate correct arithmetic returns for short position")
    void shouldCalculateArithmeticReturnsForShortPosition() {
      // Given
      ReturnsTest.this.tradingContext
          .withTradeType(Trade.TradeType.SELL)
          .enter(1).asap()
          .exit(1).after(4);

      // When
      final var returns = new Returns(
          ReturnsTest.this.context.getBarSeries().numFactory(),
          ReturnsTest.this.tradingContext.getTradingRecord(), Returns.ReturnType.ARITHMETIC
      );

      // Then
      assertNumEquals(0.00, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(-0.10, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(0.0455, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0952, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0435, returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES)));
    }
  }

  @Nested
  @DisplayName("LOG Returns Tests")
  class LogReturnsTest {

    @Test
    @DisplayName("Should calculate correct log returns for long position")
    void shouldCalculateLogReturnsForLongPosition() {
      // Given
      ReturnsTest.this.tradingContext
          .enter(1).asap()
          .exit(1).after(4);

      // When
      final var returns = new Returns(
          ReturnsTest.this.context.getBarSeries().numFactory(),
          ReturnsTest.this.tradingContext.getTradingRecord(), Returns.ReturnType.LOG
      );

      // Then
      assertNumEquals(0.00, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(0.0953, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0465, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(0.0909, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
      assertNumEquals(0.0426, returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES)));
    }


    @Test
    @DisplayName("Should calculate correct log returns for short position")
    void shouldCalculateLogReturnsForShortPosition() {
      // Given
      ReturnsTest.this.tradingContext
          .withTradeType(Trade.TradeType.SELL)
          .enter(1).asap()
          .exit(1).after(4);

      // When
      final var returns = new Returns(
          ReturnsTest.this.context.getBarSeries().numFactory(),
          ReturnsTest.this.tradingContext.getTradingRecord(), Returns.ReturnType.LOG
      );

      // Then
      assertNumEquals(0.00, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0953, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(0.0465, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0909, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0426, returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES)));
    }
  }

  @Nested
  @DisplayName("Multiple Positions Tests")
  class MultiplePositionsTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle gaps between trades correctly")
    void shouldHandleGapsBetweenTrades(final NumFactory numFactory) {
      // Given
      final var testContext = new MarketEventTestContext()
          .withCandleDuration(ChronoUnit.MINUTES)
          .withNumFactory(numFactory)
          .withStartTime(ReturnsTest.this.startTime)
          .withCandlePrices(100, 110, 105, 115, 120, 118, 125, 122, 130, 135)
          .withNumFactory(numFactory)
          .toTradingRecordContext()
          // First position
          .enter(1).asap()
          .exit(1).after(2)
          // gap 3 bars without opened position
          .enter(1).after(3)
          .exit(1).after(2);

      // When
      final var returns = new Returns(
          testContext.getBarSeries().numFactory(),
          testContext.getTradingRecord(), Returns.ReturnType.ARITHMETIC
      );

      // Then
      // First position returns
      assertNumEquals(0.10, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(-0.0455, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));

      // Gap period
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(6, ChronoUnit.MINUTES)));

      // Second position returns
      assertNumEquals(0.0593, returns.getValue(ReturnsTest.this.startTime.plus(7, ChronoUnit.MINUTES)));
      assertNumEquals(-0.024, returns.getValue(ReturnsTest.this.startTime.plus(8, ChronoUnit.MINUTES)));

      // After last trade
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(9, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(10, ChronoUnit.MINUTES)));
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should return zero for times before first trade")
    void shouldReturnZeroBeforeFirstTrade(final NumFactory numFactory) {
      // Given
      ReturnsTest.this.tradingContext
          .withNumFactory(numFactory)
          .enter(1).at(118)
          .exit(1).at(122);

      // When
      final var returns = new Returns(
          ReturnsTest.this.context.getBarSeries().numFactory(),
          ReturnsTest.this.tradingContext.getTradingRecord(), Returns.ReturnType.ARITHMETIC
      );

      // Then
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }
  }
}
