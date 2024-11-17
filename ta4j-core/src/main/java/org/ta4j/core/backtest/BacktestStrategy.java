/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.IndicatorContext;

/**
 * Base implementation of a {@link Strategy}.
 */
public class BacktestStrategy implements Strategy {

  /** The logger. */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** The name of the strategy. */
  private final String name;

  /** The entry rule. */
  private final Rule entryRule;

  /** The exit rule. */
  private final Rule exitRule;
  private final IndicatorContext indicatorContext;

  /** Recording of execution of this strategy */
  private BackTestTradingRecord tradingRecord;

  /** Current time */
  private Instant currentTick;


  /**
   * Constructor.
   *
   * @param name the name of the strategy
   * @param entryRule the entry rule
   * @param exitRule the exit rule
   */
  public BacktestStrategy(
      final String name,
      final Rule entryRule,
      final Rule exitRule,
      final IndicatorContext indicatorContext
  ) {
    this.indicatorContext = indicatorContext;
    if (entryRule == null || exitRule == null) {
      throw new IllegalArgumentException("Rules cannot be null");
    }
    if (indicatorContext == null) {
      throw new IllegalArgumentException("IndicatorContext cannot be null");
    }
    this.name = name;
    this.entryRule = entryRule;
    this.exitRule = exitRule;
  }


  @Override
  public String getName() {
    return this.name;
  }


  @Override
  public Rule getEntryRule() {
    return this.entryRule;
  }


  @Override
  public Rule getExitRule() {
    return this.exitRule;
  }


  @Override
  public boolean isStable() {
    return this.indicatorContext.isStable();
  }


  /**
   * @return true to recommend a trade, false otherwise (no recommendation)
   */
  public boolean shouldOperate() {
    final Position position = this.tradingRecord.getCurrentPosition();
    if (position.isNew()) {
      return shouldEnter();
    } else if (position.isOpened()) {
      return shouldExit();
    }
    return false;
  }


  @Override
  public boolean shouldEnter() {
    final boolean enter = Strategy.super.shouldEnter();
    traceShouldEnter(enter);
    return enter;
  }


  @Override
  public boolean shouldExit() {
    final boolean exit = Strategy.super.shouldExit();
    traceShouldExit(exit);
    return exit;
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


  public void register(final BackTestTradingRecord backTestTradingRecord) {
    this.tradingRecord = backTestTradingRecord;
  }


  @Override
  public String toString() {
    return "BacktestStrategy{" + "className='" + getClass().getSimpleName() + '\'' + ", name='" + this.name + '\''
           + ", entryRule=" + this.entryRule + ", exitRule=" + this.exitRule + '}';
  }
}
