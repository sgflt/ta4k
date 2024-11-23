package org.ta4j.core;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

import org.ta4j.core.backtest.BackTestTradingRecord;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * @author Lukáš Kvídera
 */
public class TradingRecordTestContext {

  private final Clock clock = Clock.fixed(Instant.ofEpochMilli(-1), ZoneId.systemDefault());
  private Trade.TradeType tradeType = Trade.TradeType.BUY;
  private BackTestTradingRecord tradingRecord = new BackTestTradingRecord(this.tradeType);
  private NumFactory numFactory;
  private AnalysisCriterion criterion;


  public TradingRecordTestContext withNumFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    NumFactoryProvider.setDefaultNumFactory(numFactory);
    return this;
  }


  public TradingRecordTestContext withTradeType(final Trade.TradeType tradeType) {
    this.tradeType = tradeType;
    this.tradingRecord = new BackTestTradingRecord(tradeType);
    return this;
  }


  public Operation operate(final int amount) {
    return new Operation(amount);
  }


  public TradingRecordTestContext withCriterion(final AnalysisCriterion criterion) {
    this.criterion = criterion;
    return this;
  }


  public void assertResults(final double expected) {
    assertNumEquals(expected, this.criterion.calculate(this.tradingRecord));
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
                  Duration.ofMinutes(ThreadLocalRandom.current().nextInt(10))
              )
          ),
          TradingRecordTestContext.this.numFactory.numOf(price),
          TradingRecordTestContext.this.numFactory.numOf(this.amount)
      );
      return TradingRecordTestContext.this;
    }
  }
}
