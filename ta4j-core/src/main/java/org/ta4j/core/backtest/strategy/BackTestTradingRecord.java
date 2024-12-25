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
package org.ta4j.core.backtest.strategy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.TradeType;
import org.ta4j.core.api.series.Bar;
import org.ta4j.core.api.strategy.RuntimeContext;
import org.ta4j.core.api.strategy.RuntimeValueResolver;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.Trade;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.analysis.cost.CostModel;
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel;
import org.ta4j.core.events.TickReceived;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Base implementation of a {@link TradingRecord}.
 */
@Slf4j
public class BackTestTradingRecord implements TradingRecord, RuntimeContext {

  private final TradeType startingType;
  /** The name of the trading record. */
  private String name;

  /** The recorded trades. */
  @Getter
  private final List<Trade> trades = new ArrayList<>();

  /** The recorded positions. */
  private final List<Position> positions = new ArrayList<>();

  /** The current non-closed position (there's always one). */
  private Position currentPosition;

  /** The cost model for transactions of the asset. */
  private final CostModel transactionCostModel;

  /** The cost model for holding asset (e.g. borrowing). */
  private final CostModel holdingCostModel;
  private final NumFactory numFactory;


  /** Constructor with {@link #startingType} = BUY. */
  public BackTestTradingRecord(final NumFactory numFactory) {
    this(TradeType.BUY, numFactory);
  }


  /**
   * Constructor with {@link #startingType} = BUY.
   *
   * @param name the name of the tradingRecord
   */
  public BackTestTradingRecord(final String name, final NumFactory numFactory) {
    this(TradeType.BUY, numFactory);
    this.name = name;
  }


  /**
   * Constructor.
   *
   * @param name the name of the trading record
   * @param tradeType the {@link TradeType trade type} of entries in the trading
   *     session
   */
  public BackTestTradingRecord(final String name, final TradeType tradeType, final NumFactory numFactory) {
    this(tradeType, new ZeroCostModel(), new ZeroCostModel(), numFactory);
    this.name = name;
  }


  /**
   * Constructor.
   *
   * @param tradeType the {@link TradeType trade type} of entries in the trading
   *     session
   */
  public BackTestTradingRecord(final TradeType tradeType, final NumFactory numFactory) {
    this(tradeType, new ZeroCostModel(), new ZeroCostModel(), numFactory);
  }


  /**
   * Constructor.
   *
   * @param entryTradeType the {@link TradeType trade type} of entries in
   *     the trading session
   * @param transactionCostModel the cost model for transactions of the asset
   * @param holdingCostModel the cost model for holding the asset (e.g.
   *     borrowing)
   *
   * @throws NullPointerException if entryTradeType is null
   */
  public BackTestTradingRecord(
      final TradeType entryTradeType,
      final CostModel transactionCostModel,
      final CostModel holdingCostModel,
      final NumFactory numFactory
  ) {
    this.startingType = Objects.requireNonNull(entryTradeType, "Starting type must not be null");
    this.transactionCostModel = transactionCostModel;
    this.holdingCostModel = holdingCostModel;
    this.numFactory = numFactory;
    this.currentPosition = new Position(entryTradeType, transactionCostModel, holdingCostModel, numFactory);
  }


  @Override
  public String getName() {
    return this.name;
  }


  @Override
  public TradeType getStartingType() {
    return this.startingType;
  }


  @Override
  public Position getCurrentPosition() {
    return this.currentPosition;
  }


  @Override
  public Num getMaximumDrawdown() {
    return this.positions.stream()
        .max(Comparator.comparing(Position::getMaxDrawdown))
        .map(Position::getMaxDrawdown)
        .orElse(this.numFactory.zero())
        ;
  }


  private void operate(final Instant whenExecuted, final Num pricePerAsset, final Num amount) {
    if (this.currentPosition.isClosed()) {
      // Current position closed, should not occur
      throw new IllegalStateException("Current position should not be closed");
    }

    final var newTrade = this.currentPosition.operate(whenExecuted, pricePerAsset, amount);
    recordTrade(newTrade);
  }


  @Override
  public boolean enter(final Instant whenExecuted, final Num pricePerAsset, final Num amount) {
    if (this.currentPosition.isNew()) {
      operate(whenExecuted, pricePerAsset, amount);
      return true;
    }

    return false;
  }


  @Override
  public boolean exit(final Instant whenExecuted, final Num pricePerAsset, final Num amount) {
    if (this.currentPosition.isOpened()) {
      operate(whenExecuted, pricePerAsset, amount);
      return true;
    }

    return false;
  }


  @Override
  public List<Position> getPositions() {
    return this.positions;
  }


  @Override
  public Trade getLastTrade() {
    if (!this.trades.isEmpty()) {
      return this.trades.getLast();
    }
    return null;
  }


  @Override
  public Trade getLastTrade(final TradeType tradeType) {
    return this.trades.stream()
        .sorted(Comparator.comparing(Trade::getWhenExecuted).reversed())
        .filter(trade -> trade.getType() == tradeType)
        .findFirst()
        .orElse(null);
  }


  @Override
  public Trade getLastEntry() {
    return this.trades.stream()
        .sorted(Comparator.comparing(Trade::getWhenExecuted).reversed())
        .filter(trade -> trade.getOrderType() == Trade.OrderType.OPEN)
        .findFirst()
        .orElse(null);
  }


  @Override
  public Trade getLastExit() {
    return this.trades.stream()
        .sorted(Comparator.comparing(Trade::getWhenExecuted).reversed())
        .filter(trade -> trade.getOrderType() == Trade.OrderType.CLOSE)
        .findFirst()
        .orElse(null);
  }


  @Override
  public boolean isEmpty() {
    return this.trades.isEmpty();
  }


  /**
   * Records a trade and the corresponding position (if closed).
   *
   * @param trade the trade to be recorded
   *
   * @throws NullPointerException if trade is null
   */
  private void recordTrade(final Trade trade) {
    Objects.requireNonNull(trade, "Trade should not be null");

    // Storing the new trade in trades list
    this.trades.add(trade);

    // Storing the position if closed
    if (this.currentPosition.isClosed()) {
      this.positions.add(this.currentPosition);
      this.currentPosition =
          new Position(this.startingType, this.transactionCostModel, this.holdingCostModel, this.numFactory);
    }
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append("BackTestTradingRecord: ")
        .append(this.name == null ? "" : this.name)
        .append(System.lineSeparator());
    for (final var trade : this.trades) {
      sb.append(trade.toString()).append(System.lineSeparator());
    }
    return sb.toString();
  }


  @Override
  public void onBar(final Bar bar) {
    this.currentPosition.onBar(bar);  // TODO allow DCA
  }


  @Override
  public void onTick(final TickReceived event) {
    log.debug("onTick: {}", event);
  }


  @Override
  public <T> T getValue(final RuntimeValueResolver<T> resolver) {
    return resolver.resolve(this);
  }


  @Override
  public Object getValue(final String key) {
    return switch (key) {
      case RuntimeContextKeys.CURRENT_POSITION -> this.currentPosition;
      case RuntimeContextKeys.BACKTEST_TRADING_RECORD -> this;
      default -> null;
    };
  }


  @UtilityClass
  public static final class RuntimeContextKeys {
    public static final String CURRENT_POSITION = "currentPosition";
    public static final String BACKTEST_TRADING_RECORD = "backtestTradingRecord";
  }


  public static final class CurrentPositionResolver implements RuntimeValueResolver<Position> {
    @Override
    public Position resolve(final RuntimeContext context) {
      return (Position) context.getValue(RuntimeContextKeys.CURRENT_POSITION);
    }
  }

  public static final class TradeRecordResolver implements RuntimeValueResolver<BackTestTradingRecord> {
    @Override
    public BackTestTradingRecord resolve(final RuntimeContext context) {
      return (BackTestTradingRecord) context.getValue(RuntimeContextKeys.BACKTEST_TRADING_RECORD);
    }
  }

}
