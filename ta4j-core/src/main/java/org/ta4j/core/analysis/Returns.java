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
import java.util.NavigableMap;
import java.util.TreeMap;

import lombok.Getter;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Calculates returns for a time series of prices.
 */
@Getter
public class Returns {

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

  private final ReturnType type;
  private final BacktestBarSeries barSeries;
  private final NavigableMap<Instant, Num> returns = new TreeMap<>();


  public Returns(final BacktestBarSeries barSeries, final Position position, final ReturnType type) {
    this.barSeries = barSeries;
    this.type = type;

    if (position.getEntry() != null) {
      this.returns.putAll(calculatePositionReturns(position));
    }
  }


  public Returns(final BacktestBarSeries barSeries, final TradingRecord tradingRecord, final ReturnType type) {
    this.barSeries = barSeries;
    this.type = type;

    tradingRecord.getPositions().stream()
        .filter(p -> p.getEntry() != null)
        .map(this::calculatePositionReturns)
        .forEach(this.returns::putAll);
  }


  public Num getValue(final Instant time) {
    return this.returns.getOrDefault(time, this.barSeries.numFactory().zero());
  }


  /**
   * @return a list of all returns values in chronological order, starting with a zero return.
   * For a single position, returns the initial zero return followed by calculated returns.
   * For a trading record, returns the initial zero return followed by returns for all positions.
   */
  public List<Num> getValues() {
    final var values = new ArrayList<Num>();
    values.add(this.barSeries.numFactory().zero()); // Initial return is always 0
    values.addAll(this.returns.values());
    return values;
  }


  public int getSize() {
    return this.returns.size();
  }


  private record ReturnState(Num previousPrice, NavigableMap<Instant, Num> returns) {
  }


  private NavigableMap<Instant, Num> calculatePositionReturns(final Position position) {
    final var entryTrade = position.getEntry();
    final var exitTrade = position.getExit();
    final var isLongTrade = entryTrade.isBuy();
    final var holdingCost = position.getHoldingCost();

    final var startTime = entryTrade.getWhenExecuted();
    final var endTime = exitTrade != null ?
                        exitTrade.getWhenExecuted() :
                        this.barSeries.getLastBar().endTime();

    return this.barSeries.getBarData().stream()
        .filter(bar -> isBarInTimeRange(bar.endTime(), startTime, endTime))
        .reduce(
            new ReturnState(entryTrade.getNetPrice(), new TreeMap<>()),
            (state, bar) -> {
              final var currentPrice = bar.closePrice();
              final var adjustedPrice = isLongTrade ?
                                        currentPrice.minus(holdingCost) :
                                        currentPrice.plus(holdingCost);

              final var assetReturn =
                  this.type.calculate(adjustedPrice, state.previousPrice(), this.barSeries.numFactory());
              final var strategyReturn = isLongTrade ?
                                         assetReturn :
                                         assetReturn.negate();

              final var newReturns = new TreeMap<>(state.returns());
              newReturns.put(bar.endTime(), strategyReturn);

              return new ReturnState(currentPrice, newReturns);
            },
            (a, b) -> {
              final var combined = new TreeMap<>(a.returns());
              combined.putAll(b.returns());
              return new ReturnState(b.previousPrice(), combined);
            }
        ).returns();
  }


  private boolean isBarInTimeRange(final Instant barTime, final Instant start, final Instant end) {
    return !barTime.isBefore(start) && !barTime.isAfter(end);
  }
}
