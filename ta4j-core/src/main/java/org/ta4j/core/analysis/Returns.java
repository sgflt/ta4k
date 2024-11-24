/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors
 */
package org.ta4j.core.analysis;

import java.time.Instant;
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
