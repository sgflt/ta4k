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
import java.util.Objects;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;

/**
 * A {@code Trade} is defined by:
 *
 * <ul>
 * <li>the index (in the {@link BarSeries bar series}) on which the trade is
 * executed
 * <li>a {@link TradeType type} (BUY or SELL)
 * <li>a pricePerAsset (optional)
 * <li>a trade amount (optional)
 * </ul>
 *
 * A {@link Position position} is a pair of complementary trades.
 */
@Getter
@EqualsAndHashCode
public class Trade {

  /** The type of a {@link Trade trade}. */
  public enum TradeType {

    /** A BUY corresponds to a <i>BID</i> trade. */
    BUY {
      @Override
      public TradeType complementType() {
        return SELL;
      }
    },

    /** A SELL corresponds to an <i>ASK</i> trade. */
    SELL {
      @Override
      public TradeType complementType() {
        return BUY;
      }
    };


    /**
     * @return the complementary trade type
     */
    public abstract TradeType complementType();
  }

  public enum OrderType {
    OPEN,
    CLOSE,
  }

  /** The type of the trade. */
  private final TradeType type;

  /** The type of the trade. */
  private final OrderType orderType;

  /** The index the trade was executed. */
  private final Instant whenExecuted;

  /** The trade price per asset. */
  private Num pricePerAsset;

  /**
   * The net price per asset for the trade (i.e. {@link #pricePerAsset} with
   * {@link #cost}).
   */
  private Num netPrice;

  /** The trade amount. */
  private final Num amount;

  /** The cost for executing the trade. */
  private Num cost;

  /** The cost model for trade execution. */
  private CostModel costModel;


  /**
   * Constructor.
   *
   * @param whenExecuted the time of execution
   * @param type the trade type
   * @param pricePerAsset the trade price per asset
   * @param amount the trade amount
   * @param transactionCostModel the cost model for trade execution
   */
  @Builder
  protected Trade(
      final Instant whenExecuted,
      final Num pricePerAsset,
      final TradeType type,
      final OrderType orderType,
      final Num amount,
      final CostModel transactionCostModel
  ) {
    this.type = type;
    this.orderType = orderType;
    this.whenExecuted = whenExecuted;
    this.amount = amount;

    setPricesAndCost(pricePerAsset, amount, transactionCostModel);
  }


  /**
   * Sets the raw and net prices of the trade.
   *
   * @param pricePerAsset the raw price of the asset
   * @param amount the amount of assets ordered
   * @param transactionCostModel the cost model for trade execution
   */
  private void setPricesAndCost(final Num pricePerAsset, final Num amount, final CostModel transactionCostModel) {
    this.costModel = transactionCostModel;
    this.pricePerAsset = Objects.requireNonNull(pricePerAsset);
    this.cost = transactionCostModel.calculate(this.pricePerAsset, amount);

    final Num costPerAsset = this.cost.dividedBy(amount);
    // onCandle transaction costs to the pricePerAsset at the trade
    if (isBuy()) {
      this.netPrice = this.pricePerAsset.plus(costPerAsset);
    } else {
      this.netPrice = this.pricePerAsset.minus(costPerAsset);
    }
  }


  /**
   * @return true if this is a BUY trade, false otherwise
   */
  public boolean isBuy() {
    return this.type == TradeType.BUY;
  }


  /**
   * @return true if this is a SELL trade, false otherwise
   */
  public boolean isSell() {
    return this.type == TradeType.SELL;
  }


  /**
   * @return the market value of a trade (without transaction cost)
   */
  public Num getValue() {
    return this.pricePerAsset.multipliedBy(this.amount);
  }


  public String toString() {
    return "%s %s %s | %s @ %s".formatted(
        this.type,
        this.orderType,
        this.whenExecuted,
        this.amount,
        this.pricePerAsset
    );
  }
}
