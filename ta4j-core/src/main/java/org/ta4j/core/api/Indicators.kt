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
package org.ta4j.core.api

import org.ta4j.core.indicators.numeric.CloseLocationValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.LowerShadowIndicator
import org.ta4j.core.indicators.numeric.candles.RealBodyIndicator
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.MedianPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.OpenPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.TypicalPriceIndicator
import org.ta4j.core.indicators.numeric.channels.bollinger.BollingerBandFacade
import org.ta4j.core.indicators.numeric.channels.donchian.DonchianChannelFacade
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.indicators.numeric.momentum.MassIndexIndicator
import org.ta4j.core.indicators.numeric.momentum.RWIHighIndicator
import org.ta4j.core.indicators.numeric.momentum.RWILowIndicator
import org.ta4j.core.indicators.numeric.momentum.WilliamsRIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.ADXIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDIIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDMIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.PlusDIIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.PlusDMIndicator
import org.ta4j.core.indicators.numeric.oscilators.AwesomeOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscilators.PVOIndicator
import org.ta4j.core.indicators.numeric.oscilators.StochasticOscillatorDIndicator
import org.ta4j.core.indicators.numeric.oscilators.StochasticOscillatorKIndicator
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonDownIndicator
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonUpIndicator
import org.ta4j.core.indicators.numeric.supertrend.SuperTrendIndicator
import org.ta4j.core.indicators.numeric.volume.AccumulationDistributionIndicator
import org.ta4j.core.indicators.numeric.volume.ChaikinMoneyFlowIndicator
import org.ta4j.core.indicators.numeric.volume.ChaikinOscillatorIndicator
import org.ta4j.core.indicators.numeric.volume.IIIIndicator
import org.ta4j.core.indicators.numeric.volume.MVWAPIndicator
import org.ta4j.core.indicators.numeric.volume.MoneyFlowIndexIndicator
import org.ta4j.core.indicators.numeric.volume.NVIIndicator
import org.ta4j.core.indicators.numeric.volume.OnBalanceVolumeIndicator
import org.ta4j.core.indicators.numeric.volume.VWAPIndicator
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

/**
 * @author Lukáš Kvídera
 */
object Indicators {
    @JvmStatic
    fun medianPrice() = extended().medianPrice()

    @JvmStatic
    fun closePrice() = extended().closePrice()

    @JvmStatic
    fun openPrice() = extended().openPrice()

    @JvmStatic
    fun lowPrice() = extended().lowPrice()

    @JvmStatic
    fun highPrice() = extended().highPrice()

    @JvmStatic
    fun typicalPrice() = extended().typicalPrice()

    @JvmStatic
    fun closeLocationValue() = extended().closeLocationValue()

    @JvmStatic
    fun volume() = extended().volume()

    @JvmStatic
    fun obv() = extended().obv()

    @JvmStatic
    fun williamsR(barCount: Int) = extended().williamsR(barCount)

    @JvmStatic
    fun massIndex(emaBarCount: Int = 9, barCount: Int = 25) = extended().massIndex(emaBarCount, barCount)

    @JvmStatic
    fun macd(shortBarCount: Int = 12, longBarCount: Int = 26) = extended().macd(shortBarCount, longBarCount)

    @JvmStatic
    fun accDist() = extended().accDist()

    @JvmStatic
    fun mfi(barCount: Int) = extended().mfi(barCount)

    @JvmStatic
    fun nvi(startingValue: Int) = extended().nvi(startingValue)

    @JvmStatic
    fun pvo(
        volumeBarCount: Int,
        shortBarCount: Int = 12,
        longBarCount: Int = 26,
    ) = extended().pvo(volumeBarCount, shortBarCount, longBarCount)

    @JvmStatic
    fun superTrend(barCount: Int = 10, multiplier: Double = 3.0) = extended().superTrend(barCount, multiplier)

    @JvmStatic
    fun vwap(barCount: Int) = extended().vwap(barCount)

    @JvmStatic
    fun mvwap(vwapBarCount: Int, mvwapBarCount: Int) = extended().mvwap(vwapBarCount, mvwapBarCount)

    @JvmStatic
    fun bollingerBands(barCount: Int, k: Number) = extended().bollingerBands(barCount, k)

    @JvmStatic
    fun donchianChannel(barCount: Int) = extended().donchianChannel(barCount)

    @JvmStatic
    fun realBody() = extended().realBody()

    @JvmStatic
    fun aroonOscillator(barCount: Int) = extended().aroonOscillator(barCount)

    @JvmStatic
    fun aroonUp(barCount: Int) = extended().aroonUp(barCount)

    @JvmStatic
    fun aroonDown(barCount: Int) = extended().aroonDown(barCount)

    @JvmStatic
    fun atr(barCount: Int) = extended().atr(barCount)

    @JvmStatic
    fun adx(diBarCount: Int, adxBarCount: Int) = extended().adx(diBarCount, adxBarCount)

    @JvmStatic
    fun adx(barCount: Int) = adx(barCount, barCount)

    @JvmStatic
    fun plusDMI() = extended().plusDMI()

    @JvmStatic
    fun plusDII(barCount: Int) = extended().plusDII(barCount)

    @JvmStatic
    fun minusDMI() = extended().minusDMI()

    @JvmStatic
    fun minusDII(barCount: Int) = extended().minusDII(barCount)

    @JvmStatic
    fun lowerShadow() = extended().lowerShadow()

    @JvmStatic
    fun stochasticKOscillator(barCount: Int) = extended().stochasticKOscillator(barCount)

    @JvmStatic
    fun stochasticOscillator(barCount: Int) = extended().stochasticOscillator(barCount)

    @JvmStatic
    fun awesomeOscillator(shortBarCount: Int, longBarCount: Int) =
        extended().awesomeOscillator(shortBarCount, longBarCount)

    @JvmStatic
    fun rwiHigh(barCount: Int) = extended().rwiHigh(barCount)

    @JvmStatic
    fun rwiLow(barCount: Int) = extended().rwiLow(barCount)

    @JvmStatic
    fun chainkinMoneyFlow(barCount: Int) = extended().chaikinMoneyFlow(barCount)

    @JvmStatic
    fun chainkinMoneyFlow(shortBarCount: Int = 3, longBarCount: Int = 10) =
        extended().chaikinOscillator(shortBarCount, longBarCount)

    @JvmStatic
    fun iii() = extended().iii()

    @JvmStatic
    private fun extended() = extended(NumFactoryProvider.defaultNumFactory)

    @JvmStatic
    fun extended(numFactory: NumFactory) = ExtendedIndicatorFactory(numFactory)

    class ExtendedIndicatorFactory(private val numFactory: NumFactory) {
        fun medianPrice() = MedianPriceIndicator(numFactory)

        fun closePrice() = ClosePriceIndicator(numFactory)

        fun openPrice() = OpenPriceIndicator(numFactory)

        fun lowPrice() = LowPriceIndicator(numFactory)

        fun highPrice() = HighPriceIndicator(numFactory)

        fun typicalPrice() = TypicalPriceIndicator(numFactory)

        fun volume() = VolumeIndicator(numFactory)

        fun obv() = OnBalanceVolumeIndicator(numFactory)

        fun williamsR(barCount: Int) = WilliamsRIndicator(numFactory, barCount)

        fun massIndex(emaBarCount: Int = 9, barCount: Int = 25) = MassIndexIndicator(numFactory, emaBarCount, barCount)

        fun accDist() = AccumulationDistributionIndicator(numFactory)

        fun mfi(barCount: Int) = MoneyFlowIndexIndicator(numFactory, barCount)

        fun nvi(startingValue: Int) = NVIIndicator(numFactory, numFactory.numOf(startingValue))

        fun pvo(
            volumeBarCount: Int,
            shortBarCount: Int = 12,
            longBarCount: Int = 26,
        ) = PVOIndicator(numFactory, volumeBarCount, shortBarCount, longBarCount)

        fun superTrend(barCount: Int = 10, multiplier: Double = 3.0) =
            SuperTrendIndicator(numFactory, barCount, multiplier)

        fun vwap(barCount: Int) = VWAPIndicator(numFactory, barCount)

        fun mvwap(vwapBarCount: Int, mvwapBarCount: Int) = MVWAPIndicator(numFactory, vwapBarCount, mvwapBarCount)

        fun bollingerBands(barCount: Int, k: Number) = BollingerBandFacade(closePrice(), barCount, k)

        fun donchianChannel(barCount: Int) = DonchianChannelFacade(barCount, numFactory)

        fun realBody() = RealBodyIndicator(numFactory)

        fun aroonOscillator(barCount: Int) = AroonOscillatorIndicator(numFactory, barCount)

        fun aroonUp(barCount: Int) = AroonUpIndicator(numFactory, barCount = barCount)

        fun aroonDown(barCount: Int) = AroonDownIndicator(numFactory, barCount)

        fun atr(barCount: Int) = ATRIndicator(numFactory, barCount = barCount)

        fun adx(diBarCount: Int, adxBarCount: Int) = ADXIndicator(numFactory, diBarCount, adxBarCount)

        fun adx(barCount: Int) = adx(barCount, barCount)

        fun plusDMI() = PlusDMIndicator(numFactory)

        fun plusDII(barCount: Int) = PlusDIIndicator(numFactory, barCount)

        fun minusDMI() = MinusDMIndicator(numFactory)

        fun minusDII(barCount: Int) = MinusDIIndicator(numFactory, barCount)

        fun lowerShadow() = LowerShadowIndicator(numFactory)

        fun stochasticOscillator(barCount: Int) =
            StochasticOscillatorDIndicator(numFactory, stochasticKOscillator(barCount))

        fun stochasticKOscillator(barCount: Int) = StochasticOscillatorKIndicator(numFactory, barCount)

        fun awesomeOscillator(
            indicator: NumericIndicator,
            shortBarCount: Int,
            longBarCount: Int,
        ) = AwesomeOscillatorIndicator(numFactory, indicator, shortBarCount, longBarCount)

        fun awesomeOscillator(shortBarCount: Int, longBarCount: Int) =
            awesomeOscillator(medianPrice(), shortBarCount, longBarCount)

        fun closeLocationValue() = CloseLocationValueIndicator(numFactory)

        fun rwiHigh(barCount: Int) = RWIHighIndicator(numFactory, barCount)

        fun rwiLow(barCount: Int) = RWILowIndicator(numFactory, barCount)

        fun macd(shortBarCount: Int = 12, longBarCount: Int = 26) =
            Indicators.closePrice().macd(shortBarCount, longBarCount)

        fun chaikinMoneyFlow(barCount: Int) = ChaikinMoneyFlowIndicator(numFactory, barCount)


        fun chaikinOscillator(shortBarCount: Int = 3, longBarCount: Int = 10) =
            ChaikinOscillatorIndicator(numFactory, shortBarCount, longBarCount)

        fun iii() = IIIIndicator(numFactory)
    }
}
