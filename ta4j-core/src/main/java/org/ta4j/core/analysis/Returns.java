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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import lombok.Getter;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Calculates returns for a time series of prices.
 */
@Getter
public class Returns {


  private final NumFactory numFactory;

  private final ReturnType type;

  private final List<ReturnRange> returnHistory = new ArrayList<>();

  private record ReturnRange(
      Instant startTime,
      Instant endTime,
      NavigableMap<Instant, Num> returns
  ) {
  }


  public Returns(final NumFactory numFactory, final Position position, final ReturnType type) {
    this.numFactory = numFactory;
    this.type = type;

    if (position.getEntry() != null) {
      this.returnHistory.add(
          new ReturnRange(
              position.getEntry().getWhenExecuted(),
              position.getExit() != null ? position.getExit().getWhenExecuted() : Instant.MAX,
              calculatePositionReturns(position)
          )
      );
    }
  }


  public Returns(final NumFactory numFactory, final TradingRecord tradingRecord, final ReturnType type) {
    this.numFactory = numFactory;
    this.type = type;

    tradingRecord.getPositions().stream()
        .filter(p -> p.getEntry() != null)
        .forEach(position -> {
          final var returns = calculatePositionReturns(position);
          this.returnHistory.add(new ReturnRange(
              position.getEntry().getWhenExecuted(),
              position.getExit() != null ? position.getExit().getWhenExecuted() : Instant.MAX,
              returns
          ));
        });
  }


  public Num getValue(final Instant time) {
    // Sum returns from all positions active at this time
    return this.returnHistory.stream()
        .filter(range -> !time.isBefore(range.startTime()) && !time.isAfter(range.endTime()))
        .map(range -> range.returns().getOrDefault(time, this.numFactory.zero()))
        .reduce(this.numFactory.zero(), Num::plus);
  }


  /**
   * @return a list of all returns values in chronological order, starting with a zero return.
   *     For a single position, returns the initial zero return followed by calculated returns.
   *     For a trading record, returns the initial zero return followed by returns for all positions.
   */
  public List<Num> getValues() {
    final var values = new ArrayList<Num>();
    values.add(this.numFactory.zero()); // Initial return is always 0

    // Get all unique timestamps across all positions
    final var allTimestamps = this.returnHistory.stream()
        .map(ReturnRange::returns)
        .map(Map::keySet)
        .flatMap(Set::stream)
        .sorted()
        .distinct()
        .toList();

    // Add accumulated returns for each timestamp
    allTimestamps.forEach(timestamp ->
        values.add(getValue(timestamp))
    );

    return values;
  }


  public int getSize() {
    return this.returnHistory.size();
  }


  private NavigableMap<Instant, Num> calculatePositionReturns(final Position position) {
    return position.getReturns(this.type);
  }


  public enum ReturnType {
    LOG {
      @Override
      public Num calculate(final Num xNew, final Num xOld, final NumFactory numFactory) {
        return (xNew.dividedBy(xOld)).log();
      }
    },
    ARITHMETIC {
      @Override
      public Num calculate(final Num xNew, final Num xOld, final NumFactory numFactory) {
        return xNew.dividedBy(xOld).minus(numFactory.one());
      }
    };


    public abstract Num calculate(Num xNew, Num xOld, NumFactory numFactory);
  }
}
