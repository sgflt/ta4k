package org.ta4j.core.analysis;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.backtest.BacktestBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DecimalNumFactory;

class ReturnsTest {

  private BacktestBarSeries series;
  private Position position;
  private Instant startTime;


  @BeforeEach
  void setUp() {
    this.series = new BacktestBarSeriesBuilder()
        .withNumFactory(DecimalNumFactory.getInstance())
        .build();

    this.startTime = Instant.parse("2024-01-01T10:00:00Z");

    // Add 10 bars with prices to allow for gaps
    addBar(100, 0);  // 10:00
    addBar(110, 1);  // 10:01
    addBar(105, 2);  // 10:02
    addBar(115, 3);  // 10:03
    addBar(120, 4);  // 10:04
    addBar(118, 5);  // 10:05
    addBar(125, 6);  // 10:06
    addBar(122, 7);  // 10:07
    addBar(130, 8);  // 10:08
    addBar(135, 9);  // 10:09
  }


  private void addBar(final double price, final int minutesOffset) {
    final var time = this.startTime.plus(minutesOffset, ChronoUnit.MINUTES);
    this.series.barBuilder()
        .timePeriod(Duration.of(1, ChronoUnit.MINUTES))
        .endTime(time)
        .openPrice(price).highPrice(price).lowPrice(price).closePrice(price)
        .volume(1000)
        .add();
  }


  @Nested
  @DisplayName("ARITHMETIC Returns Tests")
  class ArithmeticReturnsTest {

    @Test
    @DisplayName("Should calculate correct arithmetic returns for long position")
    void shouldCalculateArithmeticReturnsForLongPosition() {
      // Given
      ReturnsTest.this.position = createPosition(Trade.TradeType.BUY, 100, 120, 0, 4);

      // When
      final var returns =
          new Returns(ReturnsTest.this.series, ReturnsTest.this.position, Returns.ReturnType.ARITHMETIC);

      // Then
      // (110-100)/100 = 0.10
      assertNumEquals(0.10, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));

      // (105-110)/110 = -0.0455
      assertNumEquals(-0.0455, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));

      // (115-105)/105 = 0.0952
      assertNumEquals(0.0952, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));

      // (120-115)/115 = 0.0435
      assertNumEquals(0.0435, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }


    @Test
    @DisplayName("Should calculate correct arithmetic returns for short position")
    void shouldCalculateArithmeticReturnsForShortPosition() {
      // Given
      ReturnsTest.this.position = createPosition(Trade.TradeType.SELL, 120, 100, 0, 4);

      // When
      final var returns =
          new Returns(ReturnsTest.this.series, ReturnsTest.this.position, Returns.ReturnType.ARITHMETIC);

      // Then - for short positions, returns are negated
      // -((110-100)/100) = -0.10
      assertNumEquals(-0.10, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));

      // -((105-110)/110) = 0.0455
      assertNumEquals(0.0455, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));

      // -((115-105)/105) = -0.0952
      assertNumEquals(-0.0952, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));

      // -((120-115)/115) = -0.0435
      assertNumEquals(-0.0435, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }
  }

  @Nested
  @DisplayName("LOG Returns Tests")
  class LogReturnsTest {

    @Test
    @DisplayName("Should calculate correct log returns for long position")
    void shouldCalculateLogReturnsForLongPosition() {
      // Given
      ReturnsTest.this.position = createPosition(Trade.TradeType.BUY, 100, 120, 0, 4);

      // When
      final var returns = new Returns(ReturnsTest.this.series, ReturnsTest.this.position, Returns.ReturnType.LOG);

      // Then
      // ln(110/100) = 0.0953
      assertNumEquals(0.0953, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));

      // ln(105/110) = -0.0465
      assertNumEquals(-0.0465, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));

      // ln(115/105) = 0.0909
      assertNumEquals(0.0909, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));

      // ln(120/115) = 0.0426
      assertNumEquals(0.0426, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }


    @Test
    @DisplayName("Should calculate correct log returns for short position")
    void shouldCalculateLogReturnsForShortPosition() {
      // Given
      ReturnsTest.this.position = createPosition(Trade.TradeType.SELL, 120, 100, 0, 4);

      // When
      final var returns = new Returns(ReturnsTest.this.series, ReturnsTest.this.position, Returns.ReturnType.LOG);

      // Then - for short positions, returns are negated
      // -ln(110/100) = -0.0953
      assertNumEquals(-0.0953, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));

      // -ln(105/110) = 0.0465
      assertNumEquals(0.0465, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));

      // -ln(115/105) = -0.0909
      assertNumEquals(-0.0909, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));

      // -ln(120/115) = -0.0426
      assertNumEquals(-0.0426, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }
  }

  @Nested
  @DisplayName("Multiple Positions Tests")
  class MultiplePositionsTest {

    @Test
    @DisplayName("Should handle gaps between trades correctly")
    void shouldHandleGapsBetweenTrades() {
      // Given
      final var tradingRecord = new BackTestTradingRecord();

      // First position: 10:00 - 10:02
      tradingRecord.operate(
          ReturnsTest.this.startTime,                                    // 10:00
          DecimalNum.valueOf(100),
          DecimalNum.valueOf(1)
      );
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES),       // 10:02
          DecimalNum.valueOf(105),
          DecimalNum.valueOf(1)
      );

      // Gap between 10:02 and 10:05

      // Second position: 10:05 - 10:07
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES),       // 10:05
          DecimalNum.valueOf(118),
          DecimalNum.valueOf(1)
      );
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(7, ChronoUnit.MINUTES),       // 10:07
          DecimalNum.valueOf(122),
          DecimalNum.valueOf(1)
      );

      // When
      final var returns = new Returns(ReturnsTest.this.series, tradingRecord, Returns.ReturnType.ARITHMETIC);

      // Then
      // First position returns
      assertNumEquals(0.10, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));  // (110-100)/100
      assertNumEquals(
          -0.0455,
          returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES))
      ); // (105-110)/110

      // Gap period - should return zero
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));

      // Second position returns
      assertNumEquals(
          0.0593,
          returns.getValue(ReturnsTest.this.startTime.plus(6, ChronoUnit.MINUTES))
      ); // (125-118)/118
      assertNumEquals(
          -0.024,
          returns.getValue(ReturnsTest.this.startTime.plus(7, ChronoUnit.MINUTES))
      ); // (122-125)/125

      // After last trade - should return zero
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(8, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(9, ChronoUnit.MINUTES)));
    }


    @Test
    @DisplayName("Should return zero for times before first trade")
    void shouldReturnZeroBeforeFirstTrade() {
      // Given
      final var tradingRecord = new BackTestTradingRecord();

      // Position starting at 10:05
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES),
          DecimalNum.valueOf(118),
          DecimalNum.valueOf(1)
      );
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(7, ChronoUnit.MINUTES),
          DecimalNum.valueOf(122),
          DecimalNum.valueOf(1)
      );

      // When
      final var returns = new Returns(ReturnsTest.this.series, tradingRecord, Returns.ReturnType.ARITHMETIC);

      // Then
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES)));
      assertNumEquals(0.0, returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES)));
    }


    @Test
    @Disabled("Ta4j currently does not support pyramid trading")
    @DisplayName("Should handle overlapping trade times correctly")
    void shouldHandleOverlappingTrades() {
      // Given
      final var tradingRecord = new BackTestTradingRecord();

      // First position: 10:00 - 10:04
      tradingRecord.operate(
          ReturnsTest.this.startTime,
          DecimalNum.valueOf(100),
          DecimalNum.valueOf(1)
      );
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES),
          DecimalNum.valueOf(120),
          DecimalNum.valueOf(1)
      );

      // Second position: 10:02 - 10:06 (overlaps with first)
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES),
          DecimalNum.valueOf(105),
          DecimalNum.valueOf(1)
      );
      tradingRecord.operate(
          ReturnsTest.this.startTime.plus(6, ChronoUnit.MINUTES),
          DecimalNum.valueOf(125),
          DecimalNum.valueOf(1)
      );

      // When
      final var returns = new Returns(ReturnsTest.this.series, tradingRecord, Returns.ReturnType.ARITHMETIC);

      // Then
      // Should combine returns from both positions
      assertNumEquals(
          0.10,
          returns.getValue(ReturnsTest.this.startTime.plus(1, ChronoUnit.MINUTES))
      );  // First position
      assertNumEquals(
          -0.0455,
          returns.getValue(ReturnsTest.this.startTime.plus(2, ChronoUnit.MINUTES))
      ); // Both positions
      assertNumEquals(
          0.0952,
          returns.getValue(ReturnsTest.this.startTime.plus(3, ChronoUnit.MINUTES))
      ); // Both positions
      assertNumEquals(
          0.0435,
          returns.getValue(ReturnsTest.this.startTime.plus(4, ChronoUnit.MINUTES))
      ); // Both positions
      assertNumEquals(
          -0.0167,
          returns.getValue(ReturnsTest.this.startTime.plus(5, ChronoUnit.MINUTES))
      ); // Second position
      assertNumEquals(
          0.0593,
          returns.getValue(ReturnsTest.this.startTime.plus(6, ChronoUnit.MINUTES))
      ); // Second position
    }
  }


  private Position createPosition(
      final Trade.TradeType type, final double entryPrice, final double exitPrice,
      final int entryIndex, final int exitIndex
  ) {
    final var position = new Position(type);

    position.operate(
        this.startTime.plus(entryIndex, ChronoUnit.MINUTES),
        DecimalNum.valueOf(entryPrice),
        DecimalNum.valueOf(1)
    );

    position.operate(
        this.startTime.plus(exitIndex, ChronoUnit.MINUTES),
        DecimalNum.valueOf(exitPrice),
        DecimalNum.valueOf(1)
    );

    return position;
  }
}
