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
package org.ta4j.core.backtest;

import java.time.Instant;
import java.util.Objects;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarListener;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

/**
 * This implementation is designed for backtesting of custom strategy.
 *
 * It adds trading record for performance analysis.
 *
 * Tested strategy is wrapped into this adaptation class.
 */
@ToString
public class BacktestStrategy implements Strategy, BarListener {

  /** The logger. */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Tested strategy
   */
  private final Strategy testedStrategy;

  /** Recording of execution of this strategy */
  private final BackTestTradingRecord tradingRecord;

  /** Current time */
  private Instant currentTick;


  public BacktestStrategy(
      final Strategy testedStrategy,
      final BackTestTradingRecord tradingRecord
  ) {
    this.testedStrategy = testedStrategy;
    this.tradingRecord = Objects.requireNonNull(tradingRecord, "TradingRecord cannot be null");
  }


  @Override
  public String getName() {
    return this.testedStrategy.getName();
  }


  @Override
  public Rule getEntryRule() {
    return this.testedStrategy.getEntryRule();
  }


  @Override
  public Rule getExitRule() {
    return this.testedStrategy.getExitRule();
  }


  @Override
  public boolean isStable() {
    return this.testedStrategy.isStable();
  }


  /**
   * @return {@link OperationType#ENTER} to recommend entering, {@link OperationType#EXIT} to recommend exit, otherwise
   *     (no recommendation)
   */
  public OperationType shouldOperate() {
    final Position position = this.tradingRecord.getCurrentPosition();
    if (position.isNew()) {
      if (shouldEnter()) {
        return OperationType.ENTER;
      }
    } else if (position.isOpened() && shouldExit()) {
      return OperationType.EXIT;
    }
    return OperationType.NOOP;
  }


  @Override
  public boolean shouldEnter() {
    final boolean enter = this.testedStrategy.shouldEnter();
    traceShouldEnter(enter);
    return enter;
  }


  @Override
  public boolean shouldExit() {
    final boolean exit = this.testedStrategy.shouldExit();
    traceShouldExit(exit);
    return exit;
  }


  @Override
  public void onBar(final Bar bar) {
    this.currentTick = bar.endTime();
    this.tradingRecord.onBar(bar);
  }


  /**
   * Traces the {@code shouldEnter()} method calls.
   *
   * @param enter true if the strategy should enter, false otherwise
   */
  protected void traceShouldEnter(final boolean enter) {
    if (this.log.isTraceEnabled()) {
      this.log.trace(">>> {}#shouldEnter({}): {}", getName(), this.currentTick, enter);
    }
  }


  /**
   * Traces the {@code shouldExit()} method calls.
   *
   * @param exit true if the strategy should exit, false otherwise
   */
  protected void traceShouldExit(final boolean exit) {
    if (this.log.isTraceEnabled()) {
      this.log.trace(">>> {}#shouldExit({}): {}", getName(), this.currentTick, exit);
    }
  }


  public TradingRecord getTradeRecord() {
    return this.tradingRecord;
  }
}
