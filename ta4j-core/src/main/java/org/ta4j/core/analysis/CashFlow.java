/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.analysis;

import java.time.Instant;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * Tracks the portfolio value evolution over time considering all price movements.
 */
public class CashFlow {

  /** The portfolio value mapped to timestamps */
  private final NavigableMap<Instant, Num> values;


  /**
   * Constructor for cash flows of a single position.
   *
   * @param position a single position
   */
  public CashFlow(final Position position) {
    this.values = new TreeMap<>();

    if (position.getEntry() != null) {
      calculatePositionValues(position);
    }
  }


  /**
   * Constructor for cash flows of a trading record.
   *
   * @param tradingRecord the trading record
   */
  public CashFlow(final TradingRecord tradingRecord) {
    this.values = new TreeMap<>();

    tradingRecord.getPositions().stream()
        .filter(p -> p.getEntry() != null)
        .forEach(this::calculatePositionValues);

    // Calculate for current open position
    final var currentPosition = tradingRecord.getCurrentPosition();
    if (currentPosition.isOpened()) {
      calculatePositionValues(currentPosition);
    }
  }


  /**
   * @return all cash flow values with their timestamps
   */
  public NavigableMap<Instant, Num> getValues() {
    return new TreeMap<>(this.values);
  }


  /**
   * Gets the cash flow value at a specific instant.
   *
   * @param instant the point in time
   * @return the cash flow value (returns 1 if no value exists for the instant)
   */
  public Num getValue(final Instant instant) {
    return this.values.getOrDefault(instant, NumFactoryProvider.getDefaultNumFactory().one());
  }


  private void calculatePositionValues(final Position position) {
    position.getCashFlow()
        .forEach(positionValue -> this.values.compute(
            positionValue.when(), (k, v) -> sumUpMultiplePositions(positionValue, v)
        ));
  }


  private static Num sumUpMultiplePositions(final Position.PositionValue positionValue, final Num oldValue) {
    if (oldValue == null) {
      return positionValue.value();
    }

    return oldValue.plus(positionValue.value());
  }
}
