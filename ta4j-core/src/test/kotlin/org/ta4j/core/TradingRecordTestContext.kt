package org.ta4j.core

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.backtest.BacktestBarSeries
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider
import org.ta4j.core.strategy.RuntimeContext

class TradingRecordTestContext(
    private var marketEvenTestContext: MarketEventTestContext? = null,
) {
    private var simulationTime: Int = 0
    private var tradeType: TradeType = TradeType.BUY
    lateinit var tradingRecord: BackTestTradingRecord
        private set
    private var criterion: AnalysisCriterion? = null
    private var transactionCostModel: CostModel = ZeroCostModel
    private var holdingCostModel: CostModel = ZeroCostModel
    private var numFactory: NumFactory = NumFactoryProvider.defaultNumFactory
    private var resolution: ChronoUnit = ChronoUnit.DAYS

    init {
        marketEvenTestContext?.let {
            withNumFactory(it.barSeries.numFactory)
        }
        reinitalizeTradingRecord()
    }

    fun withTradeType(tradeType: TradeType): TradingRecordTestContext = apply {
        this.tradeType = tradeType
        reinitalizeTradingRecord()
    }

    fun withNumFactory(numFactory: NumFactory): TradingRecordTestContext = apply {
        this.numFactory = numFactory
        NumFactoryProvider.defaultNumFactory = numFactory
        reinitalizeTradingRecord()
    }

    private fun reinitalizeTradingRecord() {
        tradingRecord = BackTestTradingRecord(
            tradeType,
            "",
            transactionCostModel,
            holdingCostModel,
            numFactory
        )

        marketEvenTestContext?.withBarListener(tradingRecord)
    }

    fun withCriterion(criterion: AnalysisCriterion): TradingRecordTestContext = apply {
        this.criterion = criterion
    }

    fun withMarketDataDependentCriterion(criterionFactory: (List<MarketEvent>) -> AnalysisCriterion): TradingRecordTestContext =
        apply {
            criterion = criterionFactory(requireNotNull(marketEvenTestContext).marketEvents.toList())
        }

    fun withTransactionCostModel(transactionCostModel: CostModel): TradingRecordTestContext = apply {
        this.transactionCostModel = transactionCostModel
        reinitalizeTradingRecord()
    }

    fun withHoldingCostModel(holdingCostModel: CostModel): TradingRecordTestContext = apply {
        this.holdingCostModel = holdingCostModel
        reinitalizeTradingRecord()
    }

    fun getTradingRecord(): TradingRecord = this.tradingRecord

    val runtimeContext: RuntimeContext
        get() = this.tradingRecord

    fun assertResults(expected: Double) {
        requireNotNull(criterion) { "Criterion must be set before asserting results" }
        assertNumEquals(expected, criterion!!.calculate(this.tradingRecord))
    }

    fun enter(amount: Double): Operation = EnterOperation(amount)

    fun exit(amount: Double): Operation = ExitOperation(amount)

    private fun getNumFactory(): NumFactory = numFactory

    val barSeries: BacktestBarSeries
        get() = requireNotNull(marketEvenTestContext) { "MarketEventTestContext must be set to access barSeries" }.barSeries

    fun forwardTime(countOfBars: Int): TradingRecordTestContext = apply {
        marketEvenTestContext?.fastForward(countOfBars) ?: run {
            simulationTime += countOfBars
        }
    }

    fun fastForwardToTheEnd() = apply {
        requireNotNull(marketEvenTestContext) { "MarketEventTestContext must be set for fast forward operations" }.fastForwardToTheEnd()
    }

    fun withResolution(resolution: ChronoUnit): TradingRecordTestContext = apply {
        this.resolution = resolution
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
            requireNotNull(marketEvenTestContext) { "MarketEventTestContext must be set for time-based operations" }
            marketEvenTestContext!!.fastForward(countOfBars)

            val bar = marketEvenTestContext!!.barSeries.bar
            tradingRecord.enter(
                bar.endTime,
                bar.closePrice,
                getNumFactory().numOf(amount)
            )

            // add bar to positions history
            tradingRecord.onBar(bar)

            return this@TradingRecordTestContext
        }

        override fun at(price: Double): TradingRecordTestContext {
            tradingRecord.enter(
                getSimulatedTime(),
                getNumFactory().numOf(price),
                getNumFactory().numOf(amount)
            )
            return this@TradingRecordTestContext
        }
    }

    inner class ExitOperation(private val amount: Double) : Operation {

        override fun after(countOfBars: Int): TradingRecordTestContext {
            requireNotNull(marketEvenTestContext) { "MarketEventTestContext must be set for time-based operations" }
            marketEvenTestContext!!.fastForward(countOfBars)

            val bar = marketEvenTestContext!!.barSeries.bar
            tradingRecord.exit(
                bar.endTime,
                bar.closePrice,
                getNumFactory().numOf(amount)
            )

            // add bar to positions history
            tradingRecord.onBar(bar)

            return this@TradingRecordTestContext
        }

        override fun at(price: Double): TradingRecordTestContext {
            tradingRecord.exit(
                getSimulatedTime(),
                getNumFactory().numOf(price),
                getNumFactory().numOf(amount)
            )
            return this@TradingRecordTestContext
        }
    }

    private fun getSimulatedTime(): Instant =
        Instant.MIN.plus(resolution.duration.multipliedBy((simulationTime++).toLong()))
}
