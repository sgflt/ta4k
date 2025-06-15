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
    private var candleDuration: Duration = TimeFrameMapping.getDuration(TimeFrame.DAY)
    private var startTime: Instant = Instant.EPOCH


    /**
     * Sets OHLC to the same value.
     *
     * @param prices to set for each candle
     *
     * @return this
     */
    fun withCandlePrices(vararg prices: Double): MarketEventTestContext = apply {
        marketEvents = LinkedList(
            MockMarketEventBuilder()
                .withStartTime(startTime)
                .withCandleDuration(candleDuration)
                .withCandlePrices(*prices)
                .build()
        )
    }

    fun withMarketEvents(marketEvents: List<MarketEvent>): MarketEventTestContext = apply {
        this.marketEvents = LinkedList(marketEvents)
    }


    fun withDefaultMarketEvents(): MarketEventTestContext = apply {
        marketEvents = LinkedList(MockMarketEventBuilder().withDefaultData().build())
    }


    fun withIndicator(indicator: Indicator<*>): MarketEventTestContext = apply {
        indicatorContext.add(indicator)
    }


    fun withIndicator(indicator: Indicator<*>, name: String): MarketEventTestContext = apply {
        indicatorContext.add(indicator, IndicatorIdentification(name))
    }


    fun withIndicators(vararg indicator: Indicator<*>): MarketEventTestContext = apply {
        indicator.forEach { withIndicator(it) }
    }


    /**
     * This method should be called before creation of any indicator to preserve precision.
     *
     * @param factory that wil build [Num]
     *
     * @return this
     */
    fun withNumFactory(factory: NumFactory): MarketEventTestContext = apply {
        barSeries =
            BacktestBarSeriesBuilder()
                .withNumFactory(factory)
                .withTimeFrame(barSeries.timeFrame)
                .withIndicatorContext(indicatorContext)
                .build()
    }

    /**
     * Sets a custom barSeries for this context.
     * This is useful when you need a specific timeframe that differs from the default DAY timeframe.
     */
    fun withBarSeries(customBarSeries: BacktestBarSeries): MarketEventTestContext = apply {
        barSeries = customBarSeries
    }
    


    fun advance(): Boolean {
        log.trace("\t ### advance")

        val marketEvent = marketEvents.poll()
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


    fun fastForwardUntilStable(): MarketEventTestContext = apply {
        while (indicatorContext.isNotEmpty && !indicatorContext.isStable) {
            fastForward(1)
        }
    }


    fun assertNextNaN(): MarketEventTestContext = apply {
        fastForward(1)
        assertNumEquals(NaN, firstNumericIndicator!!.value)
    }

    fun assertCurrentNaN(): MarketEventTestContext = apply {
        assertNumEquals(NaN, firstNumericIndicator!!.value)
    }


    fun assertNextFalse(): MarketEventTestContext = apply {
        fastForward(1)
        assertThat(fisrtBooleanIndicator!!.value).isFalse()
    }


    fun assertNextTrue(): MarketEventTestContext = apply {
        fastForward(1)
        assertThat(fisrtBooleanIndicator!!.value).isTrue()
    }


    /**
     * @param bars how many bars should be skipped.
     *
     * @return this
     */
    fun fastForward(bars: Int): MarketEventTestContext = apply {
        log.debug("Fast forward =====> {}", bars)
        for (i in 0..<bars) {
            check(advance()) { "Fast forward failed at index " + i }
        }
    }


    fun assertCurrent(expected: Double): MarketEventTestContext = apply {
        assertCurrent(firstNumericIndicator!!, expected)
    }


    private fun assertCurrent(indicator: NumericIndicator, expected: Double): MarketEventTestContext = apply {
        assertNumEquals(expected, indicator.value)
    }


    private fun getNumericIndicator(indicatorName: IndicatorIdentification): NumericIndicator? {
        return indicatorContext.getNumericIndicator(indicatorName)
    }


    fun assertNext(expected: Double): MarketEventTestContext = apply {
        assertNext(firstNumericIndicator!!, expected)
    }


    private fun assertNext(indicator: NumericIndicator, expected: Double): MarketEventTestContext = apply {
        check(advance()) { "Next failed" }

        assertNumEquals(expected, indicator.value)
    }


    fun assertIndicatorEquals(
        expectedIndicator: Indicator<Num>,
        indicator: Indicator<Num>,
    ): MarketEventTestContext = apply {
        while (advance()) {
            assertThat(indicator.value.doubleValue())
                .describedAs { "Failed at index ${barSeries.currentIndex} and time ${barSeries.currentTime}" }
                .isCloseTo(
                    expectedIndicator.value.doubleValue(),
                    Offset.offset(GENERAL_OFFSET)
                )
        }
    }


    fun onIndicator(name: String): IndicatorAsserts = IndicatorAsserts(name)


    /**
     * Adaptation of market events to executed position testing.
     *
     * There is some magic due to different clocks. This method adjusts clock to match with market events.
     *
     * Some criteria are not event based, but are retrospective instead. So for them, we have to replay all events
     * and calculate them on historical data.
     */
    fun toTradingRecordContext(): TradingRecordTestContext = TradingRecordTestContext(this)


    fun withBarListener(barListener: BarListener) {
        barSeries.clearListeners()
        barSeries.addBarListener(barListener)
    }


    fun withCandleDuration(candleDuration: ChronoUnit): MarketEventTestContext = apply {
        this.candleDuration = candleDuration.duration
    }


    fun withStartTime(startTime: Instant): MarketEventTestContext = apply {
        this.startTime = startTime
    }


    fun assertIsStable() {
        val indicator = getAnyIndicator()
        assertThat(indicator?.isStable).isTrue()
    }

    fun assertIsUnStable() {
        val indicator = getAnyIndicator()
        assertThat(indicator?.isStable).isFalse()
    }

    private fun getAnyIndicator() = firstNumericIndicator ?: fisrtBooleanIndicator


    inner class IndicatorAsserts(indicatorName: String) {
        private val indicatorId: IndicatorIdentification = IndicatorIdentification(indicatorName)


        init {
            check(indicatorId in this@MarketEventTestContext.indicatorContext) { "Indicator $indicatorName not found" }
        }


        fun assertNext(expected: Double): IndicatorAsserts = apply {
            this@MarketEventTestContext.assertNext(getNumericIndicator(indicatorId)!!, expected)
        }


        fun assertCurrent(expected: Double): IndicatorAsserts = apply {
            this@MarketEventTestContext.assertCurrent(getNumericIndicator(indicatorId)!!, expected)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MarketEventTestContext::class.java)
    }
}
