package org.ta4j.core

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.function.Function
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.backtest.BacktestBarSeries
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider
import org.ta4j.core.strategy.RuntimeContext

class TradingRecordTestContext {

    private var simulationTime: Int = 0
    private var tradeType: TradeType = TradeType.BUY
    lateinit var tradingRecord: BackTestTradingRecord
        private set
    private lateinit var criterion: AnalysisCriterion
    private var transactionCostModel: CostModel = ZeroCostModel
    private var holdingCostModel: CostModel = ZeroCostModel
    private var marketEvenTestContext: MarketEventTestContext? = null
    private var numFactory: NumFactory = NumFactoryProvider.defaultNumFactory
    private var resolution: ChronoUnit = ChronoUnit.DAYS

    constructor() {
        reinitalizeTradingRecord()
    }

    constructor(marketEventTestContext: MarketEventTestContext) {
        this.marketEvenTestContext = marketEventTestContext
        withNumFactory(marketEventTestContext.barSeries.numFactory)
        reinitalizeTradingRecord()
    }

    fun withTradeType(tradeType: TradeType): TradingRecordTestContext {
        this.tradeType = tradeType
        reinitalizeTradingRecord()
        return this
    }

    fun withNumFactory(numFactory: NumFactory): TradingRecordTestContext {
        this.numFactory = numFactory
        NumFactoryProvider.defaultNumFactory = numFactory
        reinitalizeTradingRecord()
        return this
    }

    private fun reinitalizeTradingRecord() {
        this.tradingRecord = BackTestTradingRecord(
            this.tradeType,
            "",
            this.transactionCostModel,
            this.holdingCostModel,
            this.numFactory
        )

        this.marketEvenTestContext?.withBarListener(this.tradingRecord)
    }

    fun withCriterion(criterion: AnalysisCriterion): TradingRecordTestContext {
        this.criterion = criterion
        return this
    }

    fun withSeriesRelatedCriterion(criterionFactory: Function<BacktestBarSeries, AnalysisCriterion>): TradingRecordTestContext {
        this.criterion = criterionFactory.apply(this.marketEvenTestContext!!.barSeries)
        return this
    }

    fun withTransactionCostModel(transactionCostModel: CostModel): TradingRecordTestContext {
        this.transactionCostModel = transactionCostModel
        reinitalizeTradingRecord()
        return this
    }

    fun withHoldingCostModel(holdingCostModel: CostModel): TradingRecordTestContext {
        this.holdingCostModel = holdingCostModel
        reinitalizeTradingRecord()
        return this
    }

    fun getTradingRecord(): TradingRecord {
        return this.tradingRecord
    }

    val runtimeContext: RuntimeContext
        get() = this.tradingRecord

    fun assertResults(expected: Double) {
        assertNumEquals(expected, this.criterion.calculate(this.tradingRecord))
    }

    fun enter(amount: Double): Operation {
        return EnterOperation(amount)
    }

    fun exit(amount: Double): Operation {
        return ExitOperation(amount)
    }

    private fun getNumFactory(): NumFactory {
        return this.numFactory
    }

    val barSeries: BacktestBarSeries
        get() = this.marketEvenTestContext!!.barSeries

    fun forwardTime(countOfBars: Int): TradingRecordTestContext {
        if (this.marketEvenTestContext != null) {
            this.marketEvenTestContext!!.fastForward(countOfBars)
        } else {
            this.simulationTime += countOfBars
        }
        return this
    }

    fun withResolution(resolution: ChronoUnit): TradingRecordTestContext {
        this.resolution = resolution
        return this
    }

    interface Operation {
        /**
         * This method executes operation relatively to current time.
         *
         * 0 enter immediatelly at current bar
         * 1 means one bar is replayed, enter at next bar
         *
         * ```
         * .enter(1).after(1) // wait until next bar
         * .exit(1).after(2) // wait two bars, exit at the second
         * ```
         *
         * @param countOfBars numbered from 1
         */
        fun after(countOfBars: Int): TradingRecordTestContext

        /**
         * Executes operation as soon as possible. Usually at next bar.
         */
        fun asap(): TradingRecordTestContext = after(1)

        /**
         * This method does not care about time, just mocks entry at some price.
         * Also does not refresh history of positions price.
         *
         * @param price at which we enter
         *
         * @return this
         */
        fun at(price: Double): TradingRecordTestContext
    }

    inner class EnterOperation(private val amount: Double) : Operation {

        override fun after(countOfBars: Int): TradingRecordTestContext {
            this@TradingRecordTestContext.marketEvenTestContext!!.fastForward(countOfBars)

            val bar = this@TradingRecordTestContext.marketEvenTestContext!!.barSeries.bar
            this@TradingRecordTestContext.tradingRecord.enter(
                bar.endTime,
                bar.closePrice,
                getNumFactory().numOf(this.amount)
            )

            // ad bar to positions history
            this@TradingRecordTestContext.tradingRecord.onBar(bar)

            return this@TradingRecordTestContext
        }

        override fun at(price: Double): TradingRecordTestContext {
            this@TradingRecordTestContext.tradingRecord.enter(
                getSimulatedTime(),
                getNumFactory().numOf(price),
                getNumFactory().numOf(this.amount)
            )

            return this@TradingRecordTestContext
        }
    }

    inner class ExitOperation(private val amount: Double) : Operation {

        override fun after(orderOfReceivedCandle: Int): TradingRecordTestContext {
            this@TradingRecordTestContext.marketEvenTestContext!!.fastForward(orderOfReceivedCandle)

            val bar = this@TradingRecordTestContext.marketEvenTestContext!!.barSeries.bar
            this@TradingRecordTestContext.tradingRecord.exit(
                bar.endTime,
                bar.closePrice,
                getNumFactory().numOf(this.amount)
            )

            // ad bar to positions history
            this@TradingRecordTestContext.tradingRecord.onBar(bar)

            return this@TradingRecordTestContext
        }

        override fun at(price: Double): TradingRecordTestContext {
            this@TradingRecordTestContext.tradingRecord.exit(
                getSimulatedTime(),
                getNumFactory().numOf(price),
                getNumFactory().numOf(this.amount)
            )

            return this@TradingRecordTestContext
        }
    }

    private fun getSimulatedTime(): Instant {
        return Instant.MIN.plus(this.resolution.duration.multipliedBy((this.simulationTime++).toLong()))
    }
}
