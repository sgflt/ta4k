/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.analysis.Returns;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * A {@code Position} is a pair of two {@link Trade trades}.
 *
 * <p>
 * The exit trade has the complement type of the entry trade, i.e.:
 * <ul>
 * <li>entry == BUY --> exit == SELL
 * <li>entry == SELL --> exit == BUY
 * </ul>
 */
@Slf4j
@Getter
public class Position implements BarListener {

  /** The entry trade */
  private Trade entry;

  /** The exit trade */
  private Trade exit;

  private final PositionHistory priceHistory = new PositionHistory();

  /** The type of the entry trade */
  private final TradeType startingType;

  /** The cost model for transactions of the asset */
  private final CostModel transactionCostModel;

  /** The cost model for holding the asset */
  private final CostModel holdingCostModel;
  private final NumFactory numFactory;


  /** Constructor with {@link #startingType} = BUY. */
  public Position(final NumFactory numFactory) {
    this(TradeType.BUY, numFactory);
  }


  /**
   * Constructor.
   *
   * @param startingType the starting {@link TradeType trade type} of the position
   *     (i.e. type of the entry trade)
   */
  public Position(final TradeType startingType, final NumFactory numFactory) {
    this(startingType, new ZeroCostModel(), new ZeroCostModel(), numFactory);
  }


  /**
   * Constructor.
   *
   * @param startingType the starting {@link TradeType trade type} of the
   *     position (i.e. type of the entry trade)
   * @param transactionCostModel the cost model for transactions of the asset
   * @param holdingCostModel the cost model for holding asset (e.g. borrowing)
   */
  public Position(
      final TradeType startingType,
      final CostModel transactionCostModel,
      final CostModel holdingCostModel,
      final NumFactory numFactory
  ) {
    this.numFactory = numFactory;
    if (startingType == null) {
      throw new IllegalArgumentException("Starting type must not be null");
    }
    this.startingType = startingType;
    this.transactionCostModel = transactionCostModel;
    this.holdingCostModel = holdingCostModel;
  }


  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final Position p) {
      return (this.entry == null ? p.getEntry() == null : this.entry.equals(p.getEntry()))
             && (this.exit == null ? p.getExit() == null : this.exit.equals(p.getExit()));
    }
    return false;
  }


  @Override
  public int hashCode() {
    return Objects.hash(this.entry, this.exit);
  }


  /**
   * Operates the position at the index-th position.
   *
   * @param whenExecuted the executed bar
   * @param amount the amount
   *
   * @return the trade
   *
   * @throws IllegalStateException if {@link #isOpened()}
   */
  public Trade operate(final Instant whenExecuted, final Num pricePerAsset, final Num amount) {
    Trade trade = null;
    if (isNew()) {
      trade = Trade.builder()
          .whenExecuted(whenExecuted)
          .pricePerAsset(pricePerAsset)
          .type(this.startingType)
          .orderType(Trade.OrderType.OPEN)
          .amount(amount)
          .transactionCostModel(this.transactionCostModel)
          .build();
      this.entry = trade;
    } else if (isOpened()) {
      trade = Trade.builder()
          .whenExecuted(whenExecuted)
          .pricePerAsset(pricePerAsset)
          .type(this.startingType)
          .orderType(Trade.OrderType.CLOSE)
          .amount(amount)
          .transactionCostModel(this.transactionCostModel)
          .build();
      this.exit = trade;
    }
    return trade;
  }


  private boolean isLong() {
    return this.entry != null && this.entry.isBuy();
  }


  /**
   * @return true if the position is closed, false otherwise
   */
  public boolean isClosed() {
    return (this.entry != null) && (this.exit != null);
  }


  /**
   * @return true if the position is opened, false otherwise
   */
  public boolean isOpened() {
    return this.entry != null && this.exit == null;
  }


  /**
   * @return true if the position is new, false otherwise
   */
  public boolean isNew() {
    return this.entry == null && this.exit == null;
  }


  /**
   * @return true if position is closed and {@link #getProfit()} > 0
   */
  public boolean hasProfit() {
    return getProfit().isPositive();
  }


  /**
   * @return true if position is closed and {@link #getProfit()} < 0
   */
  public boolean hasLoss() {
    return getProfit().isNegative();
  }


  /**
   * Calculates the net profit of the position if it is closed. The net profit
   * includes any trading costs.
   *
   * @return the profit or loss of the position
   */
  public Num getProfit() {
    if (isOpened()) {
      return zero();
    } else {
      return getGrossProfit(this.exit.getPricePerAsset()).minus(getPositionCost());
    }
  }


  /**
   * Calculates the net profit of the position. If it is open, calculates the
   * profit until the final bar. The net profit includes any trading costs.
   *
   * @param finalIndex the index of the final bar to be considered (if position is
   *     open)
   * @param finalPrice the price of the final bar to be considered (if position is
   *     open)
   *
   * @return the profit or loss of the position
   */
  public Num getProfit(final int finalIndex, final Num finalPrice) {
    final Num grossProfit = getGrossProfit(finalPrice);
    final Num tradingCost = getPositionCost(finalIndex);
    return grossProfit.minus(tradingCost);
  }


  /**
   * Calculates the gross profit of the position if it is closed. The gross profit
   * excludes any trading costs.
   *
   * @return the gross profit of the position
   */
  public Num getGrossProfit() {
    if (isOpened()) {
      return zero();
    } else {
      return getGrossProfit(this.exit.getPricePerAsset());
    }
  }


  /**
   * Calculates the gross profit of the position. The gross profit excludes any
   * trading costs.
   *
   * @param finalPrice the price of the final bar to be considered (if position is
   *     open)
   *
   * @return the profit or loss of the position
   */
  public Num getGrossProfit(final Num finalPrice) {
    Num grossProfit;
    if (isOpened()) {
      grossProfit = this.entry.getAmount().multipliedBy(finalPrice).minus(this.entry.getValue());
    } else {
      grossProfit = this.exit.getValue().minus(this.entry.getValue());
    }

    // Profits of long position are losses of short
    if (this.entry.isSell()) {
      grossProfit = grossProfit.negate();
    }
    return grossProfit;
  }


  /**
   * Calculates the gross return of the position if it is closed. The gross return
   * excludes any trading costs (and includes the base).
   *
   * @return the gross return of the position in percent
   *
   * @see #getGrossReturn(Num)
   */
  public Num getGrossReturn() {
    if (isOpened()) {
      return zero();
    } else {
      return getGrossReturn(this.exit.getPricePerAsset());
    }
  }


  /**
   * Calculates the gross return of the position, if it exited at the provided
   * price. The gross return excludes any trading costs (and includes the base).
   *
   * @param finalPrice the price of the final bar to be considered (if position is
   *     open)
   *
   * @return the gross return of the position in percent
   *
   * @see #getGrossReturn(Num, Num)
   */
  public Num getGrossReturn(final Num finalPrice) {
    return getGrossReturn(getEntry().getPricePerAsset(), finalPrice);
  }


  /**
   * Calculates the gross return between entry and exit price in percent. Includes
   * the base.
   *
   * <p>
   * For example:
   * <ul>
   * <li>For buy position with a profit of 4%, it returns 1.04 (includes the base)
   * <li>For sell position with a loss of 4%, it returns 0.96 (includes the base)
   * </ul>
   *
   * @param entryPrice the entry price
   * @param exitPrice the exit price
   *
   * @return the gross return in percent between entryPrice and exitPrice
   *     (includes the base)
   */
  public Num getGrossReturn(final Num entryPrice, final Num exitPrice) {
    if (getEntry().isBuy()) {
      return exitPrice.dividedBy(entryPrice);
    } else {
      final Num one = entryPrice.getNumFactory().one();
      return ((exitPrice.dividedBy(entryPrice).minus(one)).negate()).plus(one);
    }
  }


  /**
   * Calculates the total cost of the position.
   *
   * @param finalIndex the index of the final bar to be considered (if position is
   *     open)
   *
   * @return the cost of the position
   */
  public Num getPositionCost(final int finalIndex) {
    final Num transactionCost = this.transactionCostModel.calculate(this, finalIndex);
    final Num borrowingCost = getHoldingCost(finalIndex);
    return transactionCost.plus(borrowingCost);
  }


  /**
   * Calculates the total cost of the closed position.
   *
   * @return the cost of the position
   */
  public Num getPositionCost() {
    final Num transactionCost = this.transactionCostModel.calculate(this);
    final Num borrowingCost = getHoldingCost();
    return transactionCost.plus(borrowingCost);
  }


  /**
   * Calculates the holding cost of the closed position.
   *
   * @return the cost of the position
   */
  public Num getHoldingCost() {
    return this.holdingCostModel.calculate(this);
  }


  /**
   * Calculates the holding cost of the position.
   *
   * @param finalIndex the index of the final bar to be considered (if position is
   *     open)
   *
   * @return the cost of the position
   */
  public Num getHoldingCost(final int finalIndex) {
    return this.holdingCostModel.calculate(this, finalIndex);
  }


  /**
   * @return the Num of 0
   */
  private Num zero() {
    return this.entry.getNetPrice().getNumFactory().zero();
  }


  @Override
  public void onBar(final Bar bar) {
    if (isOpened()) {
      this.priceHistory.onBar(bar);
    }
  }


  public List<PositionValue> getCashFlow() {
    return this.priceHistory.getCashFlow();
  }


  @Override
  public String toString() {
    return "Entry: " + this.entry + " exit: " + this.exit;
  }


  /**
   * Calculates the maximum drawdown from a cash flow over a series.
   *
   * The formula is as follows:
   *
   * <pre>
   * MDD = (LP - PV) / PV
   * with MDD: Maximum drawdown, in percent.
   * with LP: Lowest point (lowest value after peak value).
   * with PV: Peak value (highest value within the observation).
   * </pre>
   *
   * @return the maximum drawdown from a cash flow over a series
   */
  public Num getMaxDrawdown() {
    final var values = new CashFlow(this).getValues();
    if (values.isEmpty()) {
      log.debug("No values in cash flow - returning zero drawdown");
      return this.numFactory.zero();
    }

    var maxPeak = this.numFactory.one();
    var maximumDrawdown = this.numFactory.zero();

    log.debug("Calculating maximum drawdown for {} position", isLong() ? "long" : "short");

    for (final var entry : values.entrySet()) {
      final var time = entry.getKey();
      final var value = entry.getValue();
      final var adjustedValue = isLong() ? value : this.numFactory.one().dividedBy(value);

      log.debug("Processing value at {}: raw={}, adjusted={}", time, value, adjustedValue);

      // Update peak if we have a new high
      if (adjustedValue.isGreaterThan(maxPeak)) {
        log.debug("New peak found: {} -> {}", maxPeak, adjustedValue);
        maxPeak = adjustedValue;
      }

      // Calculate drawdown from peak
      final var drawdown = maxPeak.minus(adjustedValue).dividedBy(maxPeak);
      log.debug("Current drawdown: {} (peak={}, value={})", drawdown, maxPeak, adjustedValue);

      if (drawdown.isGreaterThan(maximumDrawdown)) {
        log.debug("New maximum drawdown: {} -> {}", maximumDrawdown, drawdown);
        maximumDrawdown = drawdown;
      }
    }

    log.debug("Final maximum drawdown: {}", maximumDrawdown);
    return maximumDrawdown;
  }


  /**
   * Calculates returns for the position using specified return type.
   *
   * @param returnType type of return calculation to use
   *
   * @return Returns instance representing position returns over time
   */
  public NavigableMap<Instant, Num> getReturns(final Returns.ReturnType returnType) {
    if (this.priceHistory.priceHistory.isEmpty()) {
      return new TreeMap<>(); // Empty returns if no price history
    }

    final var returns = new TreeMap<Instant, Num>();
    final var isLongTrade = this.entry.isBuy();
    final var holdingCost = getHoldingCost();
    var previousPrice = this.entry.getNetPrice();

    for (final var bar : this.priceHistory.priceHistory) {
      final var currentPrice = bar.closePrice();
      final var adjustedPrice = isLongTrade ?
                                currentPrice.minus(holdingCost) :
                                currentPrice.plus(holdingCost);

      final var assetReturn = returnType.calculate(adjustedPrice, previousPrice, this.numFactory);
      final var strategyReturn = isLongTrade ? assetReturn : assetReturn.negate();

      returns.put(bar.endTime(), strategyReturn);
      previousPrice = currentPrice;
    }

    return returns;
  }


  /**
   * @param when at which time point we measure value
   * @param value at time point
   */
  public record PositionValue(Instant when, Num value) {

  }

  /**
   * accumulates bars during lifetime of position
   */
  public class PositionHistory implements BarListener {

    private final List<Bar> priceHistory = new ArrayList<>();


    public List<PositionValue> getCashFlow() {
      return this.priceHistory.stream()
          .map(bar -> {
                final var currentPrice = bar.closePrice();
                final var adjustedPrice = isLong() // FIXME holding cost is over night, usually not during day
                                          ? currentPrice.minus(getHoldingCost())
                                          : currentPrice.plus(getHoldingCost());

                final var ratio = calculatePriceRatio(isLong(), Position.this.entry.getPricePerAsset(), adjustedPrice);
                return new PositionValue(bar.endTime(), ratio);
              }

          ).toList();
    }


    private Num calculatePriceRatio(final boolean isLongTrade, final Num entryPrice, final Num currentPrice) {
      if (isLongTrade) {
        return currentPrice.dividedBy(entryPrice);
      }

      // For short positions
      return Position.this.numFactory.one().plus(entryPrice.minus(currentPrice).dividedBy(entryPrice));
    }


    @Override
    public void onBar(final Bar bar) {
      this.priceHistory.add(bar);
    }
  }
}
