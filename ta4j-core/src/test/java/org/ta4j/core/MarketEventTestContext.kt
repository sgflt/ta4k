package org.ta4j.core

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.TestUtils.GENERAL_OFFSET
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.backtest.BacktestBarSeries
import org.ta4j.core.backtest.BacktestBarSeriesBuilder
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.TimeFrameMapping

/**
 * This class simplifies testing by connecting market events and bar series through indicator context.
 *
 * Events are replayed to bar series that refreshes indicator context by created bar.
 */
class MarketEventTestContext {
    private lateinit var marketEvents: Queue<MarketEvent>
    private val indicatorContext: IndicatorContext = IndicatorContext.empty(TimeFrame.DAY)


    var barSeries: BacktestBarSeries =
        BacktestBarSeriesBuilder().withIndicatorContext(indicatorContext).build()
        private set
    private var candleDuration: Duration = TimeFrameMapping.getDuration(TimeFrame.DAY)!!
    private var startTime: Instant = Instant.EPOCH


    /**
     * Sets OHLC to the same value.
     *
     * @param prices to set for each candle
     *
     * @return this
     */
    fun withCandlePrices(vararg prices: Double): MarketEventTestContext {
        marketEvents = LinkedList(
            MockMarketEventBuilder()
                .withStartTime(startTime)
                .withCandleDuration(candleDuration)
                .withCandlePrices(*prices)
                .build()
        )
        return this
    }

    fun withMarketEvents(marketEvents: List<MarketEvent>): MarketEventTestContext {
        this.marketEvents = LinkedList(marketEvents)
        return this
    }


    fun withDefaultMarketEvents(): MarketEventTestContext {
        marketEvents = LinkedList(MockMarketEventBuilder().withDefaultData().build())
        return this
    }


    fun withIndicator(indicator: Indicator<*>): MarketEventTestContext {
        indicatorContext.add(indicator)
        return this
    }


    fun withIndicator(indicator: Indicator<*>, name: String): MarketEventTestContext {
        indicatorContext.add(indicator, IndicatorIdentification(name))
        return this
    }


    fun withIndicators(vararg indicator: Indicator<*>): MarketEventTestContext {
        indicator.forEach { withIndicator(it) }
        return this
    }


    /**
     * This method should be called before creation of any indicator to preserve precision.
     *
     * @param factory that wil build [Num]
     *
     * @return this
     */
    fun withNumFactory(factory: NumFactory): MarketEventTestContext {
        barSeries =
            BacktestBarSeriesBuilder()
                .withNumFactory(factory)
                .withTimeFrame(TimeFrame.DAY)
                .withIndicatorContext(indicatorContext)
                .build()
        return this
    }


    fun advance(): Boolean {
        log.trace("\t ### advance")

        val marketEvent = marketEvents!!.poll()
        if (marketEvent == null) {
            return false
        }

        when (marketEvent) {
            is CandleReceived -> barSeries.onCandle(marketEvent)
            else -> error("Unexpected value: $marketEvent")
        }

        return true
    }


    val firstNumericIndicator: NumericIndicator?
        get() = indicatorContext.first as NumericIndicator?


    val fisrtBooleanIndicator: BooleanIndicator?
        get() = indicatorContext.first as BooleanIndicator?


    fun fastForwardUntilStable(): MarketEventTestContext {
        while (indicatorContext.isNotEmpty && !indicatorContext.isStable) {
            fastForward(1)
        }

        return this
    }


    fun assertNextNaN(): MarketEventTestContext {
        advance()
        assertNumEquals(NaN, firstNumericIndicator!!.value)
        return this
    }


    fun assertNextFalse(): MarketEventTestContext {
        advance()
        assertThat(fisrtBooleanIndicator!!.value).isFalse()
        return this
    }


    fun assertNextTrue(): MarketEventTestContext {
        advance()
        assertThat(fisrtBooleanIndicator!!.value).isTrue()
        return this
    }


    /**
     * @param bars how many bars should be skipped.
     *
     * @return this
     */
    fun fastForward(bars: Int): MarketEventTestContext {
        log.debug("Fast forward =====> {}", bars)
        for (i in 0..<bars) {
            check(advance()) { "Fast forward failed at index " + i }
        }
        return this
    }


    fun assertCurrent(expected: Double): MarketEventTestContext {
        assertCurrent(firstNumericIndicator!!, expected)
        return this
    }


    private fun assertCurrent(indicator: NumericIndicator, expected: Double): MarketEventTestContext {
        assertNumEquals(expected, indicator.value)
        return this
    }


    private fun getNumericIndicator(indicatorName: IndicatorIdentification): NumericIndicator? {
        return indicatorContext.getNumericIndicator(indicatorName)
    }


    fun assertNext(expected: Double): MarketEventTestContext {
        assertNext(firstNumericIndicator!!, expected)
        return this
    }


    private fun assertNext(indicator: NumericIndicator, expected: Double): MarketEventTestContext {
        check(advance()) { "Next failed" }

        assertNumEquals(expected, indicator.value)
        return this
    }


    fun assertIndicatorEquals(
        expectedIndicator: Indicator<Num>,
        indicator: Indicator<Num>,
    ): MarketEventTestContext {
        while (advance()) {
            assertThat(indicator.value.doubleValue())
                .isCloseTo(
                    expectedIndicator.value.doubleValue(),
                    Offset.offset(GENERAL_OFFSET)
                )
        }

        return this
    }


    fun onIndicator(name: String): IndicatorAsserts {
        return IndicatorAsserts(name)
    }


    /**
     * Adaptation of market events to executed position testing.
     *
     * There is some magic due to different clocks. This method adjusts clock to match with market events.
     *
     * Some criteria are not event based, but are retrospective instead. So for them, we have to replay all events
     * and calculate them on historical data.
     */
    fun toTradingRecordContext(): TradingRecordTestContext {
        return TradingRecordTestContext(this)
    }


    fun withBarListener(barListener: BarListener) {
        barSeries.clearListeners()
        barSeries.addBarListener(barListener)
    }


    fun withCandleDuration(candleDuration: ChronoUnit): MarketEventTestContext {
        this.candleDuration = candleDuration.duration
        return this
    }


    fun withStartTime(startTime: Instant): MarketEventTestContext {
        this.startTime = startTime
        return this
    }


    inner class IndicatorAsserts(indicatorName: String) {
        private val indicatorId: IndicatorIdentification = IndicatorIdentification(indicatorName)


        init {
            check(indicatorId in this@MarketEventTestContext.indicatorContext) { "Indicator $indicatorName not found" }
        }


        fun assertNext(expected: Double): IndicatorAsserts {
            this@MarketEventTestContext.assertNext(getNumericIndicator(indicatorId)!!, expected)
            return this
        }


        fun assertCurrent(expected: Double): IndicatorAsserts {
            this@MarketEventTestContext.assertCurrent(getNumericIndicator(indicatorId)!!, expected)
            return this
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MarketEventTestContext::class.java)
    }
}
