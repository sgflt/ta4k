/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.IsHighestIndicator
import org.ta4j.core.indicators.bool.IsLowestIndicator
import org.ta4j.core.indicators.bool.helpers.CrossIndicator
import org.ta4j.core.indicators.helpers.DifferenceIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.average.ATMAIndicator
import org.ta4j.core.indicators.numeric.average.DoubleEMAIndicator
import org.ta4j.core.indicators.numeric.average.EMAIndicator
import org.ta4j.core.indicators.numeric.average.EWMAIndicator
import org.ta4j.core.indicators.numeric.average.McGinleyMAIndicator
import org.ta4j.core.indicators.numeric.average.VIDYAIndicator
import org.ta4j.core.indicators.numeric.average.HMAIndicator
import org.ta4j.core.indicators.numeric.average.KAMAIndicator
import org.ta4j.core.indicators.numeric.average.LWMAIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.indicators.numeric.average.PPOIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.average.TripleEMAIndicator
import org.ta4j.core.indicators.numeric.average.WMAIndicator
import org.ta4j.core.indicators.numeric.average.ZLEMAIndicator
import org.ta4j.core.indicators.numeric.helpers.DistanceFromMAIndicator
import org.ta4j.core.indicators.numeric.helpers.GainIndicator
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LossIndicator
import org.ta4j.core.indicators.numeric.helpers.LogReturnIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.RatioIndicator
import org.ta4j.core.indicators.numeric.helpers.RunningTotalIndicator
import org.ta4j.core.indicators.numeric.helpers.StreakIndicator
import org.ta4j.core.indicators.numeric.momentum.CoppockCurveIndicator
import org.ta4j.core.indicators.numeric.momentum.KSTIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.indicators.numeric.momentum.ROCIndicator
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator
import org.ta4j.core.indicators.numeric.operation.BinaryOperation
import org.ta4j.core.indicators.numeric.operation.UnaryOperation
import org.ta4j.core.indicators.numeric.oscillators.CMOIndicator
import org.ta4j.core.indicators.numeric.oscillators.DPOIndicator
import org.ta4j.core.indicators.numeric.oscillators.MACDIndicator
import org.ta4j.core.indicators.numeric.oscillators.RAVIIndicator
import org.ta4j.core.indicators.bool.renko.RenkoDownIndicator
import org.ta4j.core.indicators.bool.renko.RenkoUpIndicator
import org.ta4j.core.indicators.bool.renko.RenkoXIndicator
import org.ta4j.core.indicators.numeric.oscillators.ConnorsRSIIndicator
import org.ta4j.core.indicators.numeric.oscillators.SchaffTrendCycleIndicator
import org.ta4j.core.indicators.numeric.oscillators.StochasticIndicator
import org.ta4j.core.indicators.numeric.oscillators.TrueStrengthIndexIndicator
import org.ta4j.core.indicators.numeric.statistics.CusumIndicator
import org.ta4j.core.indicators.numeric.statistics.EwmaVarianceIndicator
import org.ta4j.core.indicators.numeric.statistics.HurstExponentIndicator
import org.ta4j.core.indicators.numeric.statistics.KendallTauIndicator
import org.ta4j.core.indicators.numeric.statistics.PercentRankIndicator
import org.ta4j.core.indicators.numeric.statistics.SpearmanRankCorrelationIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonDownIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonUpIndicator
import org.ta4j.core.indicators.numeric.risk.UlcerIndexIndicator
import org.ta4j.core.indicators.numeric.statistics.CovarianceIndicator
import org.ta4j.core.indicators.numeric.statistics.MeanDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.SigmaIndicator
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardErrorIndicator
import org.ta4j.core.indicators.numeric.statistics.VarianceIndicator
import org.ta4j.core.indicators.numeric.statistics.ZScoreIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.rules.AverageTrueRangeStopGainRule
import org.ta4j.core.strategy.rules.AverageTrueRangeStopLossRule
import org.ta4j.core.strategy.rules.AverageTrueRangeTrailingStopGainRule
import org.ta4j.core.strategy.rules.AverageTrueRangeTrailingStopLossRule
import org.ta4j.core.strategy.rules.FixedAmountStopGainRule
import org.ta4j.core.strategy.rules.FixedAmountStopLossRule
import org.ta4j.core.strategy.rules.OverIndicatorRule
import org.ta4j.core.strategy.rules.OverOrEqualIndicatorRule
import org.ta4j.core.strategy.rules.RiskRewardRatioRule
import org.ta4j.core.strategy.rules.StopLossRule
import org.ta4j.core.strategy.rules.TakeProfitRule
import org.ta4j.core.strategy.rules.TrailingFixedAmountStopGainRule
import org.ta4j.core.strategy.rules.TrailingFixedAmountStopLossRule
import org.ta4j.core.strategy.rules.TrailingStopGainRule
import org.ta4j.core.strategy.rules.TrailingStopLossRule
import org.ta4j.core.strategy.rules.UnderIndicatorRule
import org.ta4j.core.strategy.rules.UnderOrEqualIndicatorRule
import org.ta4j.core.strategy.rules.VolatilityStopGainRule
import org.ta4j.core.strategy.rules.VolatilityStopLossRule
import org.ta4j.core.strategy.rules.VolatilityTrailingStopGainRule
import org.ta4j.core.strategy.rules.VolatilityTrailingStopLossRule

/**
 * NumericIndicator is a fluent class. It provides
 * methods to create rules and other "lightweight" indicators, using a
 * (hopefully) natural-looking and expressive series of method calls.
 *
 *
 *
 * Methods like plus(), minus() and sqrt() correspond directly to methods in the
 * `Num` interface. These methods create "lightweight" (not cached)
 * indicators to add, subtract, etc. Many methods are overloaded to accept
 * either `NumericIndicator` or `Number` arguments.
 *
 *
 *
 * Methods like sma() and ema() simply create the corresponding indicator
 * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first
 * argument. These methods usually instantiate cached objects.
 *
 *
 *
 * Another set of methods, like crossedOver() and isGreaterThan() create Rule
 * objects. These are also overloaded to accept both `NumericIndicator` and
 * `Number` arguments.
 */
abstract class NumericIndicator protected constructor(
    /**
     * Factory used for creation of this indicator that will be propagated to child indicators.
     */
    val numFactory: NumFactory,
) : Indicator<Num> {
    private var currentBeginTime: Instant = Instant.MIN

    override var value: Num = NaN
        protected set


    fun aroonUp(barCount: Int) = AroonUpIndicator(numFactory, this, barCount)

    fun aroonDown(barCount: Int) = AroonDownIndicator(numFactory, this, barCount)

    fun difference() = DifferenceIndicator(this)

    fun gain() = GainIndicator(this)

    fun loss() = LossIndicator(this)

    fun rsi(barCount: Int) = RSIIndicator(this, barCount)

    fun roc(barCount: Int) = ROCIndicator(numFactory, this, barCount)

    fun coppock(
        longRoCBarCount: Int = 14,
        shortRoCBarCount: Int = 11,
        wmaBarCount: Int = 10,
    ) = CoppockCurveIndicator(
        numFactory,
        this,
        longRoCBarCount,
        shortRoCBarCount,
        wmaBarCount,
    )

    fun ppo(shortBarCount: Int, longBarCount: Int) = PPOIndicator(
        numFactory = numFactory,
        indicator = this,
        shortBarCount = shortBarCount,
        longBarCount = longBarCount
    )

    fun cmo(barCount: Int) = CMOIndicator(numFactory, this, barCount)

    /** Stochastic of this indicator over [barCount] bars, 0 to 100. */
    fun stochastic(barCount: Int) = StochasticIndicator(this, barCount)

    /** Connors RSI. */
    fun connorsRsi(rsiPeriod: Int = 3, streakRsiPeriod: Int = 2, percentRankPeriod: Int = 100) =
        ConnorsRSIIndicator(this, rsiPeriod, streakRsiPeriod, percentRankPeriod)

    /** Schaff Trend Cycle. */
    fun schaffTrendCycle(fastPeriod: Int = 23, slowPeriod: Int = 50, cycleLength: Int = 10, smoothingPeriod: Int = 3) =
        SchaffTrendCycleIndicator(this, fastPeriod, slowPeriod, cycleLength, smoothingPeriod)

    /** Kairi Relative Index: percent distance from the SMA of [barCount] bars. */
    fun kri(barCount: Int): NumericIndicator {
        val sma = sma(barCount)
        return minus(sma).dividedBy(sma).multipliedBy(100)
    }

    /** True Strength Index of this indicator. */
    fun tsi(firstSmoothingPeriod: Int = 25, secondSmoothingPeriod: Int = 13) =
        TrueStrengthIndexIndicator(this, firstSmoothingPeriod, secondSmoothingPeriod)


    /**
     * @param other the other indicator
     *
     * @return `this + other`, rounded as necessary
     */
    fun plus(other: NumericIndicator) = BinaryOperation.sum(this, other)


    /**
     * @param n the other number
     *
     * @return `this + n`, rounded as necessary
     */
    fun plus(n: Number) = plus(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this - other`, rounded as necessary
     */
    fun minus(other: NumericIndicator) = BinaryOperation.difference(this, other)


    /**
     * @param n the other number
     *
     * @return `this - n`, rounded as necessary
     */
    fun minus(n: Number) = minus(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this * other`, rounded as necessary
     */
    fun multipliedBy(other: NumericIndicator) = BinaryOperation.product(this, other)


    /**
     * @param n the other number
     *
     * @return `this * n`, rounded as necessary
     */
    fun multipliedBy(n: Number) = multipliedBy(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this / other`, rounded as necessary
     */
    fun dividedBy(other: NumericIndicator) = BinaryOperation.quotient(this, other)


    /**
     * @param n the other number
     *
     * @return `this / n`, rounded as necessary
     */
    fun dividedBy(n: Number) = dividedBy(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the smaller of `this` and `other`; if they are equal,
     * `this` is returned.
     */
    fun min(other: NumericIndicator) = BinaryOperation.min(this, other)


    /**
     * @param n the other number
     *
     * @return the smaller of `this` and `n`; if they are equal,
     * `this` is returned.
     */
    fun min(n: Number) = min(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the greater of `this` and `other`; if they are equal,
     * `this` is returned.
     */
    fun max(other: NumericIndicator) = BinaryOperation.max(this, other)


    /**
     * @param n the other number
     *
     * @return the greater of `this` and `n`; if they are equal,
     * `this` is returned.
     */
    fun max(n: Number) = max(createConstant(n))


    /**
     * Returns an Indicator whose values are the absolute values of `this`.
     *
     * @return `abs(this)`
     */
    fun abs() = UnaryOperation.abs(this)


    /**
     * Returns an Indicator whose values are √(this).
     *
     * @return `√(this)`
     */
    fun sqrt() = UnaryOperation.sqrt(this)


    /**
     * Returns an Indicator whose values are `this * this`.
     *
     * @return `this * this`
     */
    fun squared() = this.multipliedBy(this)


    /**
     * @param barCount the time frame
     *
     * @return the [SMAIndicator] of `this`
     */
    fun sma(barCount: Int) = SMAIndicator(this, barCount)


    /**
     * @param barCount the time frame
     *
     * @return the [ATMAIndicator] of `this`
     */
    fun atma(barCount: Int) = ATMAIndicator(this, barCount)


    /**
     * @param barCount the time frame
     *
     * @return the [EMAIndicator] of `this`
     */
    fun ema(barCount: Int) = EMAIndicator(this, barCount)


    fun doubleEMA(barCount: Int) = DoubleEMAIndicator(this, barCount)


    fun tripleEma(barCount: Int) = TripleEMAIndicator(this, barCount)


    fun zlema(barCount: Int) = ZLEMAIndicator(this, barCount)


    fun mma(barCount: Int) = MMAIndicator(this, barCount)

    /** Exponentially weighted average with explicit [decayFactor] (weight of the previous value). */
    fun ewma(barCount: Int, decayFactor: Double) = EWMAIndicator(this, barCount, decayFactor)

    /** McGinley Dynamic. */
    fun mcGinley(barCount: Int) = McGinleyMAIndicator(this, barCount)

    /** Chande's Variable Index Dynamic Average driven by a CMO of [cmoBarCount] bars. */
    fun vidya(cmoBarCount: Int) = VIDYAIndicator(this, cmoBarCount)

    /** Displaced moving average: the SMA as it was [displacement] bars ago. */
    fun dma(barCount: Int, displacement: Int) = sma(barCount).previous(displacement)

    /** Exponential displaced moving average: the EMA as it was [displacement] bars ago. */
    fun edma(barCount: Int, displacement: Int) = ema(barCount).previous(displacement)


    fun hma(barCount: Int) = HMAIndicator(this, barCount)


    fun lwma(barCount: Int) = LWMAIndicator(this, barCount)


    fun wma(barCount: Int) = WMAIndicator(this, barCount)


    fun kama(
        barCountEffectiveRatio: Int,
        barCountFast: Int,
        barCountSlow: Int,
    ) = KAMAIndicator(numFactory, this, barCountEffectiveRatio, barCountFast, barCountSlow)

    fun macd(shortBarCount: Int = 12, longBarCount: Int = 26) =
        MACDIndicator(numFactory, this, shortBarCount, longBarCount)


    fun kst(
        rcma1SMABarCount: Int = 10,
        rcma1ROCBarCount: Int = 10,
        rcma2SMABarCount: Int = 10,
        rcma2ROCBarCount: Int = 15,
        rcma3SMABarCount: Int = 10,
        rcma3ROCBarCount: Int = 20,
        rcma4SMABarCount: Int = 15,
        rcma4ROCBarCount: Int = 30,
    ) = KSTIndicator(
        numFactory,
        this,
        rcma1SMABarCount,
        rcma1ROCBarCount,
        rcma2SMABarCount,
        rcma2ROCBarCount,
        rcma3SMABarCount,
        rcma3ROCBarCount,
        rcma4SMABarCount,
        rcma4ROCBarCount,
    )

    /**
     * @param barCount the time frame
     *
     * @return the [StandardDeviationIndicator] of `this`
     */
    fun stddev(barCount: Int) = StandardDeviationIndicator(this, barCount)


    fun stderr(barCount: Int) = StandardErrorIndicator(this, barCount)


    fun meanDeviation(barCount: Int) = MeanDeviationIndicator(this, barCount)


    fun variance(barCount: Int) = VarianceIndicator(this, barCount)

    /**
     * @return z-score of `this` against its simple moving average and sample standard deviation over [barCount] bars
     */
    /** Percent of the previous [period] values below the current one, 0 to 100. */
    fun percentRank(period: Int) = PercentRankIndicator(this, period)

    fun zScore(barCount: Int) = ZScoreIndicator(minus(sma(barCount)), stddev(barCount))


    fun sigma(barCount: Int) = SigmaIndicator(this, barCount)


    fun covariance(indicator: NumericIndicator, barCount: Int) = CovarianceIndicator(this, indicator, barCount)

    /** Spearman's rank correlation with [other] over the last [barCount] bars. */
    fun spearman(other: NumericIndicator, barCount: Int) = SpearmanRankCorrelationIndicator(this, other, barCount)

    /** Kendall's tau-b rank correlation with [other] over the last [barCount] bars. */
    fun kendallTau(other: NumericIndicator, barCount: Int) = KendallTauIndicator(this, other, barCount)

    /** Rolling Hurst exponent over the last [barCount] values. */
    fun hurst(barCount: Int, maximumLag: Int = minOf(10, barCount - 1)) =
        HurstExponentIndicator(this, barCount, maximumLag)

    /** Exponentially weighted variance seeded from the first [barCount] values. */
    fun ewmaVariance(barCount: Int, decayFactor: Double = 0.94) = EwmaVarianceIndicator(this, barCount, decayFactor)

    /** One-sided CUSUM of shortfalls below [targetMean]. */
    fun cusum(targetMean: Number, allowance: Number, outlierClipFactor: Number = 3.0, scaleDecay: Number = 0.94) =
        CusumIndicator(this, targetMean, allowance, outlierClipFactor, scaleDecay)

    fun distance() = DistanceFromMAIndicator(numFactory, this)

    fun ulcer(barCount: Int) = UlcerIndexIndicator(numFactory, this, barCount)

    fun ravi(shortSmaBarCount: Int, longSmaBarCount: Int) =
        RAVIIndicator(numFactory, this, shortSmaBarCount, longSmaBarCount)

    /**
     * @param barCount the time frame
     *
     * @return the [HighestValueIndicator] of `this`
     */
    fun highest(barCount: Int) = HighestValueIndicator(numFactory, this, barCount)


    /**
     * @param barCount the time frame
     *
     * @return the [LowestValueIndicator] of `this`
     */
    fun lowest(barCount: Int) = LowestValueIndicator(numFactory, this, barCount)


    /**
     * @return the [PreviousNumericValueIndicator] of `this` with
     * `barCount=1`
     */
    @JvmOverloads
    fun previous(barCount: Int = 1) = PreviousNumericValueIndicator(numFactory, this, barCount)

    /**
     * @return natural log return of `this` over [barCount] bars
     */
    fun logReturn(barCount: Int = 1) = LogReturnIndicator(this, barCount)

    /**
     * @return length of the current run of rising (positive) or falling (negative) values
     */
    fun streak() = StreakIndicator(this)

    /** True after [brickCount] consecutive bullish Renko bricks of size [pointSize]. */
    fun renkoUp(pointSize: Number, brickCount: Int) = RenkoUpIndicator(this, pointSize, brickCount)

    /** True after [brickCount] consecutive bearish Renko bricks of size [pointSize]. */
    fun renkoDown(pointSize: Number, brickCount: Int) = RenkoDownIndicator(this, pointSize, brickCount)

    /** True after [brickCount] consecutive Renko bricks of size [pointSize] in either direction. */
    fun renkoX(pointSize: Number, brickCount: Int) = RenkoXIndicator(this, pointSize, brickCount)

    fun dpo(barCount: Int) = DPOIndicator(numFactory, this, barCount)

    fun ratio() = RatioIndicator(this)

    /**
     * Returns sum of last barCount values
     *
     * @param barCount how many values sum up
     *
     * @return sum indicator
     */
    fun runningTotal(barCount: Int) = RunningTotalIndicator(numFactory, this, barCount)


    /**
     * Whether cross over occured in last 5 bars.
     *
     * @param other the other indicator
     *
     * @return the [CrossIndicator] of `this` and `other`
     */
    @JvmOverloads
    fun crossedOver(other: NumericIndicator, barCount: Int = 1) = CrossIndicator(this, other, barCount)


    /**
     * @param n the other number
     *
     * @return the [CrossIndicator] of `this` and `n`
     */
    fun crossedOver(n: Number) = crossedOver(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the [CrossIndicator] of `this` and
     * `other`
     */
    fun crossedUnder(other: NumericIndicator) = CrossIndicator(other, this, 1)


    /**
     * @param n the other number
     *
     * @return he current comparison of `this` and `n`
     */
    fun crossedUnder(n: Number) = crossedUnder(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return he current comparison of `this` and `other`
     */
    fun isGreaterThan(other: NumericIndicator) = value > other.value


    /**
     * @param n the other number
     *
     * @return the current comparison of `this` and `n`
     */
    fun isGreaterThan(n: Number) = isGreaterThan(createConstant(n))


    fun isGreaterThanRule(other: NumericIndicator) = OverIndicatorRule(this, other)

    fun isGreaterThanRule(n: Number) = isGreaterThanRule(createConstant(n))

    fun isGreaterThanOrEqualRule(other: NumericIndicator) = OverOrEqualIndicatorRule(this, other)

    fun isGreaterThanOrEqualRule(n: Number) = isGreaterThanOrEqualRule(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return he current comparison of `this` and `other`
     */
    fun isLessThan(other: NumericIndicator) = value < other.value


    fun isLessThanRule(other: NumericIndicator) = UnderIndicatorRule(this, other)

    fun isLessThanRule(n: Number) = isLessThanRule(createConstant(n))

    fun isLessThanOrEqualRule(other: NumericIndicator) = UnderOrEqualIndicatorRule(this, other)

    fun isLessThanOrEqualRule(n: Number) = isLessThanOrEqualRule(createConstant(n))

    /**
     * Treats `this` as the planned entry price.
     *
     * @param stop planned stop price
     * @param target planned target price
     * @param minRiskReward minimum accepted reward / risk ratio
     * @param tradeType direction of the planned trade
     *
     * @return [RiskRewardRatioRule] for the planned trade
     */
    fun riskRewardRatioRule(
        stop: NumericIndicator,
        target: NumericIndicator,
        minRiskReward: Number,
        tradeType: TradeType = TradeType.BUY,
    ) = RiskRewardRatioRule(this, stop, target, tradeType, numFactory.numOf(minRiskReward))


    /**
     * @param n the other number
     *
     * @return the [UnderIndicatorRule] of `this` and `n`
     */
    fun isLessThan(n: Number) = isLessThan(createConstant(n))

    /**
     * @param lossPercentage loss from the entry price that closes the position (e.g. 5 for 5 %)
     * @param runtimeContext context providing the open position
     *
     * @return [StopLossRule] watching `this` price
     */
    fun stopLossRule(lossPercentage: Number, runtimeContext: RuntimeContext) =
        StopLossRule(this, numFactory.numOf(lossPercentage), runtimeContext)

    /**
     * @param gainPercentage gain from the entry price that closes the position (e.g. 10 for 10 %)
     * @param runtimeContext context providing the open position
     *
     * @return [TakeProfitRule] watching `this` price
     */
    fun takeProfitRule(gainPercentage: Number, runtimeContext: RuntimeContext) =
        TakeProfitRule(this, numFactory.numOf(gainPercentage), runtimeContext)

    /**
     * @param lossPercentage retracement from the best price since entry that closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [TrailingStopLossRule] watching `this` price
     */
    fun trailingStopLossRule(lossPercentage: Number, runtimeContext: RuntimeContext) =
        TrailingStopLossRule(this, numFactory.numOf(lossPercentage), runtimeContext)

    /**
     * @param gainPercentage gain that arms the stop and retracement that then closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [TrailingStopGainRule] watching `this` price
     */
    fun trailingStopGainRule(gainPercentage: Number, runtimeContext: RuntimeContext) =
        TrailingStopGainRule(this, numFactory.numOf(gainPercentage), runtimeContext)

    /**
     * @param lossAmount price distance below (long) or above (short) the entry price that closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [FixedAmountStopLossRule] watching `this` price
     */
    fun fixedAmountStopLossRule(lossAmount: Number, runtimeContext: RuntimeContext) =
        FixedAmountStopLossRule(this, numFactory.numOf(lossAmount), runtimeContext)

    /**
     * @param gainAmount price distance above (long) or below (short) the entry price that closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [FixedAmountStopGainRule] watching `this` price
     */
    fun fixedAmountStopGainRule(gainAmount: Number, runtimeContext: RuntimeContext) =
        FixedAmountStopGainRule(this, numFactory.numOf(gainAmount), runtimeContext)

    /**
     * @param lossAmount price retracement from the best price since entry that closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [TrailingFixedAmountStopLossRule] watching `this` price
     */
    fun trailingFixedAmountStopLossRule(lossAmount: Number, runtimeContext: RuntimeContext) =
        TrailingFixedAmountStopLossRule(this, numFactory.numOf(lossAmount), runtimeContext)

    /**
     * @param gainAmount price gain that arms the stop and retracement that then closes the position
     * @param runtimeContext context providing the open position
     *
     * @return [TrailingFixedAmountStopGainRule] watching `this` price
     */
    fun trailingFixedAmountStopGainRule(gainAmount: Number, runtimeContext: RuntimeContext) =
        TrailingFixedAmountStopGainRule(this, numFactory.numOf(gainAmount), runtimeContext)

    /**
     * @param volatility volatility in price units, registered in the indicator context
     * @param coefficient multiple of [volatility] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return volatility based stop-loss watching `this` price
     */
    fun volatilityStopLossRule(volatility: NumericIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        VolatilityStopLossRule(this, volatility, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param atr average true range, registered in the indicator context
     * @param coefficient multiple of [atr] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return ATR based stop-loss watching `this` price
     */
    fun atrStopLossRule(atr: ATRIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        AverageTrueRangeStopLossRule(this, atr, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param volatility volatility in price units, registered in the indicator context
     * @param coefficient multiple of [volatility] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return volatility based take-profit watching `this` price
     */
    fun volatilityStopGainRule(volatility: NumericIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        VolatilityStopGainRule(this, volatility, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param atr average true range, registered in the indicator context
     * @param coefficient multiple of [atr] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return ATR based take-profit watching `this` price
     */
    fun atrStopGainRule(atr: ATRIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        AverageTrueRangeStopGainRule(this, atr, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param volatility volatility in price units, registered in the indicator context
     * @param coefficient multiple of [volatility] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return volatility based trailing stop-loss watching `this` price
     */
    fun volatilityTrailingStopLossRule(volatility: NumericIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        VolatilityTrailingStopLossRule(this, volatility, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param atr average true range, registered in the indicator context
     * @param coefficient multiple of [atr] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return ATR based trailing stop-loss watching `this` price
     */
    fun atrTrailingStopLossRule(atr: ATRIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        AverageTrueRangeTrailingStopLossRule(this, atr, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param volatility volatility in price units, registered in the indicator context
     * @param coefficient multiple of [volatility] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return volatility based trailing stop-gain watching `this` price
     */
    fun volatilityTrailingStopGainRule(volatility: NumericIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        VolatilityTrailingStopGainRule(this, volatility, numFactory.numOf(coefficient), runtimeContext)

    /**
     * @param atr average true range, registered in the indicator context
     * @param coefficient multiple of [atr] used as stop distance
     * @param runtimeContext context providing the open position
     *
     * @return ATR based trailing stop-gain watching `this` price
     */
    fun atrTrailingStopGainRule(atr: ATRIndicator, coefficient: Number, runtimeContext: RuntimeContext) =
        AverageTrueRangeTrailingStopGainRule(this, atr, numFactory.numOf(coefficient), runtimeContext)

    fun isLowest(barCount: Int) = IsLowestIndicator(this, barCount)

    fun isHighest(barCount: Int) = IsHighestIndicator(this, barCount)

    private fun createConstant(n: Number) = ConstantNumericIndicator(numFactory.numOf(n))


    fun simpleLinearRegression(barCount: Int) = SimpleLinearRegressionIndicator(this, barCount)


    @Synchronized
    override fun onBar(bar: Bar) {
        if (bar.beginTime.isAfter(currentBeginTime)) {
            updateState(bar)
            currentBeginTime = bar.beginTime
        }
    }


    /**
     * Called by this class when it is the right time to update internal state.
     *
     * @param bar new [Bar] received
     */
    protected abstract fun updateState(bar: Bar)
}
