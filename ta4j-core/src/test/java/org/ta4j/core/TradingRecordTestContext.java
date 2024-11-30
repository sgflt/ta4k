package org.ta4j.core;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * @author Lukáš Kvídera
 */
public class TradingRecordTestContext {

  private Clock clock = Clock.fixed(Instant.MIN, ZoneId.systemDefault());
  private Trade.TradeType tradeType = Trade.TradeType.BUY;
  private BackTestTradingRecord tradingRecord = new BackTestTradingRecord(this.tradeType);
  private NumFactory numFactory;
  private AnalysisCriterion criterion;
  private CostModel transactionCostModel = new ZeroCostModel();
  private CostModel holdingCostModel = new ZeroCostModel();
  private boolean useRandomDelays = true;
  private int operationCount;
  private BacktestBarSeries series;
  private Duration duration;


  public TradingRecordTestContext withNumFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    NumFactoryProvider.setDefaultNumFactory(numFactory);
    return this;
  }


  public TradingRecordTestContext withTradeType(final Trade.TradeType tradeType) {
    this.tradeType = tradeType;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecordTestContext withRelatedSeries(final BacktestBarSeries series) {
    this.series = series;
    return this;
  }


  private void reinitalizeTradingRecord() {
    this.tradingRecord = new BackTestTradingRecord(this.tradeType, this.transactionCostModel, this.holdingCostModel);
  }


  public Operation operate(final int amount) {
    return new Operation(amount);
  }


  public TradingRecordTestContext withCriterion(final AnalysisCriterion criterion) {
    this.criterion = criterion;
    return this;
  }


  public TradingRecordTestContext withSeriesRelatedCriterion(final Function<BacktestBarSeries, AnalysisCriterion> criterionFactory) {
    this.criterion = criterionFactory.apply(this.series);
    return this;
  }


  public TradingRecordTestContext withTransactionCostModel(final CostModel transactionCostModel) {
    this.transactionCostModel = transactionCostModel;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecordTestContext withHoldingCostModel(final CostModel holdingCostModel) {
    this.holdingCostModel = holdingCostModel;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecordTestContext withConstantTimeDelays() {
    this.useRandomDelays = false;
    return this;
  }


  public TradingRecord getTradingRecord() {
    return this.tradingRecord;
  }


  public void assertResults(final double expected) {
    assertNumEquals(expected, this.criterion.calculate(this.tradingRecord));
  }


  public TradingRecordTestContext forwardTime(final int minutes) {
    this.operationCount += minutes;
    return this;
  }


  public TradingRecordTestContext withDuration(final Duration duration) {
    this.duration = duration;
    return this;
  }


  public TradingRecordTestContext withStart(final Clock clock) {
    this.clock = clock;
    return this;
  }


  public class Operation {
    private final double amount;


    public Operation(final int amount) {
      this.amount = amount;
    }


    public TradingRecordTestContext at(final double price) {
      TradingRecordTestContext.this.tradingRecord.operate(
          Instant.now(
              Clock.offset(
                  TradingRecordTestContext.this.clock,
                  getTimeDelay()
              )
          ),
          TradingRecordTestContext.this.numFactory.numOf(price),
          TradingRecordTestContext.this.numFactory.numOf(this.amount)
      );
      return TradingRecordTestContext.this;
    }
  }


  private Duration getTimeDelay() {
    if (this.duration != null) {
      return this.duration.multipliedBy(TradingRecordTestContext.this.operationCount++);
    }

    if (TradingRecordTestContext.this.useRandomDelays) {
      return Duration.ofMinutes(ThreadLocalRandom.current().nextInt(10));
    }

    return Duration.ofMinutes(TradingRecordTestContext.this.operationCount++);
  }
}
