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
package org.ta4j.core.backtest.analysis;

import java.time.Instant;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.Trade;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Tracks the money cash flow involved by a list of positions over time.
 *
 * buy at 100
 * price drops to 50
 * sell at 150
 *
 * flow is net +50
 *
 * This implementation does not track price fluctuations between entry and exit.
 */
public class RealizedCashFlow {

  /** The cash flow values mapped to timestamps */
  private final NavigableMap<Instant, Num> values;

  /** The num factory for creating numbers */
  private final NumFactory numFactory;


  /**
   * Constructor for cash flows of a closed position.
   *
   * @param numFactory the number factory to use
   * @param position a single position
   */
  public RealizedCashFlow(final NumFactory numFactory, final Position position) {
    this.numFactory = numFactory;
    this.values = new TreeMap<>();

    // Initialize with base value 1
    if (position.getEntry() != null) {
      this.values.put(position.getEntry().getWhenExecuted(), numFactory.one());
    }

    calculate(position);
  }


  /**
   * Constructor for cash flows of closed positions of a trading record.
   *
   * @param numFactory the number factory to use
   * @param tradingRecord the trading record
   */
  public RealizedCashFlow(final NumFactory numFactory, final TradingRecord tradingRecord) {
    this.numFactory = numFactory;
    this.values = new TreeMap<>();

    // Initialize with base value 1 at first trade
    if (!tradingRecord.getPositions().isEmpty()) {
      final Trade firstTrade = tradingRecord.getPositions().getFirst().getEntry();
      if (firstTrade != null) {
        this.values.put(firstTrade.getWhenExecuted(), numFactory.one());
      }
    }

    calculate(tradingRecord);
  }


  /**
   * @return all cash flow values with their timestamps
   */
  public NavigableMap<Instant, Num> getValues() {
    return new TreeMap<>(this.values);
  }


  /**
   * Calculates the cash flow for a single position.
   *
   * @param position a single position
   */
  private void calculate(final Position position) {
    if (!position.isOpened()) {
      calculateClosedPosition(position);
    } else {
      calculateOpenPosition(position, position.getEntry().getWhenExecuted());
    }
  }


  /**
   * Gets the cash flow value at a specific instant. If no value exists at that instant,
   * calculates the interpolated value based on the position state at that time.
   *
   * @param instant the point in time
   *
   * @return the cash flow value
   */
  public Num getValue(final Instant instant) {
    // If we have a value at this exact instant, return it
    if (this.values.containsKey(instant)) {
      return this.values.get(instant);
    }

    // Get the last entry before this instant
    final var lastEntry = this.values.floorEntry(instant);
    if (lastEntry == null) {
      return this.numFactory.one();
    }

    // Get the next entry after this instant
    final var nextEntry = this.values.ceilingEntry(instant);
    if (nextEntry == null) {
      return lastEntry.getValue();
    }

    // Linear interpolation between the two points
    final var lastTime = lastEntry.getKey();
    final var nextTime = nextEntry.getKey();
    final var timeRange = nextTime.getEpochSecond() - lastTime.getEpochSecond();
    final var currentTimeOffset = instant.getEpochSecond() - lastTime.getEpochSecond();

    // Calculate progress between the two points (0 to 1)
    final var progress = this.numFactory.numOf((double) currentTimeOffset / timeRange);

    // Interpolate between the two values
    final var valueRange = nextEntry.getValue().minus(lastEntry.getValue());
    return lastEntry.getValue().plus(valueRange.multipliedBy(progress));
  }


  private void calculateClosedPosition(final Position position) {
    final boolean isLongTrade = position.getEntry().isBuy();
    final Trade entry = position.getEntry();
    final Trade exit = position.getExit();

    // Calculate ratio
    final Num ratio = calculateRatio(isLongTrade, entry.getNetPrice(), exit.getNetPrice());

    // Get the value at entry and multiply by ratio for exit value
    final Num entryValue = getValue(entry.getWhenExecuted());
    this.values.put(exit.getWhenExecuted(), entryValue.multipliedBy(ratio));
  }


  private void calculateOpenPosition(final Position position, final Instant evaluationTime) {
    final boolean isLongTrade = position.getEntry().isBuy();
    final Trade entry = position.getEntry();
    final Instant entryTime = entry.getWhenExecuted();

    // Calculate holding costs
    final Num holdingCost = position.getHoldingCost();
    final Num entryPrice = entry.getNetPrice();

    // Record initial value
    final Num entryValue = getValue(entryTime);
    this.values.put(entryTime, entryValue);

    // Record value at evaluation time with holding costs
    final Num currentPrice = entry.getPricePerAsset();
    final Num adjustedPrice = addCost(currentPrice, holdingCost, isLongTrade);
    final Num ratio = calculateRatio(isLongTrade, entryPrice, adjustedPrice);
    this.values.put(evaluationTime, entryValue.multipliedBy(ratio));
  }


  private void calculate(final TradingRecord tradingRecord) {
    // Calculate for all closed positions
    tradingRecord.getPositions().forEach(this::calculate);

    // Calculate for current open position if any
    final var currentPosition = tradingRecord.getCurrentPosition();
    if (currentPosition.isOpened()) {
      // Use the latest timestamp from the trading record
      final var latestTime = tradingRecord.getPositions().stream()
          .filter(Position::isClosed)
          .map(p -> p.getExit().getWhenExecuted())
          .max(Instant::compareTo)
          .orElse(currentPosition.getEntry().getWhenExecuted());

      calculateOpenPosition(currentPosition, latestTime);
    }
  }


  /**
   * Calculates the ratio between entry and exit prices accounting for trade direction.
   */
  private Num calculateRatio(final boolean isLongTrade, final Num entryPrice, final Num exitPrice) {
    if (isLongTrade) {
      return exitPrice.dividedBy(entryPrice);
    }

    // For short positions, when price goes down we gain
    // If price drops from 100 to 90, we gain 0.1 (10%)
    // ratio should be 1 + (entry - exit)/entry
    return this.numFactory.one().plus(entryPrice.minus(exitPrice).dividedBy(entryPrice));

  }


  /**
   * Adjusts price to incorporate trading costs.
   */
  private static Num addCost(final Num rawPrice, final Num holdingCost, final boolean isLongTrade) {
    return isLongTrade ?
           rawPrice.minus(holdingCost) :
           rawPrice.plus(holdingCost);
  }
}
