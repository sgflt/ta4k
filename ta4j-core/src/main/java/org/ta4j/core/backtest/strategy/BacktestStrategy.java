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
import java.util.Objects;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarListener;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.RuntimeContext;
import org.ta4j.core.Strategy;
import org.ta4j.core.TickListener;
import org.ta4j.core.backtest.OperationType;
import org.ta4j.core.events.TickReceived;

/**
 * This implementation is designed for backtesting of custom strategy.
 *
 * It adds trading record for performance analysis.
 *
 * Tested strategy is wrapped into this adaptation class.
 */
@ToString
public class BacktestStrategy implements Strategy, BarListener, TickListener {

  /** The logger. */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Tested strategy
   */
  private final Strategy testedStrategy;

  /** Recording of execution of this strategy */
  private final RuntimeContext runtimeContext;

  /** Current time */
  private Instant currentTime;


  public BacktestStrategy(
      final Strategy testedStrategy,
      final RuntimeContext runtimeContext
  ) {
    this.testedStrategy = testedStrategy;
    this.runtimeContext = Objects.requireNonNull(runtimeContext, "RuntimeContext cannot be null");
  }


  @Override
  public String name() {
    return this.testedStrategy.name();
  }


  @Override
  public Rule entryRule() {
    return this.testedStrategy.entryRule();
  }


  @Override
  public Rule exitRule() {
    return this.testedStrategy.exitRule();
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
    final Position position = this.runtimeContext.getValue(new BackTestTradingRecord.CurrentPositionResolver());
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
    this.currentTime = bar.endTime();
  }


  @Override
  public void onTick(final TickReceived tick) {
    this.currentTime = tick.beginTime();
  }


  /**
   * Traces the {@code shouldEnter()} method calls.
   *
   * @param enter true if the strategy should enter, false otherwise
   */
  protected void traceShouldEnter(final boolean enter) {
    if (this.log.isTraceEnabled()) {
      this.log.trace(">>> {}#shouldEnter({}): {}", name(), this.currentTime, enter);
    }
  }


  /**
   * Traces the {@code shouldExit()} method calls.
   *
   * @param exit true if the strategy should exit, false otherwise
   */
  protected void traceShouldExit(final boolean exit) {
    if (this.log.isTraceEnabled()) {
      this.log.trace(">>> {}#shouldExit({}): {}", name(), this.currentTime, exit);
    }
  }


  public BackTestTradingRecord getTradeRecord() {
    return this.runtimeContext.getValue(new BackTestTradingRecord.TradeRecordResolver());
  }
}
