/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api

import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.bool.fractal.FractalHighIndicator
import org.ta4j.core.indicators.bool.trend.DownTrendIndicator
import org.ta4j.core.indicators.bool.trend.UpTrendIndicator
import org.ta4j.core.indicators.numeric.swing.ZigZagIndicator
import org.ta4j.core.indicators.bool.fractal.FractalLowIndicator
import org.ta4j.core.indicators.bool.fractal.RecentFractalSwingIndicator
import org.ta4j.core.indicators.numeric.oscillators.MACDVIndicator
import org.ta4j.core.indicators.numeric.volatility.CompressionIndicator
import org.ta4j.core.indicators.numeric.volatility.SqueezeProIndicator
import org.ta4j.core.indicators.numeric.oscillators.VolatilityNormalizedMACDIndicator
import org.ta4j.core.indicators.numeric.oscillators.billwilliams.AlligatorFacade
import org.ta4j.core.indicators.numeric.oscillators.billwilliams.MarketFacilitationIndexIndicator
import java.time.Instant
import org.ta4j.core.indicators.numeric.CloseLocationValueIndicator
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.LowerShadowIndicator
import org.ta4j.core.indicators.numeric.candles.RealBodyIndicator
import org.ta4j.core.indicators.numeric.candles.UpperShadowIndicator
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
import org.ta4j.core.indicators.numeric.oscillators.AwesomeOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscillators.CCIIndicator
import org.ta4j.core.indicators.numeric.oscillators.ChopIndicator
import org.ta4j.core.indicators.numeric.oscillators.FisherIndicator
import org.ta4j.core.indicators.numeric.oscillators.PVOIndicator
import org.ta4j.core.indicators.numeric.oscillators.StochasticOscillatorDIndicator
import org.ta4j.core.indicators.numeric.oscillators.StochasticOscillatorKIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonDownIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonUpIndicator
import org.ta4j.core.indicators.numeric.supertrend.SuperTrendIndicator
import org.ta4j.core.indicators.numeric.trend.ParabolicSARIndicator
import org.ta4j.core.indicators.numeric.volume.AccumulationDistributionIndicator
import org.ta4j.core.indicators.numeric.volume.ChaikinMoneyFlowIndicator
import org.ta4j.core.indicators.numeric.volume.ChaikinOscillatorIndicator
import org.ta4j.core.indicators.bool.candles.CandlePatterns
import org.ta4j.core.indicators.numeric.oscillators.UltimateOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscillators.vortex.VortexFacade
import org.ta4j.core.indicators.numeric.volume.ForceIndexIndicator
import org.ta4j.core.indicators.numeric.volume.IIIIndicator
import org.ta4j.core.indicators.numeric.volume.AnchoredVWAPIndicator
import org.ta4j.core.indicators.numeric.volume.EaseOfMovementIndicator
import org.ta4j.core.indicators.numeric.volume.KlingerVolumeForceIndicator
import org.ta4j.core.indicators.numeric.volume.KlingerVolumeOscillatorIndicator
import org.ta4j.core.indicators.numeric.volume.MVWAPIndicator
import org.ta4j.core.indicators.numeric.volume.MoneyFlowIndexIndicator
import org.ta4j.core.indicators.numeric.volume.NVIIndicator
import org.ta4j.core.indicators.numeric.volume.OnBalanceVolumeIndicator
import org.ta4j.core.indicators.numeric.volume.PVIIndicator
import org.ta4j.core.indicators.numeric.volume.ROCVIndicator
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
    fun macdV(shortBarCount: Int = 12, longBarCount: Int = 26) = extended().macdV(shortBarCount, longBarCount)

    @JvmStatic
    fun volatilityNormalizedMacd(fastBarCount: Int = 12, slowBarCount: Int = 26, atrBarCount: Int = 26) =
        extended().volatilityNormalizedMacd(fastBarCount, slowBarCount, atrBarCount)

    @JvmStatic
    fun accDist() = extended().accDist()

    @JvmStatic
    fun mfi(barCount: Int) = extended().mfi(barCount)

    @JvmStatic
    fun nvi(startingValue: Int) = extended().nvi(startingValue)

    @JvmStatic
    fun pvi() = extended().pvi()

    @JvmStatic
    fun cci(barCount: Int) = extended().cci(barCount)

    @JvmStatic
    fun pvo(
        volumeBarCount: Int,
        shortBarCount: Int = 12,
        longBarCount: Int = 26,
    ) = extended().pvo(volumeBarCount, shortBarCount, longBarCount)

    @JvmStatic
    fun rocv(barCount: Int) = extended().rocv(barCount)

    @JvmStatic
    fun superTrend(barCount: Int = 10, multiplier: Double = 3.0) = extended().superTrend(barCount, multiplier)

    @JvmStatic
    fun parabolicSAR(
        accelerationStart: Number = 0.02,
        maxAcceleration: Number = 0.2,
        accelerationIncrement: Number = 0.02,
    ) = extended().parabolicSAR(accelerationStart, maxAcceleration, accelerationIncrement)

    @JvmStatic
    fun chop(timeFrame: Int, scaleTo: Int = 100) = extended().chop(timeFrame, scaleTo)

    @JvmStatic
    fun fisher(barCount: Int) = extended().fisher(barCount)

    @JvmStatic
    fun vwap(barCount: Int) = extended().vwap(barCount)

    @JvmStatic
    fun anchoredVwap(anchorTime: Instant) = extended().anchoredVwap(anchorTime)

    @JvmStatic
    fun anchoredVwap(anchorSignal: BooleanIndicator) = extended().anchoredVwap(anchorSignal)

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
    fun upperShadow() = extended().upperShadow()

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

    /** Candlestick patterns: hammer, morning star, kickers, ... */
    @JvmStatic
    val candlePatterns = CandlePatterns

    /** Ultimate Oscillator (Larry Williams). */
    fun ultimateOscillator(shortPeriod: Int = 7, middlePeriod: Int = 14, longPeriod: Int = 28) =
        extended().ultimateOscillator(shortPeriod, middlePeriod, longPeriod)

    /** Vortex indicator: +VI, -VI and their difference. */
    fun vortex(barCount: Int = 14) = extended().vortex(barCount)

    @JvmStatic
    fun alligator() = extended().alligator()

    @JvmStatic
    fun zigZag(reversalAmount: Number) = extended().zigZag(reversalAmount)

    @JvmStatic
    fun zigZag(reversalAmount: NumericIndicator) = extended().zigZag(reversalAmount)

    @JvmStatic
    fun upTrend(barCount: Int = 5, strengthThreshold: Number = 25) = extended().upTrend(barCount, strengthThreshold)

    @JvmStatic
    fun downTrend(barCount: Int = 5, strengthThreshold: Number = 25) = extended().downTrend(barCount, strengthThreshold)

    @JvmStatic
    fun squeezePro(barCount: Int = 20) = extended().squeezePro(barCount)

    @JvmStatic
    fun compression(barCount: Int = 20, percentileBarCount: Int = barCount) =
        extended().compression(barCount, percentileBarCount)

    @JvmStatic
    fun marketFacilitationIndex() = extended().marketFacilitationIndex()

    @JvmStatic
    fun fractalHigh(precedingBars: Int = 2, followingBars: Int = 2) = extended().fractalHigh(precedingBars, followingBars)

    @JvmStatic
    fun fractalLow(precedingBars: Int = 2, followingBars: Int = 2) = extended().fractalLow(precedingBars, followingBars)

    @JvmStatic
    fun recentSwingHigh(precedingBars: Int = 2, followingBars: Int = 2) =
        extended().recentSwingHigh(precedingBars, followingBars)

    @JvmStatic
    fun recentSwingLow(precedingBars: Int = 2, followingBars: Int = 2) =
        extended().recentSwingLow(precedingBars, followingBars)

    /** Elder's Force Index of close price and volume. */
    fun forceIndex(barCount: Int = 13) = extended().forceIndex(barCount)

    @JvmStatic
    fun easeOfMovement(barCount: Int = 14, volumeDivisor: Number = 100_000_000L) =
        extended().easeOfMovement(barCount, volumeDivisor)

    @JvmStatic
    fun klingerVolumeOscillator(shortPeriod: Int = 34, longPeriod: Int = 55) =
        extended().klingerVolumeOscillator(shortPeriod, longPeriod)

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

        fun pvi() = PVIIndicator(numFactory)

        fun cci(barCount: Int) = CCIIndicator(numFactory, barCount)

        fun pvo(
            volumeBarCount: Int,
            shortBarCount: Int = 12,
            longBarCount: Int = 26,
        ) = PVOIndicator(numFactory, volumeBarCount, shortBarCount, longBarCount)

        fun rocv(barCount: Int) = ROCVIndicator(numFactory, barCount)

        fun superTrend(barCount: Int = 10, multiplier: Double = 3.0) =
            SuperTrendIndicator(numFactory, barCount, multiplier)

        fun parabolicSAR(
            accelerationStart: Number = 0.02,
            maxAcceleration: Number = 0.2,
            accelerationIncrement: Number = 0.02,
        ) = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(accelerationStart),
            numFactory.numOf(maxAcceleration),
            numFactory.numOf(accelerationIncrement)
        )

        fun chop(timeFrame: Int, scaleTo: Int = 100) = ChopIndicator(numFactory, timeFrame, scaleTo)

        fun fisher(barCount: Int = 10) = FisherIndicator(numFactory, barCount)

        fun vwap(barCount: Int) = VWAPIndicator(numFactory, barCount)

        /** VWAP of the typical price accumulated from the first bar beginning at or after [anchorTime]. */
        fun anchoredVwap(anchorTime: Instant) = AnchoredVWAPIndicator(typicalPrice(), volume(), anchorTime)

        /** VWAP of the typical price restarted on every bar where [anchorSignal] is true. */
        fun anchoredVwap(anchorSignal: BooleanIndicator) = AnchoredVWAPIndicator(typicalPrice(), volume(), anchorSignal)

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

        fun upperShadow() = UpperShadowIndicator(numFactory)

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
            closePrice().macd(shortBarCount, longBarCount)

        fun chaikinMoneyFlow(barCount: Int) = ChaikinMoneyFlowIndicator(numFactory, barCount)


        fun chaikinOscillator(shortBarCount: Int = 3, longBarCount: Int = 10) =
            ChaikinOscillatorIndicator(numFactory, shortBarCount, longBarCount)

        fun iii() = IIIIndicator(numFactory)

        fun forceIndex(barCount: Int = 13) = ForceIndexIndicator(closePrice(), volume(), barCount)

        fun easeOfMovement(barCount: Int = 14, volumeDivisor: Number = 100_000_000L) =
            EaseOfMovementIndicator(highPrice(), lowPrice(), volume(), barCount, volumeDivisor)

        fun klingerVolumeForce(scaleMultiplier: Number = 100) =
            KlingerVolumeForceIndicator(highPrice(), lowPrice(), closePrice(), volume(), scaleMultiplier)

        fun klingerVolumeOscillator(shortPeriod: Int = 34, longPeriod: Int = 55) =
            KlingerVolumeOscillatorIndicator(klingerVolumeForce(), shortPeriod, longPeriod)

        fun ultimateOscillator(shortPeriod: Int = 7, middlePeriod: Int = 14, longPeriod: Int = 28) =
            UltimateOscillatorIndicator(numFactory, shortPeriod, middlePeriod, longPeriod)

        fun vortex(barCount: Int = 14) = VortexFacade(numFactory, barCount)

        /** Close-price MACD of variable-weighted EMAs with weight `volume / ATR`. */
        fun macdV(shortBarCount: Int = 12, longBarCount: Int = 26) =
            MACDVIndicator(closePrice(), volume(), shortBarCount, longBarCount)

        /** Close-price MACD divided by ATR and scaled by 100 (Spiroglou's MACD-V). */
        fun volatilityNormalizedMacd(fastBarCount: Int = 12, slowBarCount: Int = 26, atrBarCount: Int = 26) =
            VolatilityNormalizedMACDIndicator(closePrice(), atr(atrBarCount), fastBarCount, slowBarCount)

        fun squeezePro(
            barCount: Int = 20,
            bollingerK: Number = 2,
            keltnerHigh: Number = 1,
            keltnerMid: Number = 1.5,
            keltnerLow: Number = 2,
        ) = SqueezeProIndicator(numFactory, barCount, bollingerK, keltnerHigh, keltnerMid, keltnerLow)

        /** Compression of ATR %, Bollinger bandwidth and Donchian width % over [barCount] bars. */
        fun compression(barCount: Int = 20, percentileBarCount: Int = barCount): CompressionIndicator {
            val close = closePrice()
            val donchian = donchianChannel(barCount)
            return CompressionIndicator(
                atrPercent = atr(barCount).dividedBy(close).multipliedBy(100),
                bollingerWidth = BollingerBandFacade(close, barCount, 2).bandwidth,
                donchianWidth = donchian.upper.minus(donchian.lower).dividedBy(donchian.middle).multipliedBy(100),
                percentileBarCount = percentileBarCount,
            )
        }

        /** ZigZag on high/low confirmed by close, ignoring moves smaller than [reversalAmount] price units. */
        fun zigZag(reversalAmount: Number) = zigZag(ConstantNumericIndicator(numFactory.numOf(reversalAmount)))

        /** ZigZag with an adaptive reversal amount, e.g. `atr(14).multipliedBy(2)`. */
        fun zigZag(reversalAmount: NumericIndicator) =
            ZigZagIndicator(highPrice(), lowPrice(), closePrice(), reversalAmount)

        fun upTrend(barCount: Int = 5, strengthThreshold: Number = 25) =
            UpTrendIndicator(numFactory, barCount, strengthThreshold)

        fun downTrend(barCount: Int = 5, strengthThreshold: Number = 25) =
            DownTrendIndicator(numFactory, barCount, strengthThreshold)

        /** Bill Williams Alligator on the median price with the classic 13/8, 8/5, 5/3 settings. */
        fun alligator() = AlligatorFacade(medianPrice())

        fun marketFacilitationIndex() = MarketFacilitationIndexIndicator(highPrice(), lowPrice(), volume())

        fun fractalHigh(precedingBars: Int = 2, followingBars: Int = 2) =
            FractalHighIndicator(highPrice(), precedingBars, followingBars)

        fun fractalLow(precedingBars: Int = 2, followingBars: Int = 2) =
            FractalLowIndicator(lowPrice(), precedingBars, followingBars)

        /** High of the most recently confirmed fractal high. */
        fun recentSwingHigh(precedingBars: Int = 2, followingBars: Int = 2) =
            RecentFractalSwingIndicator(numFactory, fractalHigh(precedingBars, followingBars))

        /** Low of the most recently confirmed fractal low. */
        fun recentSwingLow(precedingBars: Int = 2, followingBars: Int = 2) =
            RecentFractalSwingIndicator(numFactory, fractalLow(precedingBars, followingBars))
    }
}
