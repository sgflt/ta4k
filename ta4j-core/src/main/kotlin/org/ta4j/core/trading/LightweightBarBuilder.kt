/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading

import java.time.Instant
import org.ta4j.core.api.series.BarBuilder
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * A builder to build a new `LiveBar` with conversion from a
 * [Number] of type `T` to a [Num implementation][Num].
 */
internal class LightweightBarBuilder(private val series: BarSeries) : BarBuilder {
    private val numFactory: NumFactory = series.numFactory
    private var startTime: Instant = Instant.EPOCH
    private var endTime: Instant = Instant.EPOCH
    private var openPrice: Num = NaN
    private var highPrice: Num = NaN
    private var lowPrice: Num = NaN
    private var closePrice: Num = NaN
    private var volume: Num = NaN


    override fun startTime(startTime: Instant): LightweightBarBuilder {
        this.startTime = startTime
        return this
    }


    override fun endTime(endTime: Instant): LightweightBarBuilder {
        this.endTime = endTime
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    override fun openPrice(openPrice: Number): LightweightBarBuilder {
        this.openPrice = this.numFactory.numOf(openPrice)
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    fun openPrice(openPrice: String): LightweightBarBuilder {
        this.openPrice = this.numFactory.numOf(openPrice)
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    override fun highPrice(highPrice: Number): LightweightBarBuilder {
        this.highPrice = this.numFactory.numOf(highPrice)
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    fun highPrice(highPrice: String): LightweightBarBuilder {
        this.highPrice = this.numFactory.numOf(highPrice)
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    override fun lowPrice(lowPrice: Number): LightweightBarBuilder {
        this.lowPrice = this.numFactory.numOf(lowPrice)
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    fun lowPrice(lowPrice: String): LightweightBarBuilder {
        this.lowPrice = this.numFactory.numOf(lowPrice)
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    override fun closePrice(closePrice: Number): LightweightBarBuilder {
        this.closePrice = this.numFactory.numOf(closePrice)
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    fun closePrice(closePrice: String): LightweightBarBuilder {
        this.closePrice = this.numFactory.numOf(closePrice)
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    override fun volume(volume: Number): LightweightBarBuilder {
        this.volume = this.numFactory.numOf(volume)
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    fun volume(volume: String): LightweightBarBuilder {
        this.volume = this.numFactory.numOf(volume)
        return this
    }


    fun build(): LightweightBar {
        return LightweightBar(
            series.timeFrame,
            startTime,
            endTime,
            openPrice,
            highPrice,
            lowPrice,
            closePrice,
            volume
        )
    }


    override fun add() {
        series.addBar(build())
    }
}
